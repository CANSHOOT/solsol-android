package com.heyyoung.solsol.feature.settlement.domain.model

import java.time.LocalDateTime

/**
 * 정산 그룹 도메인 모델
 * - 정산의 핵심 정보를 담는 데이터 클래스
 * - ERD의 DUTCH_PAY_GROUPS 테이블과 매핑
 */
data class SettlementGroup(
    val groupId: Long? = null,
    val organizerId: String, // 이메일 형태의 사용자 ID
    val paymentId: Long,
    val groupName: String,
    val totalAmount: Double,
    val participantCount: Int,
    val amountPerPerson: Double,
    val status: SettlementStatus,
    val participants: List<SettlementParticipant> = emptyList(),
    val organizer: User? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    val completedParticipants: Int
        get() = participants.count { it.paymentStatus == ParticipantPaymentStatus.COMPLETED }
    
    val remainingAmount: Double
        get() = participants.filter { it.paymentStatus != ParticipantPaymentStatus.COMPLETED }
            .size * amountPerPerson
    
    val isCompleted: Boolean
        get() = status == SettlementStatus.COMPLETED
}

enum class SettlementStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}