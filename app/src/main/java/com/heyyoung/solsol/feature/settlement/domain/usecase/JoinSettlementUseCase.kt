package com.heyyoung.solsol.feature.settlement.domain.usecase

import com.heyyoung.solsol.feature.settlement.domain.model.SettlementParticipant
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementStatus
import com.heyyoung.solsol.feature.settlement.domain.repository.SettlementRepository
import javax.inject.Inject

class JoinSettlementUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {
    suspend operator fun invoke(
        groupId: Long,
        userId: String
    ): Result<SettlementParticipant> {
        // 정산 그룹 존재 여부 및 상태 확인
        val settlementResult = settlementRepository.getSettlementById(groupId)
        if (settlementResult.isFailure) {
            return Result.failure(settlementResult.exceptionOrNull() ?: Exception("정산을 찾을 수 없습니다"))
        }
        
        val settlement = settlementResult.getOrNull()!!
        
        if (settlement.status != SettlementStatus.ACTIVE) {
            return Result.failure(IllegalStateException("참여할 수 없는 정산입니다"))
        }
        
        if (settlement.organizerId == userId) {
            return Result.failure(IllegalArgumentException("결제자는 참여할 수 없습니다"))
        }
        
        // 이미 참여한 사용자인지 확인
        val alreadyJoined = settlement.participants.any { it.userId == userId }
        if (alreadyJoined) {
            return Result.failure(IllegalArgumentException("이미 참여한 정산입니다"))
        }
        
        return settlementRepository.joinSettlement(groupId, userId)
    }
}