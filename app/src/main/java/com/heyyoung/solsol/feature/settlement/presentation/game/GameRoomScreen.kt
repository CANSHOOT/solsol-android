package com.heyyoung.solsol.feature.settlement.presentation.game

import android.util.Log
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
import com.heyyoung.solsol.feature.settlement.domain.model.Person
import com.heyyoung.solsol.feature.settlement.presentation.SettlementEqualViewModel
import kotlinx.coroutines.delay
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRoomScreen(
    onNavigateBack: () -> Unit = {},
    onGameFinished: (String) -> Unit = {},
    onNavigateRemittance: (Long) -> Unit = {}, // groupId 전달
    viewModel: GameViewModel = viewModel(),
    settlementViewModel: SettlementEqualViewModel = androidx.hilt.navigation.compose.hiltViewModel()
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
    val finalWinnerState = remember { mutableStateOf<String?>(null) }

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

    LaunchedEffect(roomState?.phase, spinOrderIds, roomState?.winnerEndpointId, spinTickMs) {
        val state = roomState
        if (state != null && state.phase == Phase.RUNNING && 
            spinOrderIds.isNotEmpty() && 
            state.winnerEndpointId != null &&
            finalWinnerState.value == null) { // 이미 완료된 게임이 아닌 경우에만 실행

            val myEndpointId = "self" // 내 endpointId
            val totalMembers = spinOrderIds.size
            val myIndex = spinOrderIds.indexOf(myEndpointId)
            val winnerIndex = spinOrderIds.indexOf(state.winnerEndpointId)

            // 전체 애니메이션: 빠르게 시작 -> 점점 느려짐 -> 당첨자에서 멈춤
            val totalSteps = 80 // 총 80단계

            for (step in 0 until totalSteps) {
                // 순서대로 돌아가며 불빛 표시
                val currentMemberIndex = step % totalMembers
                val shouldLightUp = currentMemberIndex == myIndex

                highlightIndex.intValue = if (shouldLightUp) 1 else 0

                // 시간이 지날수록 점점 느려짐 (exponential easing)
                val progress = step.toFloat() / totalSteps
                val baseDelay = 80L // 시작 속도 (빠름)
                val maxDelay = 800L // 끝 속도 (느림)

                // 지수적으로 느려짐
                val currentDelay = (baseDelay + (maxDelay - baseDelay) * progress * progress * progress).toLong()

                delay(currentDelay)

                // 마지막 몇 단계에서 당첨자에게 멈춰야 함
                if (step > totalSteps - 10) {
                    if (currentMemberIndex == winnerIndex) {
                        // 당첨자에게 도달하면 애니메이션 종료
                        highlightIndex.intValue = if (myIndex == winnerIndex) 1 else 0
                        finalWinnerState.value = state.winnerEndpointId
                        break
                    }
                }
            }

            // 애니메이션 완료 후 Phase.FINISHED로 전환 (호스트만)
            if (role == Role.HOST) {
                delay(1000) // 1초 대기
                viewModel.finishGame()
            }
        }
    }

    // 자동 전환 제거 - 사용자가 버튼을 눌러야만 이동하도록 변경

    DisposableEffect(Unit) {
        onDispose {
            viewModel.leaveRoom()
            finalWinnerState.value = null // 상태 초기화
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

                // 현재 사용자 정보 표시
                val currentUser = state.members.find { it.isSelf }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "정산 금액: ${"%,d".format(state.settlementAmount)}원",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )

                        Text(
                            text = currentUser?.displayName ?: "나",
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

                // 중앙 불빛만 표시
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // 현재 사용자가 당첨자인지 확인 - 이름으로 매칭
                    val currentUser = state.members.find { it.isSelf }
                    val winner = state.members.find { it.endpointId == state.winnerEndpointId }
                    val isWinner = state.phase == Phase.FINISHED && currentUser != null &&
                                 winner != null && currentUser.displayName == winner.displayName

                    val isLightOn = when {
                        state.phase == Phase.FINISHED -> {
                            // 게임 종료 시: 이름으로 매칭 확인
                            val finalWinner = finalWinnerState.value ?: state.winnerEndpointId
                            val winnerMember = state.members.find { it.endpointId == finalWinner }
                            currentUser != null && winnerMember != null &&
                            currentUser.displayName == winnerMember.displayName
                        }
                        state.phase == Phase.RUNNING -> highlightIndex.intValue == 1 // 게임 중 애니메이션
                        else -> false // 대기 상태
                    }

                    CenterLightDisplay(
                        isLightOn = isLightOn,
                        isFinished = state.phase == Phase.FINISHED,
                        isWinner = isWinner
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
                        // 현재 사용자가 당첨자인지 확인 - 이름으로 매칭
                        val currentUser = state.members.find { it.isSelf }
                        val winner = state.members.find { it.endpointId == state.winnerEndpointId }
                        val isWinner = currentUser != null && winner != null &&
                                     currentUser.userId == winner.userId

                        val tets = currentUser.toString()
                        val tets2 = winner.toString()
                        Log.d("test", "현재유저: $tets");
                        Log.d("test", "현재유저: $tets2");
                        Log.d("test", "결과(내가 당첨?): $isWinner");

                        if (isWinner && !(currentUser?.isHost == true)) {
                            // 당첨자용 UI
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF8E1)
                                )
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
                                    // ✅ 방장 ID 가져오기
                                    val hostMember = state.members.find { it.isHost }
                                    val hostId = hostMember?.userId ?: "1"
                                    val test = hostMember.toString()
                                    Log.d("GameRoomScreen", "호스트: $test")

                                    // 🎯 당첨자만 정산 그룹에 추가
                                    val winner = state.members.find { it.endpointId == state.winnerEndpointId }
                                    val test2 = winner.toString()
                                    Log.d("GameRoomScreen", "위너: $test2")

                                    winner?.let { winnerMember ->
                                        val participants = listOf(
                                            Person(
                                                id = winnerMember.userId,
                                                name = winnerMember.displayName,
                                                isMe = winnerMember.isSelf,
                                                amount = BigDecimal.valueOf(state.settlementAmount?.toDouble() ?: 0.0),
                                                department = "학생회",
                                                studentId = ""
                                            )
                                        )

                                        settlementViewModel.createSettlement(
                                            organizerId = hostId ?: "에러",
                                            groupName = state.title,
                                            totalAmount = state.settlementAmount?.toDouble() ?: 0.0,
                                            participants = participants
                                        )
                                    }
                                    viewModel.leaveRoom()
                                    onNavigateBack()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .shadow(8.dp, RoundedCornerShape(28.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFC107)
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text(
                                    text = "정산하러 가기",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        else if (isWinner && currentUser?.isHost == true) {
                            // 당첨자용 UI
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF8E1)
                                )
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
                                    viewModel.leaveRoom()
                                    onNavigateBack()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .shadow(8.dp, RoundedCornerShape(28.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFC107)
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text(
                                    text = "닫기",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        else {
                            // 일반 참가자용 UI - 당첨자 이름 표시
                            val winner = state.members.find { it.endpointId == state.winnerEndpointId }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF0F9FF)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "🎯", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "${winner?.displayName ?: "알 수 없음"}님이 당첨되었습니다!",
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
                                    onNavigateBack()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF6B7280)
                                ),
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
private fun CenterLightDisplay(
    isLightOn: Boolean,
    isFinished: Boolean,
    isWinner: Boolean
) {
    Box(
        modifier = Modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // 배경 원 (항상 표시)
        Box(
            modifier = Modifier
                .size(250.dp)
                .background(
                    color = Color(0xFF6B7280).copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape
                )
        )

        // 불빛 원 (조건부 표시)
        if (isLightOn || isFinished) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        color = when {
                            isFinished && isWinner -> Color(0xFFFFC107).copy(alpha = 0.9f)
                            isLightOn -> Color(0xFF8B5FBF).copy(alpha = 0.8f)
                            else -> Color(0xFF6B7280).copy(alpha = 0.2f)
                        },
                        shape = CircleShape
                    )
                    .shadow(
                        elevation = if (isLightOn || (isFinished && isWinner)) 20.dp else 8.dp,
                        shape = CircleShape
                    )
            )
        }

        // 중앙 아이콘/텍스트
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                isFinished && isWinner -> {
                    Text(text = "🏆", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "당첨!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
                isFinished && !isWinner -> {
                    Text(text = "⚪", fontSize = 48.sp, color = Color(0xFF999999))
                }
                isLightOn -> {
                    Text(text = "💡", fontSize = 48.sp)
                }
                else -> {
                    Text(text = "⚪", fontSize = 48.sp, color = Color(0xFF999999))
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