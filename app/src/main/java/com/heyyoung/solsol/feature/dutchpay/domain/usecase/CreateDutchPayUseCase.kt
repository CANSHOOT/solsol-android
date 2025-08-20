package com.heyyoung.solsol.feature.dutchpay.domain.usecase

/**
 * 더치페이 생성 유스케이스
 * - 비즈니스 로직: 입력값 검증, 1인당 금액 계산 (원 단위 올림)
 * - 백엔드 API 호출하여 더치페이 그룹 생성
 */
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayGroup
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayStatus
import com.heyyoung.solsol.feature.dutchpay.domain.repository.DutchPayRepository
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.ceil

class CreateDutchPayUseCase @Inject constructor(
    private val dutchPayRepository: DutchPayRepository
) {
    suspend operator fun invoke(
        organizerId: Long,
        paymentId: Long,
        groupName: String,
        totalAmount: Double,
        participantUserIds: List<Long>
    ): Result<DutchPayGroup> {
        if (groupName.isBlank()) {
            return Result.failure(IllegalArgumentException("그룹명을 입력해주세요"))
        }
        
        if (totalAmount <= 0) {
            return Result.failure(IllegalArgumentException("결제 금액이 올바르지 않습니다"))
        }
        
        if (participantUserIds.isEmpty()) {
            return Result.failure(IllegalArgumentException("참여자를 선택해주세요"))
        }
        
        val participantCount = participantUserIds.size + 1 // 결제자 포함
        val amountPerPerson = ceil(totalAmount / participantCount * 100) / 100 // 원 단위 올림
        
        val dutchPayGroup = DutchPayGroup(
            organizerId = organizerId,
            paymentId = paymentId,
            groupName = groupName,
            totalAmount = totalAmount,
            participantCount = participantCount,
            amountPerPerson = amountPerPerson,
            status = DutchPayStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        return dutchPayRepository.createDutchPay(dutchPayGroup)
    }
}