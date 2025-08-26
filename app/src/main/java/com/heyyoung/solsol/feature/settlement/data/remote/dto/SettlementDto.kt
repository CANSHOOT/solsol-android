package com.heyyoung.solsol.feature.settlement.data.remote.dto

import android.os.Build
import androidx.annotation.RequiresApi
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementGroup
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementStatus
import com.heyyoung.solsol.feature.settlement.domain.model.InvitationResult
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class CreateSettlementRequest(
    val paymentId: Long,
    val groupName: String,
    val totalAmount: Double,
    val participantCount: Int,
    val participantUserIds: List<String> = emptyList() // 참여자 이메일 목록
)

data class SettlementGroupDto(
    val groupId: Long,
    val organizerId: String,
    val organizerName: String,
    val paymentId: Long,
    val groupName: String,
    val totalAmount: Double,
    val participantCount: Int,
    val amountPerPerson: Double,
    val status: String,
    val participants: List<SettlementParticipantDto>,
    val createdAt: String,
    val updatedAt: String
)

data class JoinSettlementRequest(
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

data class SendInvitationsRequest(
    val participantUserIds: List<String>,
    val message: String? = null
)

data class InvitationResultDto(
    val groupId: Long,
    val sentCount: Int,
    val failedCount: Int,
    val failedUserIds: List<String>,
    val message: String
)

@RequiresApi(Build.VERSION_CODES.O)
fun SettlementGroupDto.toDomain() = SettlementGroup(
    groupId = groupId,
    organizerId = organizerId,
    paymentId = paymentId,
    groupName = groupName,
    totalAmount = totalAmount,
    participantCount = participantCount,
    amountPerPerson = amountPerPerson,
    status = SettlementStatus.valueOf(status),
    participants = participants.map { it.toDomain() },
    organizer = null, // 새 API에서는 별도 organizer 필드가 없음
    createdAt = parseDateTime(createdAt),
    updatedAt = parseDateTime(updatedAt)
)

fun InvitationResultDto.toDomain() = InvitationResult(
    groupId = groupId,
    sentCount = sentCount,
    failedCount = failedCount,
    failedUserIds = failedUserIds,
    message = message
)

@RequiresApi(Build.VERSION_CODES.O)
private fun parseDateTime(dateTimeString: String): LocalDateTime {
    return try {
        // 먼저 ISO 형식(Z가 있는 UTC) 파싱 시도
        ZonedDateTime.parse(dateTimeString).toLocalDateTime()
    } catch (e: Exception) {
        try {
            // ISO 형식 파싱 실패 시, LocalDateTime 직접 파싱 시도
            LocalDateTime.parse(dateTimeString)
        } catch (e2: Exception) {
            try {
                // 사용자 정의 형식으로 파싱 시도
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
                LocalDateTime.parse(dateTimeString.replace("Z", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"))
            } catch (e3: Exception) {
                // 모든 파싱 실패 시 현재 시간으로 대체
                LocalDateTime.now()
            }
        }
    }
}