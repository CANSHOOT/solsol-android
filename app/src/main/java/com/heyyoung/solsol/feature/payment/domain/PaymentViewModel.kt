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
class PaymentViewModel @Inject constructor(
    private val repository: BackendPaymentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "PaymentViewModel"
    }

    var uiState by mutableStateOf(PaymentUiState())
        private set

    // QR 스캔 데이터 저장
    private var currentQrData: String = ""

    /**
     * QR 스캔 완료 후 결제 정보 로드
     */
    fun loadPaymentInfo(qrData: String) {
        Log.d(TAG, "결제 정보 로드 시작: $qrData")

        if (qrData.isBlank()) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "유효하지 않은 QR 코드입니다"
            )
            return
        }

        // QR 데이터 저장 (결제 시 사용)
        currentQrData = qrData

        uiState = uiState.copy(
            isLoading = true,
            errorMessage = null,
            paymentInfo = null
        )

        viewModelScope.launch {
            when (val result = repository.getPaymentPreview(qrData)) {
                is BackendApiResult.Success -> {
                    Log.i(TAG, "결제 정보 로드 성공")
                    Log.d(TAG, "상품 개수: ${result.data.orderItems.size}")
                    Log.d(TAG, "총 금액: ${result.data.total}원")
                    Log.d(TAG, "할인율: ${result.data.discountRate}%")

                    uiState = uiState.copy(
                        isLoading = false,
                        paymentInfo = result.data,
                        errorMessage = null
                    )
                }

                is BackendApiResult.Error -> {
                    Log.e(TAG, "결제 정보 로드 실패: ${result.message}")
                    uiState = uiState.copy(
                        isLoading = false,
                        paymentInfo = null,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /**
     * 실제 결제 처리
     */
    fun processPayment() {
        Log.d(TAG, "결제 처리 시작")

        val currentPaymentInfo = uiState.paymentInfo
        if (currentPaymentInfo == null) {
            Log.e(TAG, "결제 정보가 없습니다")
            uiState = uiState.copy(errorMessage = "결제 정보가 없습니다")
            return
        }

        uiState = uiState.copy(isProcessingPayment = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // 실제 결제 API 호출 (서버에서 쿠폰 당첨 결과 응답)
                val finalAmount =
                    currentPaymentInfo.total.toInt() - currentPaymentInfo.discount.toInt()

                when (val result = repository.processPayment(currentQrData, finalAmount)) {
                    is BackendApiResult.Success -> {
                        val couponResult = result.data

                        Log.i(TAG, "결제 처리 성공")
                        Log.d(
                            TAG,
                            "서버 쿠폰 당첨 결과 - 당첨: ${couponResult.winning}, 금액: ${couponResult.amount}원"
                        )

                        uiState = uiState.copy(
                            isProcessingPayment = false,
                            isPaymentComplete = true,
                            couponResult = couponResult,
                            errorMessage = null
                        )
                    }

                    is BackendApiResult.Error -> {
                        Log.e(TAG, "결제 처리 실패: ${result.message}")
                        uiState = uiState.copy(
                            isProcessingPayment = false,
                            errorMessage = result.message
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "결제 처리 실패: ${e.message}", e)
                uiState = uiState.copy(
                    isProcessingPayment = false,
                    errorMessage = "결제 처리 중 오류가 발생했습니다"
                )
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
     * 결제 상태 초기화 (새로운 결제 시작시 호출)
     */
    fun resetPaymentState() {
        Log.d(TAG, "결제 상태 초기화")
        currentQrData = ""
        uiState = PaymentUiState()
    }

    /**
     * 결제 완료 상태 확인
     */
    fun isPaymentComplete(): Boolean = uiState.isPaymentComplete


}

/**
 * 결제 화면 UI 상태
 */
data class PaymentUiState(
    val isLoading: Boolean = false,                          // 결제 정보 로딩 중
    val isProcessingPayment: Boolean = false,                // 결제 처리 중
    val isPaymentComplete: Boolean = false,                  // 결제 완료
    val paymentInfo: PaymentPreviewResponse? = null,         // 결제 정보
    val couponResult: CouponResult? = null,                  // 쿠폰 당첨 결과
    val errorMessage: String? = null                         // 에러 메시지
)

/**
 * 쿠폰 당첨 결과 데이터
 */
data class CouponResult(
    val winning: Boolean,                                    // 쿠폰 당첨 여부
    val amount: Int                                          // 당첨 금액 (0이면 미당첨)
)
