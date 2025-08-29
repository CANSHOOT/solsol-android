package com.heyyoung.solsol.feature.settlement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.heyyoung.solsol.feature.settlement.domain.model.Person
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementGroup
import kotlinx.coroutines.delay

@Composable
fun SettlementCompleteScreen(
    settlementGroup: SettlementGroup?,
    participants: List<Person>,
    totalAmount: Int,
    amountPerPerson: Int,
    onNavigateToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val purple = Color(0xFF8B5FBF)
    val textMain = Color(0xFF2D3748)
    val textSub = Color(0xFF718096)

    // 애니메이션을 위한 상태
    var showCheckmark by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        showCheckmark = true
        delay(300)
        showContent = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // 체크마크 아이콘 - PaymentSuccessScreen 스타일
        if (showCheckmark) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(
                        elevation = 14.dp,
                        spotColor = purple.copy(alpha = 0.18f),
                        ambientColor = purple.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .background(purple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "완료",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (showContent) {
            // 제목
            Text(
                text = "정산 요청 완료!",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textMain,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 부제목
            Text(
                text = "참여자들에게 정산 알림이 발송되었어요",
                fontSize = 17.sp,
                color = textSub,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 정산 정보 카드 - PaymentSuccessScreen 스타일
            SectionCard {
                // 그룹명
                settlementGroup?.let { group ->
                    Text(
                        text = group.groupName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = purple,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 총 금액
                LabeledAmountRow(
                    label = "총 금액",
                    value = "${String.format("%,d", totalAmount)}원",
                    labelColor = textSub,
                    valueColor = textMain,
                    labelSize = 15.sp,
                    valueSize = 18.sp,
                    valueWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 참여 인원
                LabeledAmountRow(
                    label = "참여 인원",
                    value = "${participants.size}명",
                    labelColor = textSub,
                    valueColor = textMain,
                    labelSize = 15.sp,
                    valueSize = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))

                // 1인당 금액 - 강조
                LabeledAmountRow(
                    label = "1인당 금액",
                    value = "${String.format("%,d", amountPerPerson)}원",
                    labelColor = textMain,
                    valueColor = purple,
                    labelWeight = FontWeight.Bold,
                    valueWeight = FontWeight.ExtraBold,
                    labelSize = 17.sp,
                    valueSize = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 참여자 목록 제목
                Text(
                    text = "참여자 목록",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 참여자 목록
                participants.forEach { participant ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 프로필 아이콘
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                purple.copy(alpha = 0.15f),
                                                purple.copy(alpha = 0.08f)
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = participant.name.first().toString(),
                                    color = purple,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = if (participant.isMe) "${participant.name} (나)" else participant.name,
                                fontSize = 16.sp,
                                color = if (participant.isMe) purple else textMain,
                                fontWeight = if (participant.isMe) FontWeight.Bold else FontWeight.Medium
                            )
                        }

                        // 상태 표시 - 고정 너비
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .background(
                                    color = if (participant.isMe) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFFFB366).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (participant.isMe) "총무" else "대기중",
                                fontSize = 12.sp,
                                color = if (participant.isMe) Color(0xFF10B981) else Color(0xFFFFB366),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 홈으로 가기 버튼 - PaymentSuccessScreen 스타일
            Button(
                onClick = onNavigateToHome,
                modifier = Modifier
                    .width(220.dp)
                    .height(54.dp)
                    .shadow(
                        elevation = 14.dp,
                        spotColor = purple.copy(alpha = 0.25f),
                        ambientColor = purple.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(28.dp)
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = purple),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "홈으로 가기",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/* -------------------------- 작은 UI 헬퍼들 -------------------------- */

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .width(342.dp) // 본문 폭 고정으로 시선 흐름 안정화
            .shadow(
                elevation = 10.dp,
                spotColor = Color(0x1A8B5FBF),
                ambientColor = Color(0x1A8B5FBF),
                shape = RoundedCornerShape(16.dp)
            )
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(20.dp),
        content = content
    )
}

@Composable
private fun LabeledAmountRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    labelWeight: FontWeight = FontWeight.Medium,
    valueWeight: FontWeight = FontWeight.Medium,
    labelSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    valueSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = labelSize,
            color = labelColor,
            fontWeight = labelWeight
        )
        Text(
            text = value,
            fontSize = valueSize,
            color = valueColor,
            fontWeight = valueWeight,
            textAlign = TextAlign.Right
        )
    }
}