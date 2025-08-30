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
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.heyyoung.solsol.R
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale
import com.heyyoung.solsol.ui.theme.OneShinhan

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
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = Color(0xFFF8F7FF).copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (me?.displayName ?: "나").first().toString(),
                                    color = Color(0xFF8B5FBF),
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

                    val hostMember = state.members.find { it.isHost }
                    val iAmHost = me?.userId != null && hostMember?.userId == me?.userId

                    if (amWinner && !iAmHost) {
                        // 당첨자 (호스트 아님) - 글래스모피즘 스타일
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 32.dp,
                                    shape = RoundedCornerShape(28.dp),
                                    spotColor = Color(0xFFFF6B35).copy(alpha = 0.3f)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.95f)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Box {
                                // 글래스모피즘 배경
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFFFF6B35).copy(alpha = 0.1f),
                                                    Color(0xFFFF8E53).copy(alpha = 0.05f),
                                                    Color.White.copy(alpha = 0.8f)
                                                ),
                                                radius = 800f
                                            ),
                                            shape = RoundedCornerShape(28.dp)
                                        )
                                )

                                Column(
                                    modifier = Modifier.padding(36.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 펄싱 효과가 있는 트로피
                                    val pulseScale by animateFloatAsState(
                                        targetValue = 1.15f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1200, easing = EaseInOutSine),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "trophy"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .scale(pulseScale)
                                            .shadow(
                                                elevation = 20.dp,
                                                shape = RoundedCornerShape(60.dp),
                                                spotColor = Color(0xFFFF6B35).copy(alpha = 0.4f)
                                            )
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        Color(0xFFFF6B35).copy(alpha = 0.2f),
                                                        Color(0xFFFF8E53).copy(alpha = 0.1f),
                                                        Color.Transparent
                                                    )
                                                ),
                                                shape = RoundedCornerShape(60.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "🏆", fontSize = 40.sp)
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        text = "당첨!",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = OneShinhan,
                                        color = Color(0xFFFF6B35)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "축하합니다! 당신이 선택되었습니다",
                                        fontSize = 16.sp,
                                        color = Color(0xFFFF8E53),
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        fontFamily = OneShinhan,
                                        lineHeight = 24.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "오늘 모두에게 쏘세요",
                                        fontSize = 14.sp,
                                        color = Color(0xFF475569),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 글래스모피즘 정산 버튼
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
                                .height(64.dp)
                                .shadow(
                                    elevation = 20.dp,
                                    shape = RoundedCornerShape(32.dp),
                                    spotColor = Color(0xFFFF6B35).copy(alpha = 0.4f)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFFF6B35),
                                                Color(0xFFFF8E53),
                                                Color(0xFFFFA726)
                                            )
                                        ),
                                        shape = RoundedCornerShape(32.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "정산하러 가기",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = OneShinhan,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else if (amWinner && iAmHost) {
                        // 당첨자 (호스트) - 간소화된 버전
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 20.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    spotColor = Color(0xFFFF6B35).copy(alpha = 0.2f)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.9f)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFFFF6B35).copy(alpha = 0.08f),
                                                    Color(0xFFFFA726).copy(alpha = 0.04f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                )

                                Column(
                                    modifier = Modifier.padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "🎉", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "당첨되었습니다!",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF6B35)
                                    )
                                    Text(
                                        text = "전체 정산을 진행해주세요",
                                        fontSize = 16.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
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
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(28.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF6B35)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                text = "닫기",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    } else {
                        // 일반 참가자 - 모던한 스타일
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 16.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    spotColor = Color(0xFF6366F1).copy(alpha = 0.15f)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.95f)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF6366F1).copy(alpha = 0.08f),
                                                    Color(0xFF8B5CF6).copy(alpha = 0.04f),
                                                    Color.White.copy(alpha = 0.9f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .shadow(
                                                elevation = 12.dp,
                                                shape = RoundedCornerShape(40.dp),
                                                spotColor = Color(0xFF6366F1).copy(alpha = 0.2f)
                                            )
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        Color(0xFF6366F1).copy(alpha = 0.1f),
                                                        Color(0xFF8B5CF6).copy(alpha = 0.05f)
                                                    )
                                                ),
                                                shape = RoundedCornerShape(40.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "\uD83E\uDD73",
                                            fontSize = 36.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Text(
                                        text = "${winnerMember?.displayName ?: "알 수 없음"}님이 당첨!",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = OneShinhan,
                                        color = Color(0xFF6366F1),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "운이 아주 좋으십니다 !",
                                        fontSize = 14.sp,
                                        color = Color(0xFF8B5CF6),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
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
                                .height(52.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(26.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF64748B)
                            ),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Text(
                                text = "메인으로 돌아가기",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = OneShinhan,
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
        // 글래스모피즘 경고 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color(0xFFFFA500).copy(alpha = 0.2f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box {
                // 글래스모피즘 배경
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFA500).copy(alpha = 0.08f),
                                    Color(0xFFFFB347).copy(alpha = 0.04f),
                                    Color.White.copy(alpha = 0.9f)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 아이콘 영역
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = Color(0xFFFFA500).copy(alpha = 0.3f)
                            )
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFA500).copy(alpha = 0.15f),
                                        Color(0xFFFFA500).copy(alpha = 0.08f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "게임을 시작하려면 최소 2명이 필요합니다",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFA500),
                        lineHeight = 22.sp
                    )
                }
            }
        }
    } else {
        if (hasUnassignedNumbers) {
            // 번호 배정 버튼 - 글래스모피즘 스타일
            Button(
                onClick = onAssignNumbers,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2196F3),
                                    Color(0xFF1976D2),
                                    Color(0xFF1565C0)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "번호 배정",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = OneShinhan,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            // 게임 시작 버튼 - 글래스모피즘 스타일
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = Color(0xFF8B5FBF).copy(alpha = 0.1f)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF8B5FBF),
                                    Color(0xFFB794F6),
                                    Color(0xFFF093FB)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "게임 시작",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = OneShinhan,
                            color = Color.White
                        )
                    }
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
    // 깜빡임 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophy"
    )
    val blinkScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 300,
                easing = EaseInOutCubic
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = if (isFinished && isWinner) R.drawable.a2 else R.drawable.a1
            ),
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .alpha(
                    if (isLightOn && !isFinished) pulseScale else 1f
                )
                .scale(
                    if (isLightOn && !isFinished) blinkScale else 1f
                ),
            contentScale = ContentScale.Fit
        )
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
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "룰렛 게임 방법",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = OneShinhan,
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
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = OneShinhan,
                        color = Color.White
                    )
                }
            }
        }
    }
}