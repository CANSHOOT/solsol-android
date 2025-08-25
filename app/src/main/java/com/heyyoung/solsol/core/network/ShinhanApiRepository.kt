package com.heyyoung.solsol.core.network

import android.util.Log
import com.heyyoung.solsol.core.network.ApiKeyManager
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 신한은행 API Repository
 *
 * 실제 API 호출과 비즈니스 로직을 처리합니다.
 * - 사용자 생성/조회
 * - 계좌 생성 (상품 생성 -> 계좌 생성 순서)
 * - 잔액 조회
 * - 에러 처리 및 로깅
 */
@Singleton
class ShinhanApiRepository @Inject constructor(
    private val apiService: ShinhanApiService,
    private val apiKeyManager: ApiKeyManager
) {

    companion object {
        private const val TAG = "ShinhanApiRepository"
    }

    /**
     * 사용자 로그인 처리 (통합 로그인/회원가입)
     *
     * 처리 순서:
     * 1. 사용자 조회 시도
     * 2. 없으면 새로 생성
     * 3. 계좌 생성 (상품 생성 -> 계좌 생성)
     * 4. 잔액 조회
     * 5. 결과 반환
     */
    suspend fun loginUser(email: String, studentNumber: String): ApiResult<LoginResult> {
        Log.d(TAG, "로그인 처리 시작")
        Log.d(TAG, "이메일: $email")
        Log.d(TAG, "학번: $studentNumber")

        return try {
            // 1단계: 사용자 조회 또는 생성
            val userKey = findOrCreateUser(email)
            if (userKey == null) {
                Log.e(TAG, "사용자 생성/조회 실패")
                return ApiResult.Error("사용자 생성에 실패했습니다")
            }

            Log.i(TAG, "사용자 키 획득 성공: $userKey")

            // 2단계: 계좌 생성 (이미 있으면 기존 계좌 사용)
            val accountNo = createUserAccount(userKey, email)
            if (accountNo == null) {
                Log.w(TAG, "계좌 생성 실패, 기존 계좌가 있을 수 있음")
            }

            // 3단계: 잔액 조회
            val balance = if (accountNo != null) {
                getAccountBalance(userKey, accountNo)
            } else {
                Log.d(TAG, "계좌번호가 없어 잔액 조회 생략")
                0L
            }

            // 4단계: 결과 생성
            val result = LoginResult(
                userKey = userKey,
                accountNo = accountNo,
                balance = balance ?: 0L,
                studentName = extractNameFromEmail(email),
                studentNumber = studentNumber
            )

            Log.i(TAG, "로그인 처리 완료")
            Log.d(TAG, "사용자명: ${result.studentName}")
            Log.d(TAG, "계좌번호: ${result.accountNo ?: "없음"}")
            Log.d(TAG, "잔액: ${result.balance}원")

            ApiResult.Success(result)

        } catch (e: Exception) {
            Log.e(TAG, "로그인 처리 중 예외 발생: ${e.message}", e)
            ApiResult.Error("로그인 중 오류가 발생했습니다: ${e.message}")
        }
    }

    /**
     * 사용자 조회 또는 생성
     *
     * 1. 기존 사용자 조회 시도
     * 2. 없으면 새로 생성
     * 3. userKey 반환
     */
    private suspend fun findOrCreateUser(email: String): String? {
        val apiKey = apiKeyManager.getCurrentApiKey()
        val userId = this.generateUserIdFromEmail(email)

        Log.d(TAG, "사용자 조회/생성 시작")
        Log.d(TAG, "생성된 userId: $userId")

        try {
            // 1단계: 기존 사용자 조회
            Log.d(TAG, "기존 사용자 조회 시도")
            val searchResponse = apiService.searchUser(
                SearchUserRequest(
                    apiKey = apiKey.apiKey,
                    userId = userId
                )
            )

            if (searchResponse.isSuccessful) {
                val searchBody = searchResponse.body()
                if (searchBody?.userKey != null && searchBody.userKey.isNotBlank()) {
                    Log.i(TAG, "기존 사용자 발견")
                    Log.d(TAG, "userKey: ${searchBody.userKey}")
                    return searchBody.userKey
                }
            }

            Log.d(TAG, "기존 사용자 없음, 신규 생성 시도")

            // 2단계: 신규 사용자 생성
            val createResponse = apiService.createUser(
                CreateUserRequest(
                    apiKey = apiKey.apiKey,
                    userId = userId
                )
            )

            if (createResponse.isSuccessful) {
                val createBody = createResponse.body()
                if (createBody?.userKey != null && createBody.userKey.isNotBlank()) {
                    Log.i(TAG, "신규 사용자 생성 완료")
                    Log.d(TAG, "새 userKey: ${createBody.userKey}")
                    return createBody.userKey
                } else {
                    Log.e(TAG, "사용자 생성 응답에 userKey 없음: $createBody")
                }
            } else {
                Log.e(TAG, "사용자 생성 API 호출 실패: ${createResponse.code()} ${createResponse.message()}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "사용자 조회/생성 중 예외: ${e.message}", e)
        }

        return null
    }

    /**
     * 사용자 계좌 생성
     *
     * 신한은행 API는 2단계로 계좌를 생성합니다:
     * 1. 수신상품 생성 (통장 상품 만들기)
     * 2. 계좌 생성 (실제 계좌번호 발급)
     */
    private suspend fun createUserAccount(userKey: String, email: String): String? {
        Log.d(TAG, "계좌 생성 시작")

        try {
            val apiKey = apiKeyManager.getCurrentApiKey()

            // 1단계: 수신상품 생성
            Log.d(TAG, "수신상품 생성 시도")
            val productResponse = apiService.createDemandDeposit(
                CreateDemandDepositRequest(
                    Header = createRequestHeader("createDemandDeposit", apiKey.apiKey, userKey),
                    accountName = "쏠쏠해영 통장",
                    accountDescription = "대학생 전용 핀테크 계좌"
                )
            )

            if (!productResponse.isSuccessful || productResponse.body()?.REC == null) {
                Log.e(TAG, "수신상품 생성 실패: ${productResponse.code()} ${productResponse.message()}")
                return null
            }

            val productUniqueNo = productResponse.body()!!.REC!!.accountTypeUniqueNo
            Log.i(TAG, "수신상품 생성 완료")
            Log.d(TAG, "상품 고유번호: $productUniqueNo")

            // 잠시 대기 (API 서버 처리 시간 고려)
            delay(500)

            // 2단계: 실제 계좌 생성
            Log.d(TAG, "계좌 생성 시도")
            val accountResponse = apiService.createBankAccount(
                CreateAccountRequest(
                    Header = createRequestHeader("createDemandDepositAccount", apiKey.apiKey, userKey),
                    accountTypeUniqueNo = productUniqueNo
                )
            )

            if (accountResponse.isSuccessful && accountResponse.body()?.REC != null) {
                val accountNo = accountResponse.body()!!.REC!!.accountNo
                Log.i(TAG, "계좌 생성 완료")
                Log.d(TAG, "계좌번호: ${maskAccountNumber(accountNo)}")
                return accountNo
            } else {
                Log.e(TAG, "계좌 생성 실패: ${accountResponse.code()} ${accountResponse.message()}")
                return null
            }

        } catch (e: Exception) {
            Log.e(TAG, "계좌 생성 중 예외: ${e.message}", e)
            return null
        }
    }

    /**
     * 계좌 잔액 조회
     */
    private suspend fun getAccountBalance(userKey: String, accountNo: String): Long? {
        if (accountNo.isBlank()) {
            Log.d(TAG, "계좌번호가 없어 잔액 조회 생략")
            return 0L
        }

        Log.d(TAG, "잔액 조회 시작")
        Log.d(TAG, "계좌: ${maskAccountNumber(accountNo)}")

        try {
            val apiKey = apiKeyManager.getCurrentApiKey()

            val response = apiService.getAccountBalance(
                BalanceRequest(
                    Header = createRequestHeader("inquireDemandDepositAccountBalance", apiKey.apiKey, userKey),
                    accountNo = accountNo
                )
            )

            if (response.isSuccessful && response.body()?.REC != null) {
                val balance = response.body()!!.REC!!.accountBalance
                Log.i(TAG, "잔액 조회 완료: ${balance}원")
                return balance
            } else {
                Log.e(TAG, "잔액 조회 실패: ${response.code()} ${response.message()}")
                return 0L
            }

        } catch (e: Exception) {
            Log.e(TAG, "잔액 조회 중 예외: ${e.message}", e)
            return 0L
        }
    }

    // ========== 유틸리티 함수들 ==========

    /**
     * API 요청 헤더 생성
     */
    private fun createRequestHeader(apiName: String, apiKey: String, userKey: String): RequestHeader {
        return try {
            // LocalDateTime 사용 시도
            val now = LocalDateTime.now()
            val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd")
            val timeFormat = DateTimeFormatter.ofPattern("HHmmss")

            RequestHeader(
                apiName = apiName,
                transmissionDate = now.format(dateFormat),
                transmissionTime = now.format(timeFormat),
                institutionCode = "00100",
                fintechAppNo = "001",
                apiServiceCode = apiName,
                institutionTransactionUniqueNo = generateTransactionId(),
                apiKey = apiKey,
                userKey = userKey
            )
        } catch (e: Exception) {
            Log.w(TAG, "LocalDateTime 사용 실패, Calendar 사용: ${e.message}")
            // Calendar 사용으로 fallback
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)

            RequestHeader(
                apiName = apiName,
                transmissionDate = String.format("%04d%02d%02d", year, month, day),
                transmissionTime = String.format("%02d%02d%02d", hour, minute, second),
                institutionCode = "00100",
                fintechAppNo = "001",
                apiServiceCode = apiName,
                institutionTransactionUniqueNo = generateTransactionId(),
                apiKey = apiKey,
                userKey = userKey
            )
        }
    }

    /**
     * 이메일에서 UserId 생성
     */
    private fun generateUserIdFromEmail(email: String): String {
        val userId = email.trim().lowercase()
        Log.v(TAG, "이메일 -> userId 변환(이메일 전체 사용): $email -> $userId")
        return userId
    }

    /**
     * 이메일에서 이름 추출 (간단한 방식)
     */
    private fun extractNameFromEmail(email: String): String {
        val localPart = email.substringBefore("@")
        return when {
            localPart.contains("test") -> "테스트 사용자"
            localPart.contains("demo") -> "데모 사용자"
            localPart.contains("student") -> "학생 사용자"
            localPart.contains("jungini") -> "정인이"
            else -> "쏠쏠 사용자"
        }
    }

    /**
     * 고유 거래 ID 생성
     */
    private fun generateTransactionId(): String {
        return "SOLSOL${System.currentTimeMillis()}${Random().nextInt(1000)}"
    }

    /**
     * 계좌번호 마스킹 (보안)
     */
    private fun maskAccountNumber(accountNo: String): String {
        return if (accountNo.length > 6) {
            "${accountNo.take(6)}***${accountNo.takeLast(2)}"
        } else {
            accountNo
        }
    }
}

// ========== 결과 데이터 클래스들 ==========

/**
 * API 결과 래퍼 클래스
 */
sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val message: String) : ApiResult<T>()
}

/**
 * 로그인 결과
 */
data class LoginResult(
    val userKey: String,        // 신한은행 사용자 키
    val accountNo: String?,     // 계좌번호 (생성 실패시 null)
    val balance: Long,          // 계좌 잔액
    val studentName: String,    // 학생 이름
    val studentNumber: String   // 학번
)