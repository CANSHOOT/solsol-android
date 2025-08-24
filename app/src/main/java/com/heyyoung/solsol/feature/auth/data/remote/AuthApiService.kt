package com.heyyoung.solsol.feature.auth.data.remote

import com.heyyoung.solsol.feature.auth.data.remote.dto.*
import retrofit2.http.*

/**
 * 인증 관련 REST API 인터페이스
 * - 로그인, 회원가입, 토큰 갱신, 사용자 정보 조회
 */
interface AuthApiService {
    
    /**
     * 로그인
     */
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse
    
    /**
     * 회원가입
     */
    @POST("api/v1/auth/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): SignUpResponse
    
    /**
     * 토큰 갱신
     */
    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): RefreshTokenResponse
    
    /**
     * 로그아웃
     */
    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body request: RefreshTokenRequest
    ): Boolean
    
    /**
     * 사용자 정보 조회
     */
    @GET("api/v1/auth/me")
    suspend fun getUserInfo(): UserInfoResponse
    
    /**
     * 이메일 중복 확인
     */
    @GET("api/v1/auth/check-email")
    suspend fun checkEmailExists(
        @Query("email") email: String
    ): Boolean
}