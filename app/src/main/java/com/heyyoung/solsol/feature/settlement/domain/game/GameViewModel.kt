package com.heyyoung.solsol.feature.settlement.domain.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(app: Application) : AndroidViewModel(app) {
    
    val nearby = NearbyManager.get(app)
    
    companion object {
        const val SERVICE_ID = "SOLSOL_ROULETTE"
        const val TARGET_SPIN_MS = 3000L
        const val MIN_TICK_MS = 100L
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
    
    init {
        viewModelScope.launch {
            nearby.messages.collect { pair ->
                pair ?: return@collect
                val (from, msgStr) = pair
                when (val msg = Msg.parse(msgStr)) {
                    is Msg.Hello -> handleHello(from, msg)
                    is Msg.RoomUpdate -> _roomState.value = mergeSelf(msg.state)
                    is Msg.AssignNumbers -> applyAssignments(msg.assignments)
                    is Msg.StartInstruction -> startInstruction(msg.seconds)
                    is Msg.StartGame -> startGame(msg.winnerEndpointId, msg.cycles, msg.tickMs, msg.order)
                    null -> {}
                }
            }
        }
    }
    
    fun createRoom(roomTitle: String, playerName: String) {
        nearby.localEndpointName = playerName
        _role.value = Role.HOST
        
        val hostMember = Member(
            endpointId = "self",
            displayName = playerName,
            isSelf = true,
            isHost = true
        )
        
        _roomState.value = RoomState(
            title = roomTitle,
            hostEndpointId = "self",
            members = listOf(hostMember),
            phase = Phase.IDLE
        )
        
        nearby.startAdvertising(SERVICE_ID, roomTitle)
    }
    
    fun startDiscovering() {
        _role.value = Role.PARTICIPANT
        nearby.startDiscovery(SERVICE_ID)
    }
    
    fun joinRoom(endpointId: String, playerName: String) {
        nearby.localEndpointName = playerName
        nearby.requestConnection(endpointId)
    }
    
    fun startGathering() {
        if (_role.value != Role.HOST) return
        
        updateRoomPhase(Phase.GATHERING)
        broadcastRoom()
    }
    
    fun assignNumbers() {
        if (_role.value != Role.HOST) return
        
        val state = _roomState.value ?: return
        val unassignedMembers = state.members.filter { it.number == null }
        val availableNumbers = (1..state.members.size).toList().shuffled()
        
        val assignments = unassignedMembers.mapIndexed { index, member ->
            member.endpointId to availableNumbers[index]
        }.toMap()
        
        applyAssignments(assignments)
        nearby.sendToAll(Msg.AssignNumbers(assignments).toJson())
    }
    
    fun sendInstruction(seconds: Int = 5) {
        if (_role.value != Role.HOST) return
        
        updateRoomPhase(Phase.INSTRUCTION)
        nearby.sendToAll(Msg.StartInstruction(seconds).toJson())
    }
    
    fun startGameHost(tickMs: Long = 180L, cycles: Int = 3) {
        if (_role.value != Role.HOST) return
        
        var state = _roomState.value ?: return
        
        if (state.members.any { it.number == null }) {
            assignNumbers()
            state = _roomState.value ?: return
        }
        
        val members = state.members.filter { it.number != null }.sortedBy { it.number }
        val winner = members[Random.nextInt(members.size)].endpointId
        val order = members.map { it.endpointId }
        
        val totalSteps = (order.size * cycles) + order.indexOf(winner) + 1
        val computedTick = (TARGET_SPIN_MS / totalSteps.coerceAtLeast(1)).coerceAtLeast(MIN_TICK_MS)
        
        nearby.sendToAll(Msg.StartGame(winner, cycles, computedTick, order).toJson())
        
        val delayMs = totalSteps * computedTick
        viewModelScope.launch {
            delay(delayMs)
            updateRoomPhase(Phase.FINISHED)
            broadcastRoom()
        }
    }
    
    fun leaveRoom() {
        nearby.disconnectAll()
        nearby.stopAdvertising()
        nearby.stopDiscovery()
        _role.value = Role.NONE
        _roomState.value = null
        _isInstructionVisible.value = false
    }
    
    fun dismissInstruction() {
        _isInstructionVisible.value = false
    }
    
    private fun handleHello(from: String, msg: Msg.Hello) {
        if (_role.value != Role.HOST) return
        
        val state = _roomState.value ?: return
        val existingMember = state.members.find { it.endpointId == from }
        
        if (existingMember == null) {
            val newMember = Member(
                endpointId = from,
                displayName = msg.name,
                isSelf = false,
                isHost = false
            )
            
            val updatedMembers = state.members + newMember
            _roomState.value = state.copy(members = updatedMembers)
            broadcastRoom()
        }
    }
    
    private fun mergeSelf(remoteState: RoomState): RoomState {
        val selfMember = remoteState.members.find { it.endpointId == "self" }
        
        return if (selfMember != null) {
            remoteState
        } else {
            val newSelfMember = Member(
                endpointId = "self",
                displayName = nearby.localEndpointName,
                isSelf = true,
                isHost = false
            )
            remoteState.copy(members = remoteState.members + newSelfMember)
        }
    }
    
    private fun applyAssignments(assignments: Map<String, Int>) {
        val state = _roomState.value ?: return
        
        val updatedMembers = state.members.map { member ->
            val assignedNumber = assignments[member.endpointId]
            if (assignedNumber != null) {
                member.copy(number = assignedNumber)
            } else {
                member
            }
        }
        
        _roomState.value = state.copy(members = updatedMembers)
        
        if (_role.value == Role.HOST) {
            broadcastRoom()
        }
    }
    
    private fun startInstruction(seconds: Int) {
        if (_role.value == Role.HOST) return
        
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
    
    private fun startGame(winnerEndpointId: String, cycles: Int, tickMs: Long, order: List<String>) {
        updateRoomPhase(Phase.RUNNING)
        _spinCycles.value = cycles
        _spinTickMs.value = tickMs
        _spinOrderIds.value = order
        
        val state = _roomState.value ?: return
        _roomState.value = state.copy(winnerEndpointId = winnerEndpointId)
    }
    
    private fun updateRoomPhase(phase: Phase) {
        val state = _roomState.value ?: return
        _roomState.value = state.copy(phase = phase)
    }
    
    private fun broadcastRoom() {
        val state = _roomState.value ?: return
        nearby.sendToAll(Msg.RoomUpdate(state).toJson())
    }
}