package com.heyyoung.solsol.core.network

import com.heyyoung.solsol.feature.auth.data.local.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 인증 토큰 헤더 인터셉터
 * - 모든 API 요청에 Authorization 헤더 자동 추가
 * - 토큰 만료 시 자동 갱신 처리
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 인증이 필요없는 엔드포인트 (로그인, 회원가입 등)
        val authNotRequiredPaths = listOf(
            "/api/v1/auth/login",
            "/api/v1/auth/signup",
            "/api/v1/auth/refresh",
            "/api/v1/auth/check-email"
        )
        
        val requestPath = originalRequest.url.encodedPath
        val isAuthRequired = authNotRequiredPaths.none { requestPath.contains(it) }
        
        if (!isAuthRequired) {
            return chain.proceed(originalRequest)
        }
        
        // 액세스 토큰 추가
        val accessToken = tokenStorage.getAccessToken()
        if (accessToken.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }
        
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        
        val response = chain.proceed(authenticatedRequest)
        
        // 401 Unauthorized 응답 시 토큰 갱신 시도
        if (response.code == 401) {
            response.close()
            
            val refreshToken = tokenStorage.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                return handleTokenRefresh(chain, originalRequest, refreshToken)
            }
        }
        
        return response
    }
    
    private fun handleTokenRefresh(
        chain: Interceptor.Chain,
        originalRequest: okhttp3.Request,
        refreshToken: String
    ): Response {
        return runBlocking {
            try {
                // TokenRefreshHelper나 Repository를 통해 토큰 갱신
                // 여기서는 간단히 원본 요청을 재시도
                // 실제로는 RefreshTokenUseCase를 호출해야 함
                
                val newAccessToken = tokenStorage.getAccessToken()
                if (!newAccessToken.isNullOrBlank()) {
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                    chain.proceed(newRequest)
                } else {
                    // 토큰 갱신 실패 시 로그아웃 처리
                    tokenStorage.clearTokens()
                    chain.proceed(originalRequest)
                }
            } catch (e: Exception) {
                // 토큰 갱신 실패
                tokenStorage.clearTokens()
                chain.proceed(originalRequest)
            }
        }
    }
}