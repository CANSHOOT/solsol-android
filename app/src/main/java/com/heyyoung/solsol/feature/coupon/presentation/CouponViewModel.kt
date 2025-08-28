package com.heyyoung.solsol.feature.coupon.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.feature.payment.domain.BackendApiResult
import com.heyyoung.solsol.feature.payment.domain.BackendPaymentRepository
import com.heyyoung.solsol.feature.payment.domain.CouponItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponViewModel @Inject constructor(
    private val repository: BackendPaymentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CouponViewModel"
    }

    var uiState by mutableStateOf(CouponUiState())
        private set

    /**
     * 쿠폰 목록 로드
     */
    fun loadCoupons() {
        Log.d(TAG, "쿠폰 목록 로드 시작")

        uiState = uiState.copy(
            isLoading = true, 
            errorMessage = null
        )

        viewModelScope.launch {
            when (val result = repository.getCoupons()) {
                is BackendApiResult.Success -> {
                    Log.i(TAG, "쿠폰 목록 로드 성공: ${result.data.size}개")
                    
                    uiState = uiState.copy(
                        isLoading = false,
                        coupons = result.data,
                        errorMessage = null
                    )
                }
                is BackendApiResult.Error -> {
                    Log.e(TAG, "쿠폰 목록 로드 실패: ${result.message}")
                    uiState = uiState.copy(
                        isLoading = false,
                        coupons = emptyList(),
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    /**
     * 쿠폰 상태 초기화
     */
    fun resetState() {
        uiState = CouponUiState()
    }
}

/**
 * 쿠폰 화면 UI 상태
 */
data class CouponUiState(
    val isLoading: Boolean = false,
    val coupons: List<CouponItem> = emptyList(),
    val errorMessage: String? = null
)

