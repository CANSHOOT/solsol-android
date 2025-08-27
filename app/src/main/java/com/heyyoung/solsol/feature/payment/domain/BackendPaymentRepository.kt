package com.heyyoung.solsol.feature.payment.domain

import android.util.Log
import com.heyyoung.solsol.core.auth.TokenManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 백엔드 결제 API Repository
 *
 * Spring Boot 백엔드와 통신하여 결제 처리를 담당합니다.
 * - QR 스캔 후 결제 정보 조회
 * - 결제 처리
 * - JWT 토큰 관리
 */
@Singleton
class BackendPaymentRepository @Inject constructor(
    private val backendApiService: BackendPaymentService,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "BackendPaymentRepository"
    }

    /**
     * QR 스캔 결과로 결제 정보 조회
     *
     * 1. QR 데이터를 백엔드에 전송
     * 2. 상품 정보, 가격, 할인율 등 조회
     * 3. 결제 정보 반환
     */
    suspend fun getPaymentPreview(qrData: String): BackendApiResult<PaymentPreviewResponse> {
        Log.d(TAG, "결제 정보 조회 시작")
        Log.d(TAG, "QR 데이터: $qrData")

        return try {
            // 토큰 가져오기 (Flow의 경우 first()로 값 추출)
            val accessToken = tokenManager.getAccessToken()?.first()
            
            if (accessToken.isNullOrEmpty()) {
                Log.e(TAG, "액세스 토큰이 없습니다")
                return BackendApiResult.Error("로그인이 필요합니다")
            }

            Log.d(TAG, "토큰 확인: Bearer $accessToken")

            // API 호출
            val response = backendApiService.getPaymentPreview("1", "Bearer $accessToken")

            if (response.isSuccessful && response.body() != null) {
                val paymentInfo = response.body()!!
                Log.i(TAG, "결제 정보 조회 성공: ${paymentInfo.orderItems.size}개 상품")
                Log.d(TAG, "할인율: ${paymentInfo.discountRate}%, 학과: ${paymentInfo.department}")
                
                BackendApiResult.Success(paymentInfo)
            } else {
                val errorMessage = parseErrorMessage(response.code(), response.message())
                Log.e(TAG, "결제 정보 조회 실패: $errorMessage")
                BackendApiResult.Error(errorMessage)
            }

        } catch (e: Exception) {
            Log.e(TAG, "결제 정보 조회 중 예외: ${e.message}", e)
            BackendApiResult.Error("네트워크 연결을 확인해주세요")
        }
    }

    /**
     * 실제 결제 처리 (서버에서 쿠폰 당첨 결과 응답)
     *
     * 1. QR 데이터에서 tempId 추출
     * 2. 결제 금액을 서버에 전송
     * 3. 쿠폰 당첨 결과 함께 응답
     */
    suspend fun processPayment(qrData: String, finalAmount: Int): BackendApiResult<CouponResult> {
        Log.d(TAG, "결제 처리 시작")
        Log.d(TAG, "QR 데이터: $qrData, 결제 금액: ${finalAmount}원")

        return try {
            // 토큰 가져오기
            val accessToken = tokenManager.getAccessToken()?.first()
            
            if (accessToken.isNullOrEmpty()) {
                Log.e(TAG, "액세스 토큰이 없습니다")
                return BackendApiResult.Error("로그인이 필요합니다")
            }

            // QR 데이터에서 tempId 추출 (임시로 간단한 파싱)
            val tempId = extractTempIdFromQrData(qrData)
            
            // 결제 처리 API 호출
            val response = backendApiService.processPayment(
                tempId = tempId,
                request = CreatePaymentRequest(amount = java.math.BigDecimal(finalAmount)),
                authorization = "Bearer $accessToken"
            )

            if (response.isSuccessful && response.body() != null) {
                val paymentResult = response.body()!!
                Log.i(TAG, "결제 처리 성공: ${paymentResult.paymentId}")
                Log.d(TAG, "쿠폰 당첨 결과 - winning: ${paymentResult.winning}, amount: ${paymentResult.amount}원")
                
                // 서버 응답을 CouponResult로 변환
                val couponResult = CouponResult(
                    winning = paymentResult.winning,
                    amount = paymentResult.amount
                )
                
                BackendApiResult.Success(couponResult)
            } else {
                val errorMessage = parseErrorMessage(response.code(), response.message())
                Log.e(TAG, "결제 처리 실패: $errorMessage")
                BackendApiResult.Error(errorMessage)
            }

        } catch (e: Exception) {
            Log.e(TAG, "결제 처리 중 예외: ${e.message}", e)
            BackendApiResult.Error("결제 처리 중 오류가 발생했습니다")
        }
    }

    /**
     * QR 데이터에서 tempId 추출
     * QR 코드 형식에 따라 다르게 구현해야 함
     */
    private fun extractTempIdFromQrData(qrData: String): Long {
        return try {
            // 임시 구현: QR 데이터가 숫자라고 가정
            // 실제로는 QR 형식에 따라 파싱 로직 구현 필요
            qrData.toLongOrNull() ?: 1L
        } catch (e: Exception) {
            Log.w(TAG, "QR 데이터에서 tempId 추출 실패: $qrData, 기본값 1 사용")
            1L
        }
    }

    /**
     * 쿠폰 목록 조회
     *
     * 사용자가 보유한 쿠폰 목록을 조회합니다.
     */
    suspend fun getCoupons(): BackendApiResult<List<CouponItem>> {
        Log.d(TAG, "쿠폰 목록 조회 시작")

        return try {
            // 토큰 가져오기
            val accessToken = tokenManager.getAccessToken()?.first()
            
            if (accessToken.isNullOrEmpty()) {
                Log.e(TAG, "액세스 토큰이 없습니다")
                return BackendApiResult.Error("로그인이 필요합니다")
            }

            // 쿠폰 목록 조회 API 호출
            val response = backendApiService.getCoupons("Bearer $accessToken")

            if (response.isSuccessful && response.body() != null) {
                val couponsResponse = response.body()!!
                Log.i(TAG, "쿠폰 목록 조회 성공: ${couponsResponse.coupons.size}개 쿠폰")
                
                BackendApiResult.Success(couponsResponse.coupons)
            } else {
                val errorMessage = parseErrorMessage(response.code(), response.message())
                Log.e(TAG, "쿠폰 목록 조회 실패: $errorMessage")
                BackendApiResult.Error(errorMessage)
            }

        } catch (e: Exception) {
            Log.e(TAG, "쿠폰 목록 조회 중 예외: ${e.message}", e)
            BackendApiResult.Error("네트워크 연결을 확인해주세요")
        }
    }

    /**
     * HTTP 에러 코드에 따른 사용자 친화적 메시지 생성
     */
    private fun parseErrorMessage(code: Int, message: String?): String {
        return when (code) {
            400 -> "입력 정보를 확인해주세요"
            401 -> "이메일 또는 학번이 올바르지 않습니다"
            403 -> "접근 권한이 없습니다"
            404 -> "사용자를 찾을 수 없습니다"
            409 -> "이미 가입된 이메일입니다"
            500 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요"
            else -> message ?: "알 수 없는 오류가 발생했습니다"
        }
    }
}

/**
 * 백엔드 API 결과 래퍼
 */
sealed class BackendApiResult<T> {
    data class Success<T>(val data: T) : BackendApiResult<T>()
    data class Error<T>(val message: String) : BackendApiResult<T>()
}

/**
 * 결제 정보 창 조회 데이터
 */

