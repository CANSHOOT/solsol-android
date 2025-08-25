package com.heyyoung.solsol.feature.dutchpay.domain.usecase

import com.heyyoung.solsol.feature.dutchpay.domain.model.InvitationResult
import com.heyyoung.solsol.feature.dutchpay.domain.repository.DutchPayRepository
import javax.inject.Inject

/**
 * 정산 요청 알림 발송 UseCase
 * - 참여자들에게 정산 요청 푸시 알림/SMS/이메일 발송
 * - 발송 결과 및 실패 목록 반환
 */
class SendDutchPayInvitationsUseCase @Inject constructor(
    private val dutchPayRepository: DutchPayRepository
) {
    suspend operator fun invoke(
        groupId: Long,
        participantUserIds: List<String>,
        customMessage: String? = null
    ): Result<InvitationResult> {
        // 입력값 검증
        if (participantUserIds.isEmpty()) {
            return Result.failure(IllegalArgumentException("참여자 목록이 비어있습니다"))
        }
        
        if (participantUserIds.any { it.isBlank() }) {
            return Result.failure(IllegalArgumentException("유효하지 않은 사용자 ID가 포함되어 있습니다"))
        }
        
        // 중복 제거
        val uniqueParticipants = participantUserIds.distinct()
        
        return dutchPayRepository.sendDutchPayInvitations(
            groupId = groupId,
            participantUserIds = uniqueParticipants,
            message = customMessage
        )
    }
}