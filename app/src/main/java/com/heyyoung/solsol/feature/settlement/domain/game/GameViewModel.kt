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

    private val _currentSpinStep = MutableStateFlow(0)
    val currentSpinStep: StateFlow<Int> = _currentSpinStep.asStateFlow()

    private val _totalSpinSteps = MutableStateFlow(0)
    val totalSpinSteps: StateFlow<Int> = _totalSpinSteps.asStateFlow()

    private val _currentHighlightIndex = MutableStateFlow(-1)
    val currentHighlightIndex: StateFlow<Int> = _currentHighlightIndex.asStateFlow()

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
                    is Msg.SpinStep -> {
                        Log.d(TAG, "SpinStep: step=${msg.currentStep}/${msg.totalSteps}, highlight=${msg.currentHighlightIndex}")
                        handleSpinStep(msg.currentStep, msg.totalSteps, msg.currentHighlightIndex)
                    }
                    is Msg.CircularStep -> {
                        Log.d(TAG, "CircularStep: step=${msg.currentStep}/${msg.totalSteps}, next=${msg.nextMemberIndex}, winner=${msg.winnerIndex}")
                        handleCircularStep(from, msg.currentStep, msg.totalSteps, msg.nextMemberIndex, msg.winnerIndex)
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

        // 호스트가 순환형 애니메이션을 시작 - 첫 번째 멤버에게 토큰 전달
        if (_role.value == Role.HOST) {
            viewModelScope.launch {
                startCircularAnimation(winner, order, totalSteps)
            }
        }
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

    private suspend fun startCircularAnimation(winner: String, order: List<String>, totalSteps: Int) {
        val winnerIndex = order.indexOf(winner)
        _totalSpinSteps.value = totalSteps
        
        Log.d(TAG, "startCircularAnimation: winner=$winner, winnerIndex=$winnerIndex, order=$order, totalSteps=$totalSteps")
        
        // 첫 번째 멤버부터 시작 (index 0)
        val firstMemberEndpointId = order[0]
        val firstMemberIsMe = firstMemberEndpointId == "self"
        
        Log.d(TAG, "startCircularAnimation: firstMember=$firstMemberEndpointId, isMe=$firstMemberIsMe")
        
        if (firstMemberIsMe) {
            // 호스트가 첫 번째 멤버인 경우, 직접 시작
            Log.d(TAG, "startCircularAnimation: starting from host (self)")
            passTokenToNext(0, totalSteps, order, winnerIndex, 80L)
        } else {
            // 다른 멤버가 첫 번째인 경우, 해당 멤버에게 토큰 전달
            Log.d(TAG, "startCircularAnimation: sending initial token to $firstMemberEndpointId")
            nearby.sendTo(firstMemberEndpointId, Msg.CircularStep(0, totalSteps, 0, winnerIndex).toJson())
        }
    }

    private suspend fun passTokenToNext(currentStep: Int, totalSteps: Int, order: List<String>, winnerIndex: Int, delay: Long) {
        val totalMembers = order.size
        val currentMemberIndex = currentStep % totalMembers
        
        Log.d(TAG, "passTokenToNext: step=$currentStep/$totalSteps, currentIndex=$currentMemberIndex, winnerIndex=$winnerIndex, delay=${delay}ms")
        
        // 현재 멤버 하이라이트
        _currentSpinStep.value = currentStep
        _currentHighlightIndex.value = currentMemberIndex
        
        Log.d(TAG, "passTokenToNext: highlighting member at index $currentMemberIndex")
        
        delay(delay)
        
        // 게임 종료 조건 확인
        if (currentStep >= totalSteps || 
            (currentStep > totalSteps - 10 && currentMemberIndex == winnerIndex)) {
            // 게임 종료
            Log.d(TAG, "passTokenToNext: GAME END - step=$currentStep, memberIndex=$currentMemberIndex, winnerIndex=$winnerIndex")
            delay(1000)
            if (_role.value == Role.HOST) {
                finishGame()
            }
            return
        }
        
        // 다음 단계 계산
        val nextStep = currentStep + 1
        val nextMemberIndex = nextStep % totalMembers
        val nextEndpointId = order[nextMemberIndex]
        
        // 시간이 지날수록 점점 느려짐
        val progress = nextStep.toFloat() / totalSteps
        val baseDelay = 200L
        val maxDelay = 800L
        val nextDelay = (baseDelay + (maxDelay - baseDelay) * progress * progress * progress).toLong()
        
        Log.d(TAG, "passTokenToNext: nextStep=$nextStep, nextIndex=$nextMemberIndex, nextEndpoint=$nextEndpointId, nextDelay=${nextDelay}ms")
        
        // 다음 멤버에게 토큰 전달
        if (nextEndpointId == "self") {
            // 나 자신에게 토큰이 돌아온 경우
            Log.d(TAG, "passTokenToNext: token returns to self, continuing...")
            passTokenToNext(nextStep, totalSteps, order, winnerIndex, nextDelay)
        } else {
            // 다른 멤버에게 토큰 전달
            Log.d(TAG, "passTokenToNext: sending token to $nextEndpointId")
            nearby.sendTo(nextEndpointId, Msg.CircularStep(nextStep, totalSteps, nextMemberIndex, winnerIndex).toJson())
        }
    }

    private fun handleCircularStep(from: String, currentStep: Int, totalSteps: Int, nextMemberIndex: Int, winnerIndex: Int) {
        val order = _spinOrderIds.value
        if (order.isEmpty()) {
            Log.w(TAG, "handleCircularStep: order is empty")
            return
        }
        
        val myIndex = order.indexOf("self")
        val currentMemberIndex = currentStep % order.size
        Log.d(TAG, "handleCircularStep: from=$from, step=$currentStep, nextIndex=$nextMemberIndex, myIndex=$myIndex, currentIndex=$currentMemberIndex, order=$order")
        
        // 현재 단계가 내 차례인지 확인 (nextMemberIndex가 아니라 currentMemberIndex 사용)
        if (myIndex == currentMemberIndex) {
            // 내 차례 - 하이라이트 표시하고 다음으로 전달
            Log.d(TAG, "handleCircularStep: MY TURN! highlighting and passing token")
            _currentSpinStep.value = currentStep
            _currentHighlightIndex.value = currentMemberIndex
            _totalSpinSteps.value = totalSteps
            
            viewModelScope.launch {
                // 시간이 지날수록 점점 느려짐
                val progress = currentStep.toFloat() / totalSteps
                val baseDelay = 80L
                val maxDelay = 800L
                val currentDelay = (baseDelay + (maxDelay - baseDelay) * progress * progress * progress).toLong()
                
                passTokenToNext(currentStep, totalSteps, order, winnerIndex, currentDelay)
            }
        } else {
            Log.d(TAG, "handleCircularStep: not my turn (myIndex=$myIndex, currentIndex=$currentMemberIndex)")
        }
    }

    private fun handleSpinStep(currentStep: Int, totalSteps: Int, highlightIndex: Int) {
        // 기존 SpinStep 메시지 처리 (호환성 유지)
        _currentSpinStep.value = currentStep
        _totalSpinSteps.value = totalSteps
        _currentHighlightIndex.value = highlightIndex
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
