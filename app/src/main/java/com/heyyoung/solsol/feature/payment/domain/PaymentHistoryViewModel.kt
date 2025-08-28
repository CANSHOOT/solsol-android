package com.heyyoung.solsol.feature.payment.domain

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val repository: BackendPaymentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "PaymentHistoryViewModel"
    }

    var uiState by mutableStateOf(PaymentHistoryUiState())
        private set

    /**
     * 결제 내역 로드
     */
    fun loadPaymentHistory() {
        Log.d(TAG, "결제 내역 로드 시작")

        uiState = uiState.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            when (val result = repository.getPaymentHistory()) {
                is BackendApiResult.Success -> {
                    Log.i(TAG, "결제 내역 로드 성공: ${result.data.size}개")
                    
                    // 시간 기준 내림차순 정렬 (최신이 맨 위)
                    val sortedHistory = result.data.sortedByDescending { it.date }
                    Log.d(TAG, "결제 내역 정렬 완료: 최신순")
                    
                    uiState = uiState.copy(
                        isLoading = false,
                        paymentHistory = sortedHistory,
                        errorMessage = null
                    )
                }

                is BackendApiResult.Error -> {
                    Log.e(TAG, "결제 내역 로드 실패: ${result.message}")
                    uiState = uiState.copy(
                        isLoading = false,
                        paymentHistory = emptyList(),
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /**
     * 새로고침
     */
    fun refresh() {
        Log.d(TAG, "결제 내역 새로고침")
        loadPaymentHistory()
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    /**
     * 화면 진입 시 자동 로드
     */
    init {
        loadPaymentHistory()
    }
}

/**
 * 결제 내역 화면 UI 상태
 */
data class PaymentHistoryUiState(
    val isLoading: Boolean = false,
    val paymentHistory: List<PaymentHistoryItem> = emptyList(),
    val errorMessage: String? = null
)
