package com.heyyoung.solsol.feature.dutchpay.data.remote.dto

import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayGroup
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayStatus
import java.time.LocalDateTime

data class CreateDutchPayRequest(
    val paymentId: Long,
    val groupName: String,
    val totalAmount: Double,
    val participantCount: Int
)

data class DutchPayGroupDto(
    val groupId: Long,
    val organizerId: String,
    val organizerName: String,
    val paymentId: Long,
    val groupName: String,
    val totalAmount: Double,
    val participantCount: Int,
    val amountPerPerson: Double,
    val status: String,
    val participants: List<DutchPayParticipantDto>,
    val createdAt: String,
    val updatedAt: String
)

data class JoinDutchPayRequest(
    val joinMethod: String = "SEARCH"
)

data class SendPaymentRequest(
    val accountNumber: String,
    val transactionSummary: String
)

data class PaymentResultDto(
    val transactionId: String?,
    val amount: Double?,
    val status: String,
    val message: String
)

fun DutchPayGroupDto.toDomain() = DutchPayGroup(
    groupId = groupId,
    organizerId = organizerId.hashCode().toLong(), // String을 Long으로 변환
    paymentId = paymentId,
    groupName = groupName,
    totalAmount = totalAmount,
    participantCount = participantCount,
    amountPerPerson = amountPerPerson,
    status = DutchPayStatus.valueOf(status),
    participants = participants.map { it.toDomain() },
    organizer = null, // 새 API에서는 별도 organizer 필드가 없음
    createdAt = LocalDateTime.parse(createdAt),
    updatedAt = LocalDateTime.parse(updatedAt)
)