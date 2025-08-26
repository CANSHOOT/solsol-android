package com.heyyoung.solsol.feature.studentcouncil.presentation

import java.util.*

/**
 * OCR 결과 데이터
 */
data class OcrResult(
    val amount: Long,
    val storeName: String,
    val date: String,
    val description: String
)

/**
 * 지출 항목 데이터
 */
data class ExpenseItem(
    val id: String = UUID.randomUUID().toString(),
    val amount: Long,
    val storeName: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 학생회 UI 상태
 */
data class StudentCouncilUiState(
    val departmentName: String = "",
    val currentBalance: Long = 0L,
    val monthlyExpense: Long = 0L,
    val currentSemester: String = "",
    val isFeesPaid: Boolean = false,
    val isCouncilMember: Boolean = false,
    val isLoading: Boolean = false
)

/**
 * OCR 카메라 UI 상태
 */
data class OcrCameraUiState(
    val hasPermission: Boolean = false,
    val showPermissionDialog: Boolean = false,
    val isProcessing: Boolean = false
)