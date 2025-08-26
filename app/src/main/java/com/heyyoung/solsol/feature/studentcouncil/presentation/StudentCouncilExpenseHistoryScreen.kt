package com.heyyoung.solsol.feature.studentcouncil.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ExpenseHistoryScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCouncilExpenseHistoryScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    expenseList: List<ExpenseItem> = emptyList()
) {
    Log.d(TAG, "지출 내역 화면 진입 - 총 ${expenseList.size}개 항목")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("지출 내역") },
            navigationIcon = {
                IconButton(onClick = {
                    Log.d(TAG, "뒤로가기 클릭")
                    onNavigateBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            },
            actions = {
                IconButton(onClick = {
                    Log.d(TAG, "지출 등록 버튼 클릭")
                    onNavigateToRegister()
                }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "지출 등록",
                        tint = Color(0xFF8B5FBF)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1C1C1E),
                navigationIconContentColor = Color(0xFF1C1C1E)
            )
        )

        // 이번 달 지출 요약 카드
        ExpenseSummaryCard(expenseList = expenseList)

        Spacer(modifier = Modifier.height(16.dp))

        // 지출 내역 리스트
        if (expenseList.isEmpty()) {
            // 빈 상태
            EmptyExpenseState(onAddExpense = onNavigateToRegister)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(expenseList) { expense ->
                    ExpenseItemCard(expense = expense)
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ExpenseSummaryCard(expenseList: List<ExpenseItem>) {
    val currentMonth = SimpleDateFormat("yyyy.MM", Locale.KOREA).format(Date())
    val monthlyTotal = expenseList
        .filter { it.date.startsWith(currentMonth) }
        .sumOf { it.amount }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "이번 달 지출: ${String.format("%,d", monthlyTotal)}원",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Text(
                text = "현재 잔액: 1,250,000원",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun ExpenseItemCard(expense: ExpenseItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                spotColor = Color(0x0D000000),
                ambientColor = Color(0x0D000000)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 상호명과 날짜
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.storeName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1C1E)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = expense.date,
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                // 금액
                Text(
                    text = "-${String.format("%,d", expense.amount)}원",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }
        }
    }
}

@Composable
private fun EmptyExpenseState(onAddExpense: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "아직 등록된 지출이 없습니다",
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                Log.d(TAG, "첫 지출 등록 버튼 클릭")
                onAddExpense()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5FBF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("지출 등록하기", color = Color.White)
            }
        }
    }
}
