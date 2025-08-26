package com.heyyoung.solsol.feature.settlement.domain.model

/**
 * 송금 처리 결과 도메인 모델
 * - 은행 API 연동 결과를 담는 데이터 클래스
 */
data class PaymentResult(
    val transactionId: String?,
    val amount: Double?,
    val status: String,
    val message: String
) {
    val isSuccess: Boolean
        get() = status == "SUCCESS" && transactionId != null
    
    val isFailure: Boolean
        get() = status == "FAILURE"
}