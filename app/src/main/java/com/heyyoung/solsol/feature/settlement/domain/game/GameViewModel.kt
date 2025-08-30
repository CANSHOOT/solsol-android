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
        const val TARGET_SPIN_MS = 7_000L  // 총 목표 회전 시간 7초
        const val MIN_TICK_MS = 200L        // 최소 단계 간격(호스트 기준)
    }

    // --------- 상태 스트림 ---------

    private val _role = MutableStateFlow(Role.NONE)
    val role: StateFlow<Role> = _role.asStateFlow()

    private val _roomState = MutableStateFlow<RoomState?>(null)
    val roomState: StateFlow<RoomState?> = _roomState.asStateFlow()

    private val _spinTickMs = MutableStateFlow(MIN_TICK_MS)
    val spinTickMs: StateFlow<Long> = _spinTickMs.asStateFlow()

    private val _spinCycles = MutableStateFlow(1)
    val spinCycles: StateFlow<Int> = _spinCycles.asStateFlow()

    /** 토큰 순환 순서: **userId** 리스트 */
    private val _spinOrderIds = MutableStateFlow<List<String>>(emptyList())
    val spinOrderIds: StateFlow<List<String>> = _spinOrderIds.asStateFlow()

    private val _currentSpinStep = MutableStateFlow(0)
    val currentSpinStep: StateFlow<Int> = _currentSpinStep.asStateFlow()

    private val _totalSpinSteps = MutableStateFlow(0)
    val totalSpinSteps: StateFlow<Int> = _totalSpinSteps.asStateFlow()

    /** 현재 하이라이트 인덱스(로컬 order 기준) */
    private val _currentHighlightIndex = MutableStateFlow(-1)
    val currentHighlightIndex: StateFlow<Int> = _currentHighlightIndex.asStateFlow()

    private val _isInstructionVisible = MutableStateFlow(false)
    val isInstructionVisible: StateFlow<Boolean> = _isInstructionVisible.asStateFlow()

    private val _instructionCountdown = MutableStateFlow(0)
    val instructionCountdown: StateFlow<Int> = _instructionCountdown.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** 호스트: userId -> (로컬) endpointId 매핑 */
    private val userIdToEndpointId = mutableMapOf<String, String>()

    init {
        // Nearby 메시지 수신 처리
        viewModelScope.launch {
            nearby.messages.collect { pair ->
                pair ?: return@collect
                val (fromEndpointId, raw) = pair
                when (val msg = Msg.parse(raw)) {
                    is Msg.Hello -> handleHello(fromEndpointId, msg) // 호스트에서만 유효
                    is Msg.RoomUpdate -> {
                        Log.d(TAG, "RoomUpdate from=$fromEndpointId")
                        _roomState.value = mergeSelf(msg.state)
                    }
                    is Msg.AssignNumbers -> {
                        Log.d(TAG, "AssignNumbers: ${msg.assignments}")
                        applyAssignmentsByUserId(msg.assignments) // 키를 userId로 사용
                    }
                    is Msg.StartInstruction -> {
                        Log.d(TAG, "StartInstruction: ${msg.seconds}s")
                        startInstruction(msg.seconds)
                    }
                    is Msg.StartGame -> {
                        Log.d(TAG, "StartGame: winner=${msg.winnerUserId}, cycles=${msg.cycles}, tick=${msg.tickMs}")
                        // winnerUserId / order 에 **userId**가 온다고 가정
                        startGame(
                            winnerUserId = msg.winnerUserId,
                            cycles = msg.cycles,
                            tickMs = msg.tickMs,
                            orderUserIds = msg.order
                        )
                    }
                    is Msg.SpinStep -> {
                        // (레거시 호환) 단일 하이라이트 스텝
                        handleSpinStep(msg.currentStep, msg.totalSteps, msg.currentHighlightIndex)
                    }
                    is Msg.CircularStep -> {
                        // **수정**: 게스트는 하이라이트만 반영하고, 토큰 전송은 하지 않음(호스트 드라이브)
                        Log.d(TAG, "CircularStep: step=${msg.currentStep}/${msg.totalSteps}, next=${msg.nextMemberIndex}, winner=${msg.winnerIndex}")
                        handleCircularStepGuestOnly(msg.currentStep, msg.totalSteps)
                    }
                    null -> Log.w(TAG, "Unknown message ignored: $raw")
                }
            }
        }
    }

    // --------- 호스트/참가자 엔트리 ---------

    /** 방 생성(호스트 시작) */
    fun createRoom(roomTitle: String, settlementAmount: Long) {
        viewModelScope.launch {
            val userInfo = tokenManager.getCurrentUserInfo()
            val playerName = userInfo?.name ?: "사용자"
            val myUserId = userInfo?.userId ?: "unknown"
            Log.d(TAG, "createRoom(): title=$roomTitle, player=$playerName")

            nearby.localEndpointName = playerName
            _role.value = Role.HOST

            val hostMember = Member(
                endpointId = "",            // 호스트 자신의 endpointId는 로컬에서 불필요(전송 시 사용 안 함)
                displayName = playerName,
                isSelf = true,
                isHost = true,
                userId = myUserId
            )

            _roomState.value = RoomState(
                title = roomTitle,
                hostEndpointId = myUserId,  // 전역 ID로 사용
                members = listOf(hostMember),
                phase = Phase.IDLE,
                settlementAmount = settlementAmount
            )

            // 광고 이벤트 콜백
            nearby.onAdvertisingEvent = { success, msg ->
                if (success) {
                    Log.d(TAG, "Advertising started → GATHERING")
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

    /** 참가자: 방 검색 시작 */
    fun startDiscovering() {
        Log.d(TAG, "startDiscovering")
        _role.value = Role.PARTICIPANT
        nearby.startDiscovery(SERVICE_ID)
    }

    /** 참가자: 특정 엔드포인트에 연결 요청 */
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

    /** (호스트) 아직 번호 없으면 배정 후 브로드캐스트. 키는 userId */
    fun assignNumbers() {
        if (_role.value != Role.HOST) return
        val state = _roomState.value ?: return

        val unassigned = state.members.filter { it.number == null }
        if (unassigned.isEmpty()) {
            Log.d(TAG, "assignNumbers: nothing to assign")
            return
        }

        val available = (1..state.members.size).shuffled()
        val assignmentsByUserId = unassigned.mapIndexed { idx, m ->
            m.userId to available[idx]
        }.toMap()

        Log.d(TAG, "assignNumbers: $assignmentsByUserId")
        applyAssignmentsByUserId(assignmentsByUserId)
        // Protocol 상 이름은 AssignNumbers지만 값은 userId로 전송
        nearby.sendToAll(Msg.AssignNumbers(assignmentsByUserId).toJson())
    }

    /** (호스트) 안내 모달 시작 */
    fun sendInstruction(seconds: Int = 5) {
        if (_role.value != Role.HOST) return
        Log.d(TAG, "sendInstruction: $seconds s")
        updateRoomPhase(Phase.INSTRUCTION)
        startInstruction(seconds) // 호스트도 로컬로 표시
        nearby.sendToAll(Msg.StartInstruction(seconds).toJson())
    }

    /** (호스트) 게임 시작: 전역 ID(userId)로 순서/승자 고정, 호스트가 전 과정을 구동 */
    fun startGameHost(tickMs: Long = MIN_TICK_MS, cycles: Int = 1) {
        if (_role.value != Role.HOST) return
        var state = _roomState.value ?: return

        if (state.members.any { it.number == null }) {
            Log.d(TAG, "startGameHost: numbers not assigned; assigning now")
            assignNumbers()
            state = _roomState.value ?: return
        }

        val ordered = state.members.filter { it.number != null }.sortedBy { it.number }
        if (ordered.isEmpty()) {
            Log.w(TAG, "startGameHost: no members with numbers")
            return
        }

        val winnerUserId = ordered[Random.nextInt(ordered.size)].userId
        val orderUserIds = ordered.map { it.userId }

        val totalSteps = (TARGET_SPIN_MS / MIN_TICK_MS).toInt()
        val computedTick = MIN_TICK_MS

        Log.d(TAG, "startGameHost: winner=$winnerUserId, cycles=$cycles, tick=$computedTick, steps=$totalSteps")

        // 로컬 상태 세팅 및 브로드캐스트(필드명은 그대로지만 값은 userId)
        startGame(winnerUserId, cycles, computedTick, orderUserIds)
        nearby.sendToAll(Msg.StartGame(winnerUserId, cycles, computedTick, orderUserIds).toJson())

        // 호스트가 전체 순환 애니메이션을 구동(게스트는 수신만)
        viewModelScope.launch {
            driveCircularAnimationAsHost(winnerUserId, orderUserIds, totalSteps)
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
        userIdToEndpointId.clear()
        _spinOrderIds.value = emptyList()
        _currentHighlightIndex.value = -1
        _currentSpinStep.value = 0
        _totalSpinSteps.value = 0
    }

    fun dismissInstruction() {
        Log.d(TAG, "dismissInstruction")
        _isInstructionVisible.value = false
    }

    // --------- 내부 로직 ---------

    /** (호스트) 연결 완료 후 게스트 인사 수신 → 멤버 추가 + userId→endpointId 매핑 */
    private fun handleHello(fromEndpointId: String, msg: Msg.Hello) {
        if (_role.value != Role.HOST) return

        val state = _roomState.value ?: return
        userIdToEndpointId[msg.userId] = fromEndpointId

        val exists = state.members.any { it.userId == msg.userId }
        if (!exists) {
            Log.d(TAG, "handleHello: new member endpoint=$fromEndpointId (${msg.name}, userId=${msg.userId})")
            val newMember = Member(
                endpointId = fromEndpointId,
                displayName = msg.name,
                isSelf = false,
                isHost = false,
                userId = msg.userId
            )
            _roomState.value = state.copy(members = state.members + newMember)
            broadcastRoom()
        } else {
            Log.d(TAG, "handleHello: already joined userId=${msg.userId}")
        }
    }

    /** RoomUpdate 수신 시 로컬 사용자에 대해 isSelf만 마킹 */
    private suspend fun mergeSelf(remoteState: RoomState): RoomState {
        val myUserId = tokenManager.getCurrentUserInfo()?.userId
        val updatedMembers = remoteState.members.map { m ->
            m.copy(isSelf = (m.userId == myUserId))
        }
        return remoteState.copy(members = updatedMembers)
    }

    /** 번호 배정(키: userId) 적용 */
    private fun applyAssignmentsByUserId(assignments: Map<String, Int>) {
        val state = _roomState.value ?: return
        val updated = state.members.map { m ->
            assignments[m.userId]?.let { num -> m.copy(number = num) } ?: m
        }
        _roomState.value = state.copy(members = updated)
        if (_role.value == Role.HOST) broadcastRoom()
    }

    /** 안내 시작(모달 + 카운트다운) */
    private fun startInstruction(seconds: Int) {
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

    /** 게임 로컬 상태 세팅(전역 ID 기반) */
    private fun startGame(
        winnerUserId: String,
        cycles: Int,
        tickMs: Long,
        orderUserIds: List<String>
    ) {
        updateRoomPhase(Phase.RUNNING)
        _spinCycles.value = cycles
        _spinTickMs.value = tickMs
        _spinOrderIds.value = orderUserIds

        _roomState.value = _roomState.value?.copy(winnerUserId = winnerUserId)
    }

    /** (호스트 전용) 전체 순환 애니메이션을 중앙에서 구동 */
    /** (호스트 전용) 전체 순환 애니메이션을 중앙에서 구동 - 모든 단말에 매 스텝 브로드캐스트 */
    private suspend fun driveCircularAnimationAsHost(
        winnerUserId: String,
        orderUserIds: List<String>,
        totalSteps: Int
    ) {
        _totalSpinSteps.value = totalSteps

        val winnerIndex = orderUserIds.indexOf(winnerUserId).coerceAtLeast(0)
        Log.d(TAG, "driveCircularAnimationAsHost: winner=$winnerUserId, winnerIndex=$winnerIndex, order=$orderUserIds, totalSteps=$totalSteps")

        var step = 0
        while (true) {
            val currentIndex = step % orderUserIds.size

            // 호스트 자신의 화면도 매 스텝 갱신
            _currentSpinStep.value = step
            _currentHighlightIndex.value = currentIndex

            // ✅ 모든 단말(게스트 전원)에게 현재 스텝 브로드캐스트
            nearby.sendToAll(
                Msg.CircularStep(
                    currentStep = step,
                    totalSteps = totalSteps,
                    nextMemberIndex = (step + 1) % orderUserIds.size,
                    winnerIndex = winnerIndex
                ).toJson()
            )

            // 종료 조건: 총 스텝 도달 또는 마지막 10스텝 동안 승자에서 정지
            if (step >= totalSteps || (step > totalSteps - 10 && currentIndex == winnerIndex)) {
                Log.d(TAG, "driveCircularAnimationAsHost: GAME END at step=$step, index=$currentIndex (winnerIndex=$winnerIndex)")
                delay(1000)
                finishGame()
                return
            }

            // 느려지는 지연(큐빅 이징)
            val progress = step.toFloat() / totalSteps
            val baseDelay = 800L
            val maxDelay = 1200L
            val delayMs = (baseDelay + (maxDelay - baseDelay) * progress * progress * progress).toLong()

            step++
            delay(delayMs)
        }
    }

    /** 게스트: 하이라이트만 반영(전달/드라이브하지 않음) */
    private fun handleCircularStepGuestOnly(currentStep: Int, totalSteps: Int) {
        val order = _spinOrderIds.value
        if (order.isEmpty()) {
            Log.w(TAG, "handleCircularStepGuestOnly: order empty")
            return
        }
        val index = currentStep % order.size
        _currentSpinStep.value = currentStep
        _totalSpinSteps.value = totalSteps
        _currentHighlightIndex.value = index
    }

    /** (레거시) 단일 스텝 처리 */
    private fun handleSpinStep(currentStep: Int, totalSteps: Int, highlightIndex: Int) {
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
