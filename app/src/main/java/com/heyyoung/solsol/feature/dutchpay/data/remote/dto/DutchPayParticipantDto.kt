package com.heyyoung.solsol.feature.dutchpay.data.remote.dto

import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayParticipant
import com.heyyoung.solsol.feature.dutchpay.domain.model.JoinMethod
import com.heyyoung.solsol.feature.dutchpay.domain.model.ParticipantPaymentStatus
import java.time.LocalDateTime

data class DutchPayParticipantDto(
    val participantId: Long,
    val groupId: Long,
    val userId: Long,
    val user: UserDto?,
    val joinMethod: String,
    val paymentStatus: String,
    val transferTransactionId: String?,
    val joinedAt: String,
    val paidAt: String?,
    val createdAt: String,
    val updatedAt: String
)

fun DutchPayParticipantDto.toDomain() = DutchPayParticipant(
    participantId = participantId,
    groupId = groupId,
    userId = userId,
    user = user?.toDomain(),
    joinMethod = JoinMethod.valueOf(joinMethod),
    paymentStatus = ParticipantPaymentStatus.valueOf(paymentStatus),
    transferTransactionId = transferTransactionId,
    joinedAt = LocalDateTime.parse(joinedAt),
    paidAt = paidAt?.let { LocalDateTime.parse(it) },
    createdAt = LocalDateTime.parse(createdAt),
    updatedAt = LocalDateTime.parse(updatedAt)
)