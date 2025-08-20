package com.heyyoung.solsol.feature.dutchpay.domain.model

import java.time.LocalDateTime

data class DutchPayGroup(
    val groupId: Long? = null,
    val organizerId: Long,
    val paymentId: Long,
    val groupName: String,
    val totalAmount: Double,
    val participantCount: Int,
    val amountPerPerson: Double,
    val status: DutchPayStatus,
    val participants: List<DutchPayParticipant> = emptyList(),
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
        get() = status == DutchPayStatus.COMPLETED
}

enum class DutchPayStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}