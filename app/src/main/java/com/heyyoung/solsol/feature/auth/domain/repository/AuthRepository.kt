package com.heyyoung.solsol.feature.auth.domain.repository

import com.heyyoung.solsol.feature.auth.domain.model.AuthTokens
import com.heyyoung.solsol.feature.auth.domain.model.AuthUser
import com.heyyoung.solsol.feature.auth.domain.model.SignUpResult

/**
 * 인증 Repository 인터페이스
 */
interface AuthRepository {
    
    /**
     * 로그인
     */
    suspend fun login(email: String): Result<AuthTokens>
    
    /**
     * 회원가입
     */
    suspend fun signUp(
        email: String,
        name: String,
        studentNumber: String,
        departmentName: String,
        councilId: Long,
        isCouncilOfficer: Boolean = false
    ): Result<SignUpResult>
    
    /**
     * 토큰 갱신
     */
    suspend fun refreshToken(refreshToken: String): Result<AuthTokens>
    
    /**
     * 로그아웃
     */
    suspend fun logout(): Result<Boolean>
    
    /**
     * 사용자 정보 조회
     */
    suspend fun getUserInfo(): Result<AuthUser>
    
    /**
     * 이메일 중복 확인
     */
    suspend fun checkEmailExists(email: String): Result<Boolean>
    
    /**
     * 현재 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean
    
    /**
     * 저장된 사용자 ID 조회
     */
    fun getCurrentUserId(): String?
    
    /**
     * 저장된 사용자 이름 조회
     */
    fun getCurrentUserName(): String?
}