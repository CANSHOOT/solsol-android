package com.heyyoung.solsol.feature.studentcouncil.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.core.network.CouncilExpenditureResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCouncilExpenseHistoryScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    expenseList: List<CouncilExpenditureResponse>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("지출 내역") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            },
            actions = {
                IconButton(onClick = onNavigateToRegister) {
                    Icon(Icons.Default.Add, contentDescription = "지출 등록", tint = Color(0xFF8B5FBF))
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
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun ExpenseSummaryCard(expenseList: List<CouncilExpenditureResponse>) {
    val ym = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    val monthlyTotal = expenseList
        .filter { it.expenditureDate.startsWith(ym) }
        .sumOf { it.amount }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(4.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = "이번 달 지출: ${String.format("%,d", monthlyTotal)}원",
                fontSize = 16.sp,
                color = Color(0xFF1C1C1E)
            )
            Text(
                text = "지출 건수: ${expenseList.count { it.expenditureDate.startsWith(ym) }}건",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun ExpenseItemCard(expense: CouncilExpenditureResponse) {
    val displayDate = runCatching {
        LocalDate.parse(expense.expenditureDate, DateTimeFormatter.ISO_LOCAL_DATE)
            .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    }.getOrElse { expense.expenditureDate }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, spotColor = Color(0x0D000000), ambientColor = Color(0x0D000000)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = expense.description, // OCR에서 "상호 + 지출" 등으로 생성
                        fontSize = 16.sp,
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = displayDate, fontSize = 12.sp, color = Color(0xFF666666))
                }
                Text(
                    text = "-${String.format("%,d", expense.amount)}원",
                    fontSize = 18.sp,
                    color = Color(0xFF1C1C1E)
                )
            }
        }
    }
}

@Composable
private fun EmptyExpenseState(onAddExpense: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "아직 등록된 지출이 없습니다", fontSize = 16.sp, color = Color(0xFF666666))
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddExpense,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5FBF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("지출 등록하기", color = Color.White)
            }
        }
    }
}
