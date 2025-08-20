package com.heyyoung.solsol.feature.dutchpay.domain.usecase

import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayParticipant
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayStatus
import com.heyyoung.solsol.feature.dutchpay.domain.repository.DutchPayRepository
import javax.inject.Inject

class JoinDutchPayUseCase @Inject constructor(
    private val dutchPayRepository: DutchPayRepository
) {
    suspend operator fun invoke(
        groupId: Long,
        userId: Long
    ): Result<DutchPayParticipant> {
        // 더치페이 그룹 존재 여부 및 상태 확인
        val dutchPayResult = dutchPayRepository.getDutchPayById(groupId)
        if (dutchPayResult.isFailure) {
            return Result.failure(dutchPayResult.exceptionOrNull() ?: Exception("더치페이를 찾을 수 없습니다"))
        }
        
        val dutchPay = dutchPayResult.getOrNull()!!
        
        if (dutchPay.status != DutchPayStatus.ACTIVE) {
            return Result.failure(IllegalStateException("참여할 수 없는 더치페이입니다"))
        }
        
        if (dutchPay.organizerId == userId) {
            return Result.failure(IllegalArgumentException("결제자는 참여할 수 없습니다"))
        }
        
        // 이미 참여한 사용자인지 확인
        val alreadyJoined = dutchPay.participants.any { it.userId == userId }
        if (alreadyJoined) {
            return Result.failure(IllegalArgumentException("이미 참여한 더치페이입니다"))
        }
        
        return dutchPayRepository.joinDutchPay(groupId, userId)
    }
}