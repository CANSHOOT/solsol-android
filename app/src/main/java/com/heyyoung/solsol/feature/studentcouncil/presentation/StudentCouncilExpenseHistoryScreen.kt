package com.heyyoung.solsol.feature.studentcouncil.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.core.network.CouncilExpenditureResponse
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ExpenseHistoryScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCouncilExpenseHistoryScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    expenseList: List<CouncilExpenditureResponse> = emptyList(),
    currentBalance: Long
) {
    Log.d(TAG, "지출 내역 화면 진입 - 총 ${expenseList.size}개 항목")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "지출 내역",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 이번 달 지출 요약 카드
            ExpenseSummaryCard(
                expenseList = expenseList,
                currentBalance = currentBalance
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 지출 내역 리스트
            if (expenseList.isEmpty()) {
                EmptyExpenseState(onAddExpense = onNavigateToRegister)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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
}

@Composable
private fun ExpenseSummaryCard(
    expenseList: List<CouncilExpenditureResponse>,
    currentBalance: Long
    ) {
    val currentMonth = SimpleDateFormat("yyyy-MM", Locale.KOREA).format(Date())
    val monthlyTotal = expenseList
        .filter { it.expenditureDate.startsWith(currentMonth) }
        .sumOf { it.amount }

    Box(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = 1.dp,
                color = Color(0xCC8B5FBF),
                shape = RoundedCornerShape(size = 8.dp)
            )
            .padding(1.dp)
            .width(342.dp)
            .height(100.dp)
            .background(
                color = Color(0xFFF8F7FF),
                shape = RoundedCornerShape(size = 8.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "이번 달 지출: ${String.format("%,d", monthlyTotal)}원",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "현재 잔액: ${String.format("%,d", currentBalance)}원",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun ExpenseItemCard(expense: CouncilExpenditureResponse) {
    Log.v(TAG, "지출 항목 렌더링: ${expense.description} - ${expense.amount}원")

    Box(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .padding(1.dp)
            .width(342.dp)
            .height(60.dp)
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(size = 8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 왼쪽: 날짜와 설명
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.expenditureDate,
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = expense.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E)
                )
            }

            // 오른쪽: 금액
            Text(
                text = "-${String.format("%,d", expense.amount)}원",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
        }
    }
}

@Composable
private fun EmptyExpenseState(onAddExpense: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "아직 등록된 지출이 없습니다",
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                Log.d(TAG, "첫 지출 등록 버튼 클릭")
                onAddExpense()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8B5FBF)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    spotColor = Color(0x1A8B5FBF),
                    ambientColor = Color(0x1A8B5FBF)
                )
                .height(48.dp)
                .width(200.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "지출 등록하기",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}