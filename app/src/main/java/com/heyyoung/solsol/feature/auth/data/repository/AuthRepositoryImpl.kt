package com.heyyoung.solsol.feature.auth.data.repository

import com.heyyoung.solsol.feature.auth.data.local.TokenStorage
import com.heyyoung.solsol.feature.auth.data.remote.AuthApiService
import com.heyyoung.solsol.feature.auth.data.remote.dto.*
import com.heyyoung.solsol.feature.auth.domain.model.AuthTokens
import com.heyyoung.solsol.feature.auth.domain.model.AuthUser
import com.heyyoung.solsol.feature.auth.domain.model.SignUpResult
import com.heyyoung.solsol.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 인증 Repository 구현체
 * - API 통신과 로컬 토큰 저장 관리
 * - 토큰 자동 저장 및 관리
 */
class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val tokenStorage: TokenStorage
) : AuthRepository {
    
    override suspend fun login(email: String): Result<AuthTokens> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email = email)
                val response = apiService.login(request)
                val tokens = response.toDomain()
                
                // 토큰 로컬 저장
                tokenStorage.saveTokens(tokens)
                
                Result.success(tokens)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun signUp(
        email: String,
        name: String,
        studentNumber: String,
        departmentName: String,
        councilId: Long,
        isCouncilOfficer: Boolean
    ): Result<SignUpResult> {
        return withContext(Dispatchers.IO) {
            try {
                val request = SignUpRequest(
                    email = email,
                    name = name,
                    studentNumber = studentNumber,
                    departmentName = departmentName,
                    councilId = councilId,
                    isCouncilOfficer = isCouncilOfficer
                )
                val response = apiService.signUp(request)
                val signUpResult = response.toDomain()
                
                // 회원가입 성공 시 토큰 저장
                val tokens = AuthTokens(
                    accessToken = signUpResult.accessToken,
                    refreshToken = signUpResult.refreshToken,
                    userId = signUpResult.userId,
                    name = signUpResult.name
                )
                tokenStorage.saveTokens(tokens)
                
                Result.success(signUpResult)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun refreshToken(refreshToken: String): Result<AuthTokens> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RefreshTokenRequest(refreshToken = refreshToken)
                val response = apiService.refreshToken(request)
                val tokens = response.toDomain()
                
                // 갱신된 토큰 저장
                tokenStorage.saveTokens(tokens)
                
                Result.success(tokens)
            } catch (e: Exception) {
                // 리프레시 토큰이 만료된 경우 로컬 토큰 삭제
                tokenStorage.clearTokens()
                Result.failure(e)
            }
        }
    }
    
    override suspend fun logout(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = tokenStorage.getRefreshToken()
                if (!refreshToken.isNullOrBlank()) {
                    val request = RefreshTokenRequest(refreshToken = refreshToken)
                    apiService.logout(request)
                }
                
                // 로컬 토큰 삭제
                tokenStorage.clearTokens()
                
                Result.success(true)
            } catch (e: Exception) {
                // API 호출 실패해도 로컬 토큰은 삭제
                tokenStorage.clearTokens()
                Result.success(true)
            }
        }
    }
    
    override suspend fun getUserInfo(): Result<AuthUser> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserInfo()
                val user = response.toDomain()
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun checkEmailExists(email: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val exists = apiService.checkEmailExists(email)
                Result.success(exists)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override fun isLoggedIn(): Boolean {
        return tokenStorage.isLoggedIn()
    }
    
    override fun getCurrentUserId(): String? {
        return tokenStorage.getUserId()
    }
    
    override fun getCurrentUserName(): String? {
        return tokenStorage.getUserName()
    }
}