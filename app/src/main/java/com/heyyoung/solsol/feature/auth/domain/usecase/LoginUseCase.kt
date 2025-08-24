package com.heyyoung.solsol.feature.auth.domain.usecase

import com.heyyoung.solsol.feature.auth.domain.model.AuthTokens
import com.heyyoung.solsol.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 로그인 UseCase
 * - 이메일 형식 검증
 * - 로그인 API 호출
 * - 토큰 저장 처리
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<AuthTokens> {
        // 입력값 검증
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("이메일을 입력해주세요"))
        }
        
        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("올바른 이메일 형식을 입력해주세요"))
        }
        
        return authRepository.login(email)
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}