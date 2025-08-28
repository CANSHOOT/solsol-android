package com.heyyoung.solsol.feature.settlement.domain.game

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.core.auth.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class GameViewModel @Inject constructor(
    app: Application,
    private val tokenManager: TokenManager
) : AndroidViewModel(app) {

    private val TAG = "GameViewModel"

    val nearby = NearbyManager.get(app, tokenManager)

    companion object {
        const val SERVICE_ID = "SOLSOL_ROULETTE"
        const val TARGET_SPIN_MS = 10000L // 10초로 변경
        const val MIN_TICK_MS = 200L // 불빛 깜빡임 간격
    }

    private val _role = MutableStateFlow(Role.NONE)
    val role: StateFlow<Role> = _role.asStateFlow()

    private val _roomState = MutableStateFlow<RoomState?>(null)
    val roomState: StateFlow<RoomState?> = _roomState.asStateFlow()

    private val _spinTickMs = MutableStateFlow(180L)
    val spinTickMs: StateFlow<Long> = _spinTickMs.asStateFlow()

    private val _spinCycles = MutableStateFlow(3)
    val spinCycles: StateFlow<Int> = _spinCycles.asStateFlow()

    private val _spinOrderIds = MutableStateFlow<List<String>>(emptyList())
    val spinOrderIds: StateFlow<List<String>> = _spinOrderIds.asStateFlow()

    private val _isInstructionVisible = MutableStateFlow(false)
    val isInstructionVisible: StateFlow<Boolean> = _isInstructionVisible.asStateFlow()

    private val _instructionCountdown = MutableStateFlow(0)
    val instructionCountdown: StateFlow<Int> = _instructionCountdown.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Nearby 메시지 수신 처리
        viewModelScope.launch {
            nearby.messages.collect { pair ->
                pair ?: return@collect
                val (from, msgStr) = pair
                when (val msg = Msg.parse(msgStr)) {
                    is Msg.Hello -> handleHello(from, msg)
                    is Msg.RoomUpdate -> {
                        Log.d(TAG, "RoomUpdate from=$from")
                        _roomState.value = mergeSelf(msg.state)
                    }
                    is Msg.AssignNumbers -> {
                        Log.d(TAG, "AssignNumbers: ${msg.assignments}")
                        applyAssignments(msg.assignments)
                    }
                    is Msg.StartInstruction -> {
                        Log.d(TAG, "StartInstruction: ${msg.seconds}s")
                        startInstruction(msg.seconds)
                    }
                    is Msg.StartGame -> {
                        Log.d(TAG, "StartGame: winner=${msg.winnerEndpointId}, cycles=${msg.cycles}, tick=${msg.tickMs}")
                        startGame(msg.winnerEndpointId, msg.cycles, msg.tickMs, msg.order)
                    }
                    null -> Log.w(TAG, "Unknown message ignored: $msgStr")
                }
            }
        }
    }

    /** 방 생성(호스트 시작) */
    fun createRoom(roomTitle: String, settlementAmount: Long) {
        viewModelScope.launch {
            val userInfo = tokenManager.getCurrentUserInfo()
            val playerName = userInfo?.name ?: "사용자"
            val userId = userInfo?.userId ?: " "
            Log.d(TAG, "createRoom(): title=$roomTitle, player=$playerName")

            nearby.localEndpointName = playerName
            _role.value = Role.HOST

            val hostMember = Member(
                endpointId = "self",
                displayName = playerName,
                isSelf = true,
                isHost = true,
                userId = userId
            )

            _roomState.value = RoomState(
                title = roomTitle,
                hostEndpointId = "self",
                members = listOf(hostMember),
                phase = Phase.IDLE,
                settlementAmount = settlementAmount
            )

            // 광고 이벤트를 VM에서 수신하여 상태 전환
            nearby.onAdvertisingEvent = { success, msg ->
                if (success) {
                    Log.d(TAG, "Advertising started → LOBBY")
                    _roomState.update { it?.copy(phase = Phase.GATHERING) }
                } else {
                    Log.e(TAG, "Advertising failed: $msg")
                    _errorMessage.value = msg ?: "광고 시작 실패"
                    _roomState.update { it?.copy(phase = Phase.IDLE) }
                }
            }

            // endpointName = playerName 으로 광고 시작
            nearby.startAdvertising(SERVICE_ID, playerName)
        }
    }

    fun startDiscovering() {
        Log.d(TAG, "startDiscovering")
        _role.value = Role.PARTICIPANT
        nearby.startDiscovery(SERVICE_ID)
    }

    fun joinRoom(endpointId: String) {
        viewModelScope.launch {
            val userInfo = tokenManager.getCurrentUserInfo()
            val playerName = userInfo?.name ?: "사용자"
            
            Log.d(TAG, "joinRoom: endpointId=$endpointId, name=$playerName")
            nearby.localEndpointName = playerName
            nearby.requestConnection(endpointId)
        }
    }

    fun startGathering() {
        if (_role.value != Role.HOST) return
        Log.d(TAG, "startGathering")
        updateRoomPhase(Phase.GATHERING)
        broadcastRoom()
    }

    fun assignNumbers() {
        if (_role.value != Role.HOST) return

        val state = _roomState.value ?: return
        val unassignedMembers = state.members.filter { it.number == null }
        if (unassignedMembers.isEmpty()) {
            Log.d(TAG, "assignNumbers: nothing to assign")
            return
        }

        val availableNumbers = (1..state.members.size).toList().shuffled()
        val assignments = unassignedMembers.mapIndexed { index, member ->
            member.endpointId to availableNumbers[index]
        }.toMap()

        Log.d(TAG, "assignNumbers: $assignments")
        applyAssignments(assignments)
        nearby.sendToAll(Msg.AssignNumbers(assignments).toJson())
    }

    fun sendInstruction(seconds: Int = 5) {
        if (_role.value != Role.HOST) return
        Log.d(TAG, "sendInstruction: $seconds s")
        updateRoomPhase(Phase.INSTRUCTION)
        // 호스트 자신도 instruction을 받도록 수정
        startInstruction(seconds)
        nearby.sendToAll(Msg.StartInstruction(seconds).toJson())
    }

    fun startGameHost(tickMs: Long = 200L, cycles: Int = 1) {
        if (_role.value != Role.HOST) return

        var state = _roomState.value ?: return

        if (state.members.any { it.number == null }) {
            Log.d(TAG, "startGameHost: numbers not assigned; assigning now")
            assignNumbers()
            state = _roomState.value ?: return
        }

        val members = state.members.filter { it.number != null }.sortedBy { it.number }
        if (members.isEmpty()) {
            Log.w(TAG, "startGameHost: no members with numbers")
            return
        }

        val winner = members[Random.nextInt(members.size)].endpointId
        val order = members.map { it.endpointId }

        val totalSteps = (TARGET_SPIN_MS / MIN_TICK_MS).toInt() // 10초 동안 0.2초마다 = 50번 깜빡임
        val computedTick = MIN_TICK_MS

        Log.d(TAG, "startGameHost: winner=$winner, cycles=$cycles, tick=$computedTick, steps=$totalSteps")

        // 호스트 자신도 게임 시작 로직을 받도록 수정
        startGame(winner, cycles, computedTick, order)
        nearby.sendToAll(Msg.StartGame(winner, cycles, computedTick, order).toJson())

        // 자동 종료 타이머 제거 - UI에서 수동으로 처리
    }

    fun finishGame() {
        if (_role.value == Role.HOST) {
            Log.d(TAG, "finishGame - transitioning to finished phase")
            updateRoomPhase(Phase.FINISHED)
            broadcastRoom()
        }
    }

    fun leaveRoom() {
        Log.d(TAG, "leaveRoom")
        nearby.disconnectAll()
        nearby.stopAdvertising()
        nearby.stopDiscovery()
        _role.value = Role.NONE
        _roomState.value = null
        _isInstructionVisible.value = false
    }

    fun dismissInstruction() {
        Log.d(TAG, "dismissInstruction")
        _isInstructionVisible.value = false
    }

    private fun handleHello(from: String, msg: Msg.Hello) {
        if (_role.value != Role.HOST) return

        val state = _roomState.value ?: return
        val existingMember = state.members.find { it.endpointId == from }

        if (existingMember == null) {
            Log.d(TAG, "handleHello: new member $from (${msg.name})")
            val newMember = Member(
                endpointId = from,
                displayName = msg.name,
                isSelf = false,
                isHost = false,
                userId = msg.userId
            )

            val updatedMembers = state.members + newMember
            _roomState.value = state.copy(members = updatedMembers)
            broadcastRoom()
        } else {
            Log.d(TAG, "handleHello: already joined $from")
        }
    }

    private suspend fun mergeSelf(remoteState: RoomState): RoomState {
        val myName = nearby.localEndpointName ?: "Me"
        val myUserId = tokenManager.getCurrentUserInfo()?.userId

        val updatedMembers = remoteState.members.map { member ->
            when {
                // 참가자: userId 같고 endpointId != self → isSelf
                member.userId == myUserId -> {
                    member.copy(isSelf = true)
                }
                // 나머지 멤버는 전부 false
                else -> member.copy(isSelf = false)
            }
        }

        // 혹시 자기 자신이 아예 없다면 새로 추가
        val hasSelf = updatedMembers.any { it.isSelf }
        return if (!hasSelf) {
            val userInfo = tokenManager.getCurrentUserInfo()
            val newSelfMember = Member(
                endpointId = "self",
                displayName = myName,
                isSelf = true,
                isHost = false,
                userId = userInfo?.userId ?: "unknown"
            )
            remoteState.copy(members = updatedMembers + newSelfMember)
        } else {
            remoteState.copy(members = updatedMembers)
        }
    }

    private fun applyAssignments(assignments: Map<String, Int>) {
        val state = _roomState.value ?: return

        val updatedMembers = state.members.map { member ->
            val assignedNumber = assignments[member.endpointId]
            if (assignedNumber != null) member.copy(number = assignedNumber) else member
        }

        _roomState.value = state.copy(members = updatedMembers)

        if (_role.value == Role.HOST) broadcastRoom()
    }

    private fun startInstruction(seconds: Int) {
        // 호스트도 instruction을 받을 수 있도록 수정
        _isInstructionVisible.value = true
        _instructionCountdown.value = seconds

        viewModelScope.launch {
            repeat(seconds) {
                delay(1000)
                _instructionCountdown.value = _instructionCountdown.value - 1
            }
            _isInstructionVisible.value = false
        }
    }

    private fun startGame(
        winnerEndpointId: String,
        cycles: Int,
        tickMs: Long,
        order: List<String>
    ) {
        updateRoomPhase(Phase.RUNNING)
        _spinCycles.value = cycles
        _spinTickMs.value = tickMs
        _spinOrderIds.value = order

        val state = _roomState.value ?: return
        _roomState.value = state.copy(winnerEndpointId = winnerEndpointId)
    }

    private fun updateRoomPhase(phase: Phase) {
        val state = _roomState.value ?: return
        if (state.phase != phase) Log.d(TAG, "updateRoomPhase: ${state.phase} -> $phase")
        _roomState.value = state.copy(phase = phase)
    }

    private fun broadcastRoom() {
        val state = _roomState.value ?: return
        Log.d(TAG, "broadcastRoom: members=${state.members.size}, phase=${state.phase}")
        nearby.sendToAll(Msg.RoomUpdate(state).toJson())
    }
}
