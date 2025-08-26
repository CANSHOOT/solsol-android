package com.heyyoung.solsol.feature.studentcouncil.presentation

import android.util.Log
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val TAG = "StudentCouncilMain"

@Composable
fun StudentCouncilMainScreen(
    onNavigateBack: () -> Unit = {}
) {
    // 전역 지출 목록 상태 관리
    var expenseList by remember {
        mutableStateOf(createDemoExpenseList())
    }

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // 학생회 홈
        composable("home") {
            StudentCouncilScreen(
                onNavigateBack = onNavigateBack,
                onNavigateToExpenseHistory = {
                    navController.navigate("expense_history")
                },
                onNavigateToExpenseRegister = {
                    navController.navigate("ocr_camera")
                },
                onNavigateToFeeStatus = {
                    navController.navigate("fee_status")
                }
            )
        }

        // 지출 내역
        composable("expense_history") {
            StudentCouncilExpenseHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRegister = { navController.navigate("ocr_camera") },
                expenseList = expenseList
            )
        }

        // OCR 카메라
        composable("ocr_camera") {
            OcrCameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onOcrResult = { ocrResult ->
                    // 새 지출 항목 추가
                    val newExpense = ExpenseItem(
                        amount = ocrResult.amount,
                        storeName = ocrResult.storeName,
                        date = ocrResult.date
                    )

                    expenseList = listOf(newExpense) + expenseList
                    Log.i(TAG, "지출 등록 완료: ${ocrResult.storeName} - ${ocrResult.amount}원")

                    // 지출 내역으로 이동
                    navController.navigate("expense_history") {
                        popUpTo("home")
                    }
                }
            )
        }
    }
}

// 데모 데이터 생성 (ExpenseItem은 StudentCouncilModels.kt에서 import됨)
private fun createDemoExpenseList(): List<ExpenseItem> = listOf(
    ExpenseItem(amount = 180000L, storeName = "커피 제국", date = "2025.03.15"),
    ExpenseItem(amount = 85000L, storeName = "팀 회식", date = "2025.03.14"),
    ExpenseItem(amount = 45000L, storeName = "문구점", date = "2025.03.13"),
    ExpenseItem(amount = 30000L, storeName = "간식비", date = "2025.03.12")
)