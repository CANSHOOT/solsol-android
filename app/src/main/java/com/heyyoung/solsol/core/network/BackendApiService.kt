package com.heyyoung.solsol.core.network

import retrofit2.Response
import retrofit2.http.*

/**
 * 백엔드 API 서비스 인터페이스
 *
 * Base URL: http://localhost:8080/api/v1/auth
 * Spring Boot 백엔드와 통신하기 위한 API 정의
 */
interface BackendApiService {

    /**
     * 회원가입
     * POST /auth/signup
     */
    @POST("auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<AuthResponse>

    /**
     * 로그인
     * POST /auth/login
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    /**
     * JWT 토큰 갱신
     * POST /auth/refresh
     */
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<AuthResponse>

    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<UserDto>


    // ================= 학생회/정산 =======================

    // 1. 학과 홈 요약
    @GET("/api/v1/settlement/home")
    suspend fun getDeptSummary(
        @Query("month") month: String? = null,
        @Query("semester") semester: String? = null,
        @Query("tz") tz: String = "+09:00"
    ): DeptHomeSummaryResponse

    // 2. 지출 목록
    @GET("/api/v1/settlement/expenditures")
    suspend fun getExpenditures(
        @Query("month") month: String? = null,
        @Query("tz") tz: String = "+09:00",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): DeptExpenditureListResponse

    // 3. 회비 납부 현황 (특정 회비)
    @GET("/api/v1/settlement/councils/{councilId}/fees/{feeId}/status")
    suspend fun getFeeStatus(
        @Path("councilId") councilId: Long,
        @Path("feeId") feeId: Long
    ): FeeStatusResponse

    // 4. 회비 송금
    @POST("/api/v1/councils/transfer")
    suspend fun transferFee(
        @Body request: CouncilFeeTransferCommand
    ): AccountTransferResponse

    // 5. 학생회 지출 등록
    @POST("/api/v1/council-expenditures")
    suspend fun addExpenditure(
        @Body request: CouncilExpenditureRequest
    ): CouncilExpenditureResponse
}

// ========== Request 데이터 클래스들 ==========

/**
 * 회원가입 요청
 */
data class SignupRequest(
    val email: String,
    val studentNumber: String,
    val name: String,
    val departmentId: Int,
    val councilId: Int,
    val isCouncilOfficer: Boolean
)

/**
 * 로그인 요청
 */
data class LoginRequest(
    val email: String,
    val studentNumber: String
)

/**
 * 토큰 갱신 요청
 */
data class RefreshTokenRequest(
    val refreshToken: String
)

data class CouncilExpenditureRequest(
    val councilId: Long,
    val amount: Long,
    val description: String,
    val expenditureDate: String,  // yyyy-MM-dd
    val category: String
)

data class CouncilFeeTransferCommand(
    val councilId: Long,
    val fromUserId: String,
    val toUserId: String,
    val feeId: Long,
    val amount: Long,
    val withdrawalAccountNo: String,
    val depositTransactionSummary: String? = null,
    val withdrawalTransactionSummary: String? = null
)

// ========== Response DTOs ==========

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val name: String
)

data class ErrorResponse(
    val message: String,
    val code: String? = null
)

data class CouncilExpenditureResponse(
    val expenditureId: Long,
    val councilId: Long,
    val amount: Long,
    val description: String,
    val expenditureDate: String,
    val category: String,
    val approvedBy: String?
)

data class DeptHomeSummaryResponse(
    val header: Header,
    val balanceCard: BalanceCard,
    val feeBadge: FeeBadge?
) {
    data class Header(
        val departmentId: Long,
        val councilId: Long,
        val councilName: String,
        val presidentUserId: String,
        val presidentName: String
    )

    data class BalanceCard(
        val currentBalance: Long,
        val monthSpendTotal: Long,
        val month: String
    )

    data class FeeBadge(
        val semester: String,
        val paid: Boolean,
        val feeAmount: Long,
        val paidAt: String?
    )
}

data class DeptExpenditureListResponse(
    val header: Header,
    val summary: Summary,
    val page: PageMeta,
    val items: List<Item>
) {
    data class Header(
        val currentBalance: Double
    )

    data class Summary(
        val month: String,
        val monthSpendTotal: Double
    )

    data class PageMeta(
        val page: Int,
        val size: Int,
        val totalElements: Long,
        val totalPages: Int
    )

    data class Item(
        val expenditureId: Long,
        val date: String,
        val description: String,
        val amount: Double
    )
}

data class FeeStatusResponse(
    val userId: String,
    val name: String,
    val department: String,
    val studentId: String,
    val paid: Boolean,
    val paidAt: String?
)

data class AccountTransferResponse(
    val transactionId: String,
    val status: String,
    val amount: String,
    val balance: String
)

data class UserDto(
    val userId: String,
    val studentNumber: String,
    val name: String,
    val departmentId: Int,
    val departmentName: String,
    val councilId: Int,
    val accountNo: String,
    val accountBalance: Long,
    val councilOfficer: Boolean
)
