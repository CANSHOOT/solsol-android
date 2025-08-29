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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFAFA),
                        Color.White
                    )
                )
            )
    ) {
        // 상단 앱바 - 더 트렌디하게
        CenterAlignedTopAppBar(
            title = {
                Text(
                    state.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748) // solsol_dark_text
                )
            },
            actions = {
                IconButton(
                    onClick = {
                        viewModel.leaveRoom()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFFFF6B6B).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "나가기",
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 게임 정보 카드 - 더 예쁘게
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        spotColor = Color(0x1A8B5FBF),
                        ambientColor = Color(0x1A8B5FBF)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFFFFF)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "정산 금액",
                            fontSize = 14.sp,
                            color = Color(0xFF718096), // solsol_gray_text
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${amountText}원",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF8B5FBF) // solsol_purple
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = Color(0xFF2196F3).copy(alpha = 0.1f), // 하늘색 배경
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (me?.displayName ?: "나").first().toString(),
                                    color = Color(0xFF2196F3), // 하늘색
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = me?.displayName ?: "나",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748) // solsol_dark_text
                            )
                        }
                        Text(
                            text = "참가자 ${state.members.size}명 중",
                            fontSize = 13.sp,
                            color = Color(0xFF718096) // solsol_gray_text
                        )
                    }

                    PhaseIndicator(phase = state.phase)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 중앙 불빛 표시 - 더 화려하게
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

            Spacer(modifier = Modifier.height(24.dp))

            // 상태별 컨트롤 - 더 예쁘게
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
                        StatusCard(
                            backgroundColor = Color(0xFF8B5FBF).copy(alpha = 0.1f),
                            textColor = Color(0xFF8B5FBF),
                            text = "호스트가 게임을 시작하길 기다리고 있습니다..."
                        )
                    }
                }

                Phase.INSTRUCTION -> {
                    StatusCard(
                        backgroundColor = Color(0xFFFFA500).copy(alpha = 0.1f),
                        textColor = Color(0xFFFFA500),
                        text = "게임 설명이 진행 중입니다..."
                    )
                }

                Phase.RUNNING -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                spotColor = Color(0x1A10B981),
                                ambientColor = Color(0x1A10B981)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "룰렛이 돌아가고 있습니다...",
                                fontSize = 16.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
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
                        // 당첨자(호스트 아님) - 더 화려하게
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 20.dp,
                                    spotColor = Color(0x30FFC107),
                                    ambientColor = Color(0x30FFC107)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFE0B2)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            Color(0xFFFFC107).copy(alpha = 0.2f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "👑",
                                        fontSize = 36.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "축하합니다!",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFF57C00)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "당신이 당첨되었습니다!\n전체 정산을 담당해주세요.",
                                    fontSize = 16.sp,
                                    color = Color(0xFFF57C00),
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

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
                                .shadow(
                                    elevation = 16.dp,
                                    spotColor = Color(0x40FFC107),
                                    ambientColor = Color(0x40FFC107)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC107)
                            ),
                            shape = RoundedCornerShape(16.dp)
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
                                .shadow(
                                    elevation = 20.dp,
                                    spotColor = Color(0x30FFC107),
                                    ambientColor = Color(0x30FFC107)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFE0B2)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            Color(0xFFFFC107).copy(alpha = 0.2f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "👑",
                                        fontSize = 36.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "축하합니다!",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFF57C00)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "당첨되었습니다. 전체 정산을 진행해주세요.",
                                    fontSize = 16.sp,
                                    color = Color(0xFFF57C00),
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.leaveRoom()
                                onGameFinished()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(
                                    elevation = 16.dp,
                                    spotColor = Color(0x40FFC107),
                                    ambientColor = Color(0x40FFC107)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC107)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "닫기",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // 일반 참가자 - 더 깔끔하게
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 12.dp,
                                    spotColor = Color(0x1A2196F3),
                                    ambientColor = Color(0x1A2196F3)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE3F2FD)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(
                                            Color(0xFF2196F3).copy(alpha = 0.1f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🎯",
                                        fontSize = 28.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "${winnerMember?.displayName ?: "알 수 없음"}님이 당첨되었습니다!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "정산을 진행할 예정입니다",
                                    fontSize = 14.sp,
                                    color = Color(0xFF2196F3),
                                    fontWeight = FontWeight.Medium
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
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF718096) // solsol_gray_text
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "메인으로 돌아가기",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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
private fun StatusCard(
    backgroundColor: Color,
    textColor: Color,
    text: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                spotColor = textColor.copy(alpha = 0.2f),
                ambientColor = textColor.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(20.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PhaseIndicator(phase: Phase) {
    val (text, backgroundColor, textColor) = when (phase) {
        Phase.IDLE -> Triple("대기 중", Color(0xFF718096).copy(alpha = 0.1f), Color(0xFF718096))
        Phase.GATHERING -> Triple("모집 중", Color(0xFF2196F3).copy(alpha = 0.1f), Color(0xFF2196F3))
        Phase.INSTRUCTION -> Triple("설명 중", Color(0xFFFFA500).copy(alpha = 0.1f), Color(0xFFFFA500))
        Phase.RUNNING -> Triple("진행 중", Color(0xFF10B981).copy(alpha = 0.1f), Color(0xFF10B981))
        Phase.FINISHED -> Triple("완료", Color(0xFFFFC107).copy(alpha = 0.1f), Color(0xFFF57C00))
    }

    Surface(
        modifier = Modifier.shadow(
            elevation = 4.dp,
            spotColor = textColor.copy(alpha = 0.2f),
            ambientColor = textColor.copy(alpha = 0.2f)
        ),
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        contentColor = textColor
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
    if (memberCount < 2) {
        StatusCard(
            backgroundColor = Color(0xFFFFA500).copy(alpha = 0.1f),
            textColor = Color(0xFFFFA500),
            text = "게임을 시작하려면 최소 2명이 필요합니다"
        )
    } else {
        if (hasUnassignedNumbers) {
            Button(
                onClick = onAssignNumbers,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 12.dp,
                        spotColor = Color(0x402196F3),
                        ambientColor = Color(0x402196F3)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "번호 배정",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } else {
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 12.dp,
                        spotColor = Color(0x408B5FBF),
                        ambientColor = Color(0x408B5FBF)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5FBF)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "게임 시작",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // 외곽 링
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF718096).copy(alpha = 0.05f),
                            Color(0xFF718096).copy(alpha = 0.15f)
                        )
                    ),
                    shape = CircleShape
                )
                .shadow(8.dp, CircleShape)
        )

        // 메인 불빛 효과
        when {
            isFinished && isWinner -> {
                // 당첨 효과
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFC107).copy(alpha = 0.8f),
                                    Color(0xFFFFC107).copy(alpha = 0.3f),
                                    Color(0xFFFFC107).copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .shadow(32.dp, CircleShape)
                )
            }
            isLightOn -> {
                // 하이라이트 효과
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF8B5FBF).copy(alpha = 0.7f),
                                    Color(0xFF8B5FBF).copy(alpha = 0.3f),
                                    Color(0xFF8B5FBF).copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .shadow(24.dp, CircleShape)
                )
            }
        }

        // 중앙 컨텐츠
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Color.White,
                    shape = CircleShape
                )
                .shadow(16.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when {
                isFinished && isWinner -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏆",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "당첨!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57C00)
                        )
                    }
                }
                isFinished && !isWinner -> {
                    Text(
                        text = "완료",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF718096)
                    )
                }
                isLightOn -> {
                    Text(
                        text = "💫",
                        fontSize = 32.sp
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFF718096).copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    )
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
                .shadow(
                    elevation = 24.dp,
                    spotColor = Color(0x308B5FBF),
                    ambientColor = Color(0x308B5FBF)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Color(0xFF8B5FBF).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎯",
                        fontSize = 36.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "룰렛 게임 방법",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2D3748)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "곧 룰렛이 돌아갑니다!\n한 명이 당첨되어 전체 정산을 담당하게 됩니다.",
                    fontSize = 16.sp,
                    color = Color(0xFF718096),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                if (countdown > 0) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Color(0xFF8B5FBF).copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                            .shadow(8.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            countdown.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF8B5FBF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5FBF)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "확인",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}