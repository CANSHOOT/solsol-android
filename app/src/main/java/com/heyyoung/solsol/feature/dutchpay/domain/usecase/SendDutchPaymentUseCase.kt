package com.heyyoung.solsol.feature.dutchpay.domain.usecase

/**
 * 더치페이 송금 유스케이스
 * - 참여자가 결제자에게 1인당 분담금을 송금
 * - 백엔드에서 금융 API(계좌 이체) 호출하여 실제 송금 처리
 */
import com.heyyoung.solsol.feature.dutchpay.domain.model.ParticipantPaymentStatus
import com.heyyoung.solsol.feature.dutchpay.domain.repository.DutchPayRepository
import javax.inject.Inject

class SendDutchPaymentUseCase @Inject constructor(
    private val dutchPayRepository: DutchPayRepository
) {
    suspend operator fun invoke(
        groupId: Long,
        participantId: Long
    ): Result<Boolean> {
        // 더치페이 정보 조회
        val dutchPayResult = dutchPayRepository.getDutchPayById(groupId)
        if (dutchPayResult.isFailure) {
            return Result.failure(dutchPayResult.exceptionOrNull() ?: Exception("더치페이를 찾을 수 없습니다"))
        }
        
        val dutchPay = dutchPayResult.getOrNull()!!
        
        // 참여자 정보 확인
        val participant = dutchPay.participants.find { it.participantId == participantId }
            ?: return Result.failure(IllegalArgumentException("참여자 정보를 찾을 수 없습니다"))
        
        // 이미 송금 완료인지 확인
        if (participant.paymentStatus == ParticipantPaymentStatus.COMPLETED) {
            return Result.failure(IllegalStateException("이미 송금이 완료되었습니다"))
        }
        
        // 백엔드에서 금융 API 호출하여 송금 처리
        return dutchPayRepository.payDutchPay(groupId, participantId)
    }
}