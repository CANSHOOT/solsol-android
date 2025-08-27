package com.heyyoung.solsol.feature.settlement.data.remote.dto

import android.os.Build
import androidx.annotation.RequiresApi
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementParticipant
import com.heyyoung.solsol.feature.settlement.domain.model.JoinMethod
import com.heyyoung.solsol.feature.settlement.domain.model.ParticipantPaymentStatus
import java.time.LocalDateTime

data class SettlementParticipantDto(
    val participantId: Long,
    val groupId: Long,
    val userId: String,
    val userName: String,
    val joinMethod: String,
    val paymentStatus: String,
    val transferTransactionId: String?,
    val joinedAt: String,
    val paidAt: String?
)

@RequiresApi(Build.VERSION_CODES.O)
fun SettlementParticipantDto.toDomain() = SettlementParticipant(
    participantId = participantId,
    groupId = groupId,
    userId = userId,
    user = null, // 새 API에서는 별도 user 객체가 없음
    joinMethod = JoinMethod.valueOf(joinMethod),
    paymentStatus = ParticipantPaymentStatus.valueOf(paymentStatus),
    transferTransactionId = transferTransactionId,
    joinedAt = LocalDateTime.parse(joinedAt),
    paidAt = paidAt?.let { LocalDateTime.parse(it) },
    createdAt = LocalDateTime.now(), // API에 없는 필드
    updatedAt = LocalDateTime.now() // API에 없는 필드
)