package com.heyyoung.solsol.feature.payment.domain

import android.util.Log
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.math.BigDecimal

interface BackendPaymentService {
    companion object {
        private const val TAG = "BackendPaymentService"
    }

    /**
     * QR 스캔 후 결제 정보 조회
     * GET payments/preview/{qrData}
     *
     * QR 스캔 결과와 JWT 토큰을 보내서 상품 정보, 학과별 할인율 등을 조회합니다.
     */
    @GET("payments/preview/{qrData}")
    suspend fun getPaymentPreview(
        @Path("qrData") qrData: String,
        @Header("Authorization") authorization: String
    ): Response<PaymentPreviewResponse>

    /**
     * 실제 결제 처리
     * POST payments/{tempId}
     *
     * 결제를 처리하고 쿠폰 당첨 결과를 함께 응답받습니다.
     */
    @POST("payments/{tempId}")
    suspend fun processPayment(
        @Path("tempId") tempId: Long,
        @Body request: CreatePaymentRequest,
        @Header("Authorization") authorization: String
    ): Response<CreatePaymentResponse>

    /**
     * 쿠폰 목록 조회
     * GET discounts
     *
     * 사용자가 보유한 쿠폰 목록을 조회합니다.
     */
    @GET("discounts")
    suspend fun getCoupons(
        @Header("Authorization") authorization: String
    ): Response<CouponsResponse>
}

data class PaymentPreviewResponse(
    val orderItems: List<OrderItem>,
    val total: Integer,
    val discountRate: Integer,
    val discount: Integer,
    val department: String
) {
    init {
        Log.v("PaymentPreviewResponse", "department=$department")
    }
}

/**
 * 결제 처리 요청 (서버 구조에 맞춤)
 */
data class CreatePaymentRequest(
    val amount: BigDecimal
)

/**
 * 결제 처리 응답 (쿠폰 당첨 결과 포함)
 */
data class CreatePaymentResponse(
    val success: Boolean,
    val paymentId: String,
    val winning: Boolean,        // 쿠폰 당첨 여부
    val amount: Int             // 당첨 금액
)

/**
 * 쿠폰 목록 응답
 */
data class CouponsResponse(
    val coupons: List<CouponItem>
)

/**
 * 개별 쿠폰 데이터
 */
data class CouponItem(
    val discountCouponId: Int,
    val amount: Int,
    val createdDate: String,
    val endDate: String
)

/**
 * 주문한 메뉴들 데이터
 */
data class OrderItem(
    val menuId: Long,
    val name: String,
    val price: BigDecimal,
    val image: String
)