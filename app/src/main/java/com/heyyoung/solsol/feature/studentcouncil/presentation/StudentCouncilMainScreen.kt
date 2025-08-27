package com.heyyoung.solsol.feature.studentcouncil.presentation

import android.util.Log
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.heyyoung.solsol.core.network.CouncilExpenditureRequest
import com.heyyoung.solsol.feature.studentcouncil.StudentCouncilViewModel
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Main 엔트리: 학생회 화면
 * deptId, councilId는 네비게이션 파라미터로만 전달 (API 호출에는 직접 사용하지 않음)
 */
@Composable
fun StudentCouncilMainScreen(
    deptId: Long,
    councilId: Long,
    onNavigateBack: () -> Unit = {}
) {
    val navController = rememberNavController()
    val viewModel: StudentCouncilViewModel = hiltViewModel()

    // 최초 로딩 (deptId, councilId는 전달만 하고 실제 API는 Authentication 기반으로 동작)
    LaunchedEffect(Unit) {
        viewModel.loadDeptSummary()
        viewModel.loadExpenditures()
        // feeId는 아직 고정값 필요 → 실제 서비스에서는 선택된 회비 ID를 넘겨야 함
        viewModel.loadFeeStatus(councilId = 1, feeId = 10001L)
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // 홈 화면
        composable("home") {
            StudentCouncilScreen(
                summary = viewModel.summary,
                onNavigateBack = onNavigateBack,
                onNavigateToExpenseHistory = { navController.navigate("expense_history") },
                onNavigateToExpenseRegister = { navController.navigate("ocr_camera") },
                onNavigateToFeeStatus = { navController.navigate("fee_status") }
            )
        }

        // 지출 내역 화면
        composable("expense_history") {
            StudentCouncilExpenseHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRegister = { navController.navigate("ocr_camera") },
                expenseList = viewModel.expenditureList,
                currentBalance = viewModel.currentBalance
            )
        }

        // 회비 현황 화면
        composable("fee_status") {
            StudentCouncilFeeStatusScreen(
                onNavigateBack = { navController.popBackStack() },
                // FeeStatusResponse? 단일 값이므로 list 형태로 변환
                feeStatusList = viewModel.feeStatus?.let { listOf(it) } ?: emptyList()
            )
        }

        // OCR 카메라 → 지출 등록
        composable("ocr_camera") {
            OcrCameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onOcrResult = { result ->
                    Log.d("OcrResult", "최종 OCR 결과 = $result")
                    val req = CouncilExpenditureRequest(
                        councilId = councilId,
                        amount = result.amount,
                        description = result.description.ifBlank { "${result.storeName} 지출" },
                        expenditureDate = LocalDate.parse(result.date)
                            .atStartOfDay(ZoneOffset.UTC)
                            .toInstant()
                            .toString(), // → "2025-08-27T00:00:00Z"
                        category = "일반"
                    )
                    viewModel.addExpenditure(req)
                    navController.navigate("expense_history") {
                        popUpTo("home")
                    }
                }
            )
        }
    }
}