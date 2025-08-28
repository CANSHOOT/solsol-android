package com.heyyoung.solsol.feature.settlement.domain.usecase

/**
 * 정산 생성 유스케이스
 * - 비즈니스 로직: 입력값 검증, 1인당 금액 계산 (원 단위 올림)
 * - 백엔드 API 호출하여 정산 그룹 생성
 */
import android.os.Build
import androidx.annotation.RequiresApi
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementGroup
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementStatus
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementParticipant
import com.heyyoung.solsol.feature.settlement.domain.model.ParticipantPaymentStatus
import com.heyyoung.solsol.feature.settlement.domain.model.JoinMethod
import com.heyyoung.solsol.feature.settlement.domain.repository.SettlementRepository
import android.util.Log
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.ceil

class CreateSettlementGameUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {

    companion object {
        private const val TAG = "CreateSettlementUseCase"
    }
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(
        organizerId: String, // 이메일 형태의 사용자 ID
        paymentId: Long,
        groupName: String,
        totalAmount: Double,
        participantUserIds: List<String> // 이메일 형태의 사용자 ID 목록
    ): Result<SettlementGroup> {
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

        val participantList = participantUserIds.map { userId ->
            SettlementParticipant(
                participantId = null, // 서버에서 생성됨
                groupId = null, // 서버에서 설정됨
                userId = userId,
                user = null,
                joinMethod = JoinMethod.SEARCH, // 검색으로 초대된 것으로 설정
                paymentStatus = ParticipantPaymentStatus.PENDING,
                transferTransactionId = null,
                joinedAt = LocalDateTime.now(),
                paidAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }

        val settlementGroup = SettlementGroup(
            organizerId = organizerId,
            paymentId = paymentId,
            groupName = groupName,
            totalAmount = totalAmount,
            participantCount = participantCount,
            amountPerPerson = amountPerPerson,
            status = SettlementStatus.ACTIVE,
            participants = participantList,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // 1. 정산 그룹 생성
        val createResult = settlementRepository.createSettlementGame(organizerId, settlementGroup)

        return createResult.fold(
            onSuccess = { createdSettlement ->
                Log.d(TAG, "✅ 정산 그룹 생성 성공: groupId=${createdSettlement.groupId}")
                Log.d(TAG, "📋 참여자들은 join API를 통해 개별적으로 그룹에 참여할 예정")

                Result.success(createdSettlement)
            },
            onFailure = { error ->
                Log.e(TAG, "❌ 정산 그룹 생성 실패: ${error.message}")
                Result.failure(error)
            }
        )
    }
}