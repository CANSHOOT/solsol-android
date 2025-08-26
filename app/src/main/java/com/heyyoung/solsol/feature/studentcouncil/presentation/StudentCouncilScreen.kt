package com.heyyoung.solsol.feature.studentcouncil.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.core.network.DeptHomeSummaryResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCouncilScreen(
    summary: DeptHomeSummaryResponse?,   // ViewModel에서 받아옴
    onNavigateBack: () -> Unit = {},
    onNavigateToExpenseHistory: () -> Unit = {},
    onNavigateToExpenseRegister: () -> Unit = {},
    onNavigateToFeeStatus: () -> Unit = {}

) {
    Log.d("1123","a"+summary.toString())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CenterAlignedTopAppBar(
            title = { Text("학생회") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            }
        )

        summary?.let { ui ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(ui.header.councilName, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(24.dp))

                BalanceInfoCard(
                    currentBalance = ui.balanceCard.currentBalance,
                    monthlyExpense = ui.balanceCard.monthSpendTotal
                )

                Spacer(modifier = Modifier.height(24.dp))

                FeePaymentStatusCard(
                    semester = ui.feeBadge?.semester ?: "이번 학기",
                    isPaid = ui.feeBadge?.paid ?: false
                )

                Spacer(modifier = Modifier.height(24.dp))
                ActionButton("지출내역 전체 보기", Color(0xFFE8E3F3), Color(0xFF8B5FBF), onClick = onNavigateToExpenseHistory)
                Spacer(modifier = Modifier.height(16.dp))
                ActionButton("지출 등록", Color(0xFF8B5FBF), Color.White, Icons.Default.Add, onClick = onNavigateToExpenseRegister)
                Spacer(modifier = Modifier.height(16.dp))
                ActionButton("회비 현황", Color.White, Color.Black, Icons.Default.List, hasBorder = true, onClick = onNavigateToFeeStatus)
            }
        }
    }
}

@Composable
private fun BalanceInfoCard(
    currentBalance: Long,
    monthlyExpense: Long
) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "우리 과 회계 현황",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "현재 잔액: ${String.format("%,d", currentBalance)}원",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "이번 달 지출: ${String.format("%,d", monthlyExpense)}원",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeePaymentStatusCard(
    semester: String,
    isPaid: Boolean
) {
    Card(
        modifier = Modifier
            .shadow(4.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
            .fillMaxWidth()
            .height(60.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) Color(0xFFF0F9FF) else Color(0xFFFFF0F0)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPaid) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "납부완료",
                    tint = Color(0xFF10B981)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$semester 회비 납부 ${if (isPaid) "완료" else "미완료"}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPaid) Color(0xFF10B981) else Color(0xFFEF4444)
            )
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
    Button(
        onClick = onClick,
        modifier = Modifier
            .shadow(if (hasBorder) 2.dp else 8.dp, spotColor = Color(0x26000000), ambientColor = Color(0x26000000))
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        border = if (hasBorder) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            icon?.let {
                Icon(it, contentDescription = null, tint = textColor)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}
