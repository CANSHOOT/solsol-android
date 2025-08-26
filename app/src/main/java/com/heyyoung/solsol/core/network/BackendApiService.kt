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

// ========== Response 데이터 클래스들 ==========

/**
 * 인증 응답 (로그인/회원가입/토큰갱신 공통)
 */
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val name: String
)

/**
 * 에러 응답
 */
data class ErrorResponse(
    val message: String,
    val code: String? = null
)