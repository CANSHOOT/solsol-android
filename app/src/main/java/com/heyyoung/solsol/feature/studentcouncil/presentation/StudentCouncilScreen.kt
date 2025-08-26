package com.heyyoung.solsol.feature.studentcouncil.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TAG = "StudentCouncilScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCouncilScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToExpenseHistory: () -> Unit = {},
    onNavigateToExpenseRegister: () -> Unit = {},
    onNavigateToFeeStatus: () -> Unit = {}
) {
    // UI 상태 관리
    var uiState by remember { mutableStateOf(StudentCouncilUiState()) }

    // 화면 진입시 데이터 로드
    LaunchedEffect(Unit) {
        Log.d(TAG, "학생회 화면 진입 - 데이터 로드 시작")
        // TODO: ViewModel에서 데이터 로드
        loadStudentCouncilData { data ->
            uiState = data
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("학생회") },
            navigationIcon = {
                IconButton(onClick = {
                    Log.d(TAG, "뒤로가기 클릭")
                    onNavigateBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1C1C1E),
                navigationIconContentColor = Color(0xFF1C1C1E)
            )
        )

        // 메인 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 학과 학생회 제목
            Text(
                text = uiState.departmentName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 현재 잔액 및 지출 정보 카드
            BalanceInfoCard(
                currentBalance = uiState.currentBalance,
                monthlyExpense = uiState.monthlyExpense
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 회비 납부 상태 카드
            FeePaymentStatusCard(
                semester = uiState.currentSemester,
                isPaid = uiState.isFeesPaid
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 공통 버튼: 지출내역 전체 보기
            ActionButton(
                text = "지출내역 전체 보기",
                backgroundColor = Color(0xFFE8E3F3),
                textColor = Color(0xFF8B5FBF),
                onClick = {
                    Log.d(TAG, "지출내역 전체 보기 클릭")
                    onNavigateToExpenseHistory()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 학생회 권한자만 보이는 버튼들
            if (uiState.isCouncilMember) {
                Log.d(TAG, "학생회 권한자 UI 표시")

                // 지출 등록 버튼
                ActionButton(
                    text = "지출 등록",
                    backgroundColor = Color(0xFF8B5FBF),
                    textColor = Color.White,
                    icon = Icons.Default.Add,
                    onClick = {
                        Log.d(TAG, "지출 등록 클릭")
                        onNavigateToExpenseRegister()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 회비 현황 버튼
                ActionButton(
                    text = "회비 현황",
                    backgroundColor = Color.White,
                    textColor = Color(0xFF1C1C1E),
                    icon = Icons.Default.List,
                    hasBorder = true,
                    onClick = {
                        Log.d(TAG, "회비 현황 클릭")
                        onNavigateToFeeStatus()
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 우리 과 회계 현황
            Text(
                text = "우리 과 회계 현황",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )

            // 현재 잔액
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
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .fillMaxWidth()
            .height(60.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) Color(0xFFF0F9FF) else Color(0xFFFFF0F0)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPaid) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "납부완료",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
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
            .shadow(
                elevation = if (hasBorder) 2.dp else 8.dp,
                spotColor = Color(0x26000000),
                ambientColor = Color(0x26000000)
            )
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (hasBorder) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

// 임시 데이터 로드 함수 (나중에 ViewModel로 대체)
private suspend fun loadStudentCouncilData(callback: (StudentCouncilUiState) -> Unit) {
    kotlinx.coroutines.delay(500) // API 호출 시뮬레이션

    Log.d(TAG, "학생회 데이터 로드 완료")

    callback(
        StudentCouncilUiState(
            departmentName = "컴퓨터공학과 학생회",
            currentBalance = 1_250_000L,
            monthlyExpense = 340_000L,
            currentSemester = "2025-1학기",
            isFeesPaid = true,
            isCouncilMember = true // 임시: 학생회 권한자로 설정
        )
    )
}
