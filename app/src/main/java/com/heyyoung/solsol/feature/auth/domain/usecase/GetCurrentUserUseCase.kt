package com.heyyoung.solsol.feature.auth.domain.usecase

import com.heyyoung.solsol.feature.auth.domain.model.AuthUser
import com.heyyoung.solsol.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 현재 사용자 정보 조회 UseCase
 * - 로그인 상태 확인
 * - 서버에서 최신 사용자 정보 조회
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<AuthUser> {
        if (!authRepository.isLoggedIn()) {
            return Result.failure(IllegalStateException("로그인이 필요합니다"))
        }
        
        return authRepository.getUserInfo()
    }
    
    /**
     * 저장된 사용자 ID만 조회 (네트워크 호출 없음)
     */
    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }
    
    /**
     * 저장된 사용자 이름만 조회 (네트워크 호출 없음)
     */
    fun getCurrentUserName(): String? {
        return authRepository.getCurrentUserName()
    }
}