package com.heyyoung.solsol.feature.dutchpay.domain.model

import java.time.LocalDateTime

data class DutchPayParticipant(
    val participantId: Long? = null,
    val groupId: Long,
    val userId: Long,
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