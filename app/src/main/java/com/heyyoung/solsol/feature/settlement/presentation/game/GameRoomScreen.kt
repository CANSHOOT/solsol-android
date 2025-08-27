package com.heyyoung.solsol.feature.settlement.presentation.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyyoung.solsol.feature.settlement.domain.game.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRoomScreen(
    onNavigateBack: () -> Unit = {},
    onGameFinished: (String) -> Unit = {},
    viewModel: GameViewModel = viewModel()
) {
    val roomState by viewModel.roomState.collectAsState()
    val role by viewModel.role.collectAsState()
    val isInstructionVisible by viewModel.isInstructionVisible.collectAsState()
    val instructionCountdown by viewModel.instructionCountdown.collectAsState()
    val spinOrderIds by viewModel.spinOrderIds.collectAsState()
    val spinTickMs by viewModel.spinTickMs.collectAsState()
    val spinCycles by viewModel.spinCycles.collectAsState()

    val highlightIndex = remember { mutableIntStateOf(-1) }
    val rotationAngle = remember { Animatable(0f) }

    LaunchedEffect(roomState?.phase, spinOrderIds, roomState?.winnerEndpointId, spinTickMs, spinCycles) {
        val state = roomState
        if (state != null && state.phase == Phase.RUNNING && 
            spinOrderIds.isNotEmpty() && 
            state.winnerEndpointId != null) {
            
            val winnerIndex = spinOrderIds.indexOfFirst { it == state.winnerEndpointId }
            if (winnerIndex >= 0) {
                val totalSteps = spinOrderIds.size * spinCycles + winnerIndex
                for (step in 0..totalSteps) {
                    highlightIndex.intValue = step % spinOrderIds.size
                    rotationAngle.animateTo(
                        targetValue = rotationAngle.value + 360f / spinOrderIds.size,
                        animationSpec = tween(
                            durationMillis = spinTickMs.toInt(),
                            easing = LinearEasing
                        )
                    )
                    delay(spinTickMs)
                }
            }
        }
    }

    LaunchedEffect(roomState?.phase) {
        val state = roomState
        if (state != null && state.phase == Phase.FINISHED) {
            state.winnerEndpointId?.let { winnerId ->
                val winner = state.members.find { it.endpointId == winnerId }
                winner?.displayName?.let { winnerName ->
                    onGameFinished(winnerName)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.leaveRoom()
        }
    }

    val currentRoomState = roomState
    currentRoomState?.let { state ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            CenterAlignedTopAppBar(
                title = { Text(state.title) },
                actions = {
                    IconButton(onClick = {
                        viewModel.leaveRoom()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "나가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1C1C1E),
                    actionIconContentColor = Color(0xFF1C1C1E)
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "참가자 (${state.members.size}명)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )

                    PhaseIndicator(phase = state.phase)
                }

                Spacer(modifier = Modifier.height(16.dp))

                val orderedMembers = if (spinOrderIds.isNotEmpty()) {
                    spinOrderIds.mapNotNull { id -> 
                        state.members.find { it.endpointId == id }
                    }
                } else {
                    state.members.sortedBy { it.number ?: Int.MAX_VALUE }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orderedMembers) { member ->
                        val isHighlighted = state.phase == Phase.RUNNING && 
                            spinOrderIds.getOrNull(highlightIndex.intValue) == member.endpointId
                        val isWinner = state.phase == Phase.FINISHED && 
                            state.winnerEndpointId == member.endpointId

                        MemberCard(
                            member = member,
                            isHighlighted = isHighlighted,
                            isWinner = isWinner
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                when (state.phase) {
                    Phase.IDLE, Phase.GATHERING -> {
                        if (role == Role.HOST) {
                            HostControls(
                                memberCount = state.members.size,
                                onStartGathering = { viewModel.startGathering() },
                                onAssignNumbers = { viewModel.assignNumbers() },
                                onSendInstruction = { viewModel.sendInstruction() },
                                onStartGame = { viewModel.startGameHost() },
                                hasUnassignedNumbers = state.members.any { it.number == null }
                            )
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF0F9FF)
                                )
                            ) {
                                Text(
                                    text = "호스트가 게임을 시작하길 기다리고 있습니다...",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E40AF),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    
                    Phase.INSTRUCTION -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3CD)
                            )
                        ) {
                            Text(
                                text = "게임 설명이 진행 중입니다...",
                                fontSize = 14.sp,
                                color = Color(0xFF856404),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    
                    Phase.RUNNING -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFDCFCE7)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF059669),
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "🎯 룰렛이 돌아가고 있습니다...",
                                    fontSize = 14.sp,
                                    color = Color(0xFF059669),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Phase.FINISHED -> {
                        val winner = state.members.find { it.endpointId == state.winnerEndpointId }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF8F4FD)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🏆",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "🎉 게임 결과",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B5FBF)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${winner?.displayName ?: "알 수 없음"}님이 당첨되었습니다!",
                                    fontSize = 16.sp,
                                    color = Color(0xFF8B5FBF)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                viewModel.leaveRoom()
                                onNavigateBack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B5FBF)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "정산하러 가기",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (isInstructionVisible) {
            InstructionDialog(
                countdown = instructionCountdown,
                onDismiss = { viewModel.dismissInstruction() }
            )
        }
    }
}

@Composable
private fun MemberCard(
    member: Member,
    isHighlighted: Boolean,
    isWinner: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isHighlighted || isWinner) 8.dp else 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = if (isHighlighted || isWinner) 2.dp else 0.dp,
                color = when {
                    isWinner -> Color(0xFFFFC107)
                    isHighlighted -> Color(0xFF8B5FBF)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isWinner -> Color(0xFFFFF8E1)
                isHighlighted -> Color(0xFFF8F4FD)
                else -> Color.White
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when {
                            isWinner -> Color(0xFFFFC107).copy(alpha = 0.2f)
                            member.isHost -> Color(0xFF8B5FBF).copy(alpha = 0.2f)
                            else -> Color(0xFF6B7280).copy(alpha = 0.2f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (member.number != null) {
                    Text(
                        text = "#${member.number}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isWinner -> Color(0xFFFFC107)
                            member.isHost -> Color(0xFF8B5FBF)
                            else -> Color(0xFF6B7280)
                        }
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = member.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isWinner -> Color(0xFFF59E0B)
                            isHighlighted -> Color(0xFF8B5FBF)
                            else -> Color(0xFF1C1C1E)
                        }
                    )
                    
                    if (member.isHost) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "👑",
                            fontSize = 12.sp
                        )
                    }
                    
                    if (member.isSelf) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(나)",
                            fontSize = 12.sp,
                            color = Color(0xFF8B5FBF)
                        )
                    }
                }

                if (isWinner) {
                    Text(
                        text = "🏆 당첨!",
                        fontSize = 12.sp,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PhaseIndicator(phase: Phase) {
    val (text, color) = when (phase) {
        Phase.IDLE -> "대기 중" to Color(0xFF6B7280)
        Phase.GATHERING -> "모집 중" to Color(0xFF3B82F6)
        Phase.INSTRUCTION -> "설명 중" to Color(0xFFF59E0B)
        Phase.RUNNING -> "진행 중" to Color(0xFF10B981)
        Phase.FINISHED -> "완료" to Color(0xFF8B5A2B)
    }

    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun HostControls(
    memberCount: Int,
    onStartGathering: () -> Unit,
    onAssignNumbers: () -> Unit,
    onSendInstruction: () -> Unit,
    onStartGame: () -> Unit,
    hasUnassignedNumbers: Boolean
) {
    Column {
        if (memberCount < 2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3CD)
                )
            ) {
                Text(
                    text = "⚠️ 게임을 시작하려면 최소 2명이 필요합니다",
                    fontSize = 14.sp,
                    color = Color(0xFF856404),
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasUnassignedNumbers) {
                    Button(
                        onClick = onAssignNumbers,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("번호 배정", fontSize = 14.sp, color = Color.White)
                    }
                }
                
                Button(
                    onClick = onSendInstruction,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("게임 설명", fontSize = 14.sp, color = Color.White)
                }
                
                Button(
                    onClick = onStartGame,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5FBF)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("게임 시작", fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun InstructionDialog(
    countdown: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "👥",
                    fontSize = 36.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "🎯 룰렛 게임 방법",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "곧 룰렛이 돌아갑니다!\n한 명이 당첨되어 전체 정산을 담당하게 됩니다.",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (countdown > 0) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                color = Color(0xFF8B5FBF).copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = countdown.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5FBF)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5FBF)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "확인",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}