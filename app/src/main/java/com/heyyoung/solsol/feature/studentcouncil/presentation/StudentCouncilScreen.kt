package com.heyyoung.solsol.feature.studentcouncil.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.heyyoung.solsol.core.network.DeptHomeSummaryResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCouncilScreen(
    summary: DeptHomeSummaryResponse?,   // ViewModel에서 받아옴
    onNavigateBack: () -> Unit = {},
    onNavigateToExpenseHistory: () -> Unit = {},
    onNavigateToExpenseRegister: () -> Unit = {},
    onNavigateToFeeStatus: () -> Unit = {},
    onNavigateToPayment: () -> Unit = {}

) {
    val purple = Color(0xFF8B5FBF)
    val textMain = Color(0xFF2D3748)
    val textSub = Color(0xFF718096)

    Log.d("1123","a"+summary.toString())

    // 하드웨어/제스처 뒤로가기 버튼 처리
    BackHandler {
        onNavigateBack()
    }

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
        // 상단 앱바 - 모던 스타일
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "학생회",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textMain
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFFF7FAFC),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "뒤로",
                        tint = textMain,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )

        summary?.let { ui ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 학생회 이름
                Text(
                    ui.header.councilName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSub
                )

                Spacer(modifier = Modifier.height(32.dp))

                BalanceInfoCard(
                    currentBalance = ui.balanceCard.currentBalance,
                    monthlyExpense = ui.balanceCard.monthSpendTotal
                )

                Spacer(modifier = Modifier.height(20.dp))

                FeePaymentStatusCard(
                    semester = ui.feeBadge?.semester ?: "이번 학기",
                    isPaid = ui.feeBadge?.paid ?: false,
                    onNavigateToPayment = onNavigateToPayment
                )

                Spacer(modifier = Modifier.height(32.dp))

                ActionButton(
                    "지출내역 전체 보기",
                    Color(0xFFF8F6FF),
                    purple,
                    onClick = onNavigateToExpenseHistory
                )
                Spacer(modifier = Modifier.height(16.dp))

                ActionButton(
                    "지출 등록",
                    purple,
                    Color.White,
                    Icons.Default.Add,
                    onClick = onNavigateToExpenseRegister
                )
                Spacer(modifier = Modifier.height(16.dp))

                ActionButton(
                    "회비 현황",
                    Color.White,
                    textMain,
                    Icons.Default.List,
                    hasBorder = true,
                    onClick = onNavigateToFeeStatus
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun BalanceInfoCard(
    currentBalance: Long,
    monthlyExpense: Long
) {
    val purple = Color(0xFF8B5FBF)
    val textMain = Color(0xFF2D3748)
    val textSub = Color(0xFF718096)

    Card(
        modifier = Modifier
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x1A8B5FBF),
                ambientColor = Color(0x1A8B5FBF),
                shape = RoundedCornerShape(20.dp)
            )
            .fillMaxWidth()
            // ✅ 세로 여유: 고정 높이 대신 최소 높이 + 넉넉한 패딩
            .heightIn(min = 176.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // ✅ 상하 패딩 확대
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // ✅ 요소 간 간격 살짝 키움
        ) {
            Text(
                text = "우리 과 회계 현황",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = purple
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "현재 잔액",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSub
                )
                Text(
                    text = "${String.format("%,d", currentBalance)}원",
                    fontSize = 34.sp,  // 살짝 키워도 공간 충분
                    fontWeight = FontWeight.ExtraBold,
                    color = textMain
                )
                Text(
                    text = "이번 달 지출: ${String.format("%,d", monthlyExpense)}원",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSub
                )
            }
        }
    }
}


@Composable
private fun FeePaymentStatusCard(
    semester: String,
    isPaid: Boolean,
    onNavigateToPayment: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x1A8B5FBF),
                ambientColor = Color(0x1A8B5FBF),
                shape = RoundedCornerShape(16.dp)
            )
            .fillMaxWidth()
            .heightIn(min = if (isPaid) 72.dp else 96.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 아이콘 배경
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isPaid) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPaid) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "납부완료",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "$semester 회비",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF718096)
                    )
                    Text(
                        text = if (isPaid) "납부 완료" else "납부 미완료",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }

            // 납부 미완료일 때만 버튼 표시
            if (!isPaid) {
                Button(
                    onClick = onNavigateToPayment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5FBF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "납부하러 가기",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    hasBorder: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .shadow(
                elevation = if (hasBorder) 4.dp else 12.dp,
                spotColor = Color(0x2A8B5FBF),
                ambientColor = Color(0x1A8B5FBF),
                shape = RoundedCornerShape(20.dp)
            )
            .fillMaxWidth()
            .height(64.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp),
        border = if (hasBorder) CardDefaults.outlinedCardBorder() else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                icon?.let {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = textColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            it,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}