package com.heyyoung.solsol.feature.auth.domain.usecase

import com.heyyoung.solsol.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 로그아웃 UseCase
 * - 서버에 로그아웃 요청
 * - 로컬 토큰 삭제
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return authRepository.logout()
    }
}