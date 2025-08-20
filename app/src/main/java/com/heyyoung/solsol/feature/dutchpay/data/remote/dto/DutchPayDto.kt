package com.heyyoung.solsol.feature.dutchpay.data.remote.dto

import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayGroup
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayStatus
import java.time.LocalDateTime

data class CreateDutchPayRequest(
    val organizerId: Long,
    val paymentId: Long,
    val groupName: String,
    val totalAmount: Double,
    val participantUserIds: List<Long>
)

data class DutchPayGroupDto(
    val groupId: Long,
    val organizerId: Long,
    val paymentId: Long,
    val groupName: String,
    val totalAmount: Double,
    val participantCount: Int,
    val amountPerPerson: Double,
    val status: String,
    val participants: List<DutchPayParticipantDto>,
    val organizer: UserDto?,
    val createdAt: String,
    val updatedAt: String
)

data class JoinDutchPayRequest(
    val groupId: Long,
    val userId: Long
)

data class SendPaymentRequest(
    val groupId: Long,
    val participantId: Long
)

fun DutchPayGroupDto.toDomain() = DutchPayGroup(
    groupId = groupId,
    organizerId = organizerId,
    paymentId = paymentId,
    groupName = groupName,
    totalAmount = totalAmount,
    participantCount = participantCount,
    amountPerPerson = amountPerPerson,
    status = DutchPayStatus.valueOf(status),
    participants = participants.map { it.toDomain() },
    organizer = organizer?.toDomain(),
    createdAt = LocalDateTime.parse(createdAt),
    updatedAt = LocalDateTime.parse(updatedAt)
)