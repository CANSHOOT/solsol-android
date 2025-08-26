package com.heyyoung.solsol.feature.settlement.domain.model

import java.time.LocalDateTime

data class SettlementParticipant(
    val participantId: Long? = null,
    val groupId: Long?,
    val userId: String, // 이메일 형태의 사용자 ID
    val user: User? = null,
    val joinMethod: JoinMethod,
    val paymentStatus: ParticipantPaymentStatus,
    val transferTransactionId: String? = null,
    val joinedAt: LocalDateTime,
    val paidAt: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class JoinMethod {
    SEARCH,
    AIRDROP
}

enum class ParticipantPaymentStatus {
    PENDING,
    COMPLETED,
    FAILED
}