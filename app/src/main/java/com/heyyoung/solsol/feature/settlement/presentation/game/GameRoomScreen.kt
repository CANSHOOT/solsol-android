package com.heyyoung.solsol.feature.settlement.presentation.game

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyyoung.solsol.feature.settlement.domain.game.*
import com.heyyoung.solsol.feature.settlement.domain.model.Person
import com.heyyoung.solsol.feature.settlement.presentation.SettlementEqualViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRoomScreen(
    onNavigateBack: () -> Unit = {},
    onGameFinished: () -> Unit = {},
    onNavigateRemittance: (Long) -> Unit = {}, // groupId 전달
    viewModel: GameViewModel = viewModel(),
    settlementViewModel: SettlementEqualViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val roomState by viewModel.roomState.collectAsState()
    val role by viewModel.role.collectAsState()
    val isInstructionVisible by viewModel.isInstructionVisible.collectAsState()
    val instructionCountdown by viewModel.instructionCountdown.collectAsState()
    val spinOrderIds by viewModel.spinOrderIds.collectAsState() // userId 리스트
    val currentHighlightIndex by viewModel.currentHighlightIndex.collectAsState()

    val uiState by settlementViewModel.uiState.collectAsState()

    // 정산 완료되면 Remittance 화면으로 이동
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            uiState.createdSettlement?.groupId?.let { groupId ->
                settlementViewModel.onSettlementCompleteNavigated()
                onNavigateRemittance(groupId)
            }
        }
    }

    // 화면 떠날 때 연결 정리
    DisposableEffect(Unit) {
        onDispose { viewModel.leaveRoom() }
    }

    val state = roomState ?: return
    val me = remember(state) { state.members.firstOrNull { it.isSelf } }
    val amountText = remember(state.settlementAmount) { state.settlementAmount?.let { "%,d".format(it) } ?: "0" }

    // 당첨자/하이라이트 판정은 userId 기준으로 전역에서 계산 (스코프 오류 방지)
    val myUserId = me?.userId
    val highlightedUserId = spinOrderIds.getOrNull(currentHighlightIndex)
    val isRunningLightOn = myUserId != null && highlightedUserId != null && myUserId == highlightedUserId
    val winnerMember = state.members.find { it.userId == state.winnerUserId }
    val amWinner = state.phase == Phase.FINISHED && myUserId != null && myUserId == state.winnerUserId

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
                }) { Icon(Icons.Default.Close, contentDescription = "나가기") }
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
                Column {
                    Text(
                        "정산 금액: ${amountText}원",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = me?.displayName ?: "나",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = "참가자 ${state.members.size}명 중",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
                PhaseIndicator(phase = state.phase)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 중앙 불빛 표시 (userId 기준)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val isLightOn = when (state.phase) {
                    Phase.RUNNING -> isRunningLightOn
                    Phase.FINISHED -> amWinner
                    else -> false
                }

                CenterLightDisplay(
                    isLightOn = isLightOn,
                    isFinished = state.phase == Phase.FINISHED,
                    isWinner = amWinner
                )
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
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
                    Log.d("GameDebug", "=== 전체 멤버 리스트 ===")
                    state.members.forEachIndexed { index, m ->
                        Log.d("GameDebug", "멤버[$index]: endpointId=${m.endpointId}, displayName=${m.displayName}, userId=${m.userId}, isSelf=${m.isSelf}, isHost=${m.isHost}")
                    }
                    Log.d("GameDebug", "winnerUserId: ${state.winnerUserId}")
                    Log.d("GameDebug", "========================")

                    val iAmHost = me?.isHost == true

                    if (amWinner && !iAmHost) {
                        // 당첨자(호스트 아님)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(12.dp, RoundedCornerShape(20.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "🎊", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "축하합니다!",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "당신이 당첨되었습니다!\n전체 정산을 담당해주세요.",
                                    fontSize = 16.sp,
                                    color = Color(0xFFF59E0B),
                                    lineHeight = 24.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val hostMember = state.members.find { it.isHost }
                                val hostId = hostMember?.userId ?: "1"

                                winnerMember?.let { winner ->
                                    val participants = listOf(
                                        Person(
                                            id = winner.userId,
                                            name = winner.displayName,
                                            isMe = winner.isSelf,
                                            amount = BigDecimal.valueOf(state.settlementAmount?.toDouble() ?: 0.0),
                                            department = "학생회",
                                            studentId = ""
                                        )
                                    )

                                    settlementViewModel.createSettlementGame(
                                        organizerId = hostId,
                                        groupName = state.title,
                                        totalAmount = state.settlementAmount?.toDouble() ?: 0.0,
                                        participants = participants
                                    ) { groupId ->
                                        if (groupId != null) {
                                            Log.d("UI", "생성된 그룹 ID: $groupId")
                                            onNavigateRemittance(groupId)
                                        }
                                    }
                                }
                                viewModel.leaveRoom()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(28.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                text = "정산하러 가기",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else if (amWinner && iAmHost) {
                        // 당첨자(호스트 본인)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(12.dp, RoundedCornerShape(20.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "🎊", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "축하합니다!",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "당첨되었습니다. 전체 정산을 진행해주세요.",
                                    fontSize = 16.sp,
                                    color = Color(0xFFF59E0B),
                                    lineHeight = 24.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.leaveRoom()
                                onGameFinished()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(28.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                text = "닫기",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // 일반 참가자
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "🎯", fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "${winnerMember?.displayName ?: "알 수 없음"}님이 당첨되었습니다!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "정산을 진행할 예정입니다",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E40AF)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.leaveRoom()
                                onGameFinished()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "메인으로 돌아가기",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
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
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) { Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color) }
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text("번호 배정", fontSize = 14.sp, color = Color.White) }
                } else {
                    Button(
                        onClick = onStartGame,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5FBF)),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text("게임 시작", fontSize = 14.sp, color = Color.White) }
                }
            }
        }
    }
}

@Composable
private fun CenterLightDisplay(
    isLightOn: Boolean,
    isFinished: Boolean,
    isWinner: Boolean
) {
    Box(
        modifier = Modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // 배경 원
        Box(
            modifier = Modifier
                .size(250.dp)
                .background(Color(0xFF6B7280).copy(alpha = 0.1f), CircleShape)
                .shadow(4.dp, CircleShape)
        )

        // 불빛
        if (isLightOn || isFinished) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        when {
                            isFinished && isWinner -> Color(0xFFFFC107).copy(alpha = 0.9f)
                            isLightOn -> Color(0xFF8B5FBF).copy(alpha = 0.8f)
                            else -> Color(0xFF6B7280).copy(alpha = 0.2f)
                        },
                        CircleShape
                    )
                    .shadow(if (isLightOn || (isFinished && isWinner)) 20.dp else 8.dp, CircleShape)
            )
        }

        // 중앙 아이콘/텍스트
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                isFinished && isWinner -> {
                    Text("🏆", fontSize = 48.sp); Spacer(Modifier.height(12.dp))
                    Text("당첨!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                }
                isFinished && !isWinner -> Text("⚪", fontSize = 48.sp, color = Color(0xFF999999))
                isLightOn -> Text("💡", fontSize = 48.sp)
                else -> Text("⚪", fontSize = 48.sp, color = Color(0xFF999999))
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("👥", fontSize = 36.sp)
                Spacer(Modifier.height(16.dp))
                Text("🎯 룰렛 게임 방법", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1E))
                Spacer(Modifier.height(16.dp))
                Text(
                    "곧 룰렛이 돌아갑니다!\n한 명이 당첨되어 전체 정산을 담당하게 됩니다.",
                    fontSize = 16.sp, color = Color(0xFF666666), lineHeight = 24.sp
                )
                Spacer(Modifier.height(24.dp))
                if (countdown > 0) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFF8B5FBF).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(countdown.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5FBF))
                    }
                    Spacer(Modifier.height(16.dp))
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5FBF)),
                    shape = RoundedCornerShape(24.dp)
                ) { Text("확인", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            }
        }
    }
}
