package com.heyyoung.solsol.feature.dutchpay.data.local.entities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayParticipant
import com.heyyoung.solsol.feature.dutchpay.domain.model.JoinMethod
import com.heyyoung.solsol.feature.dutchpay.domain.model.ParticipantPaymentStatus
import java.time.LocalDateTime

@Entity(
    tableName = "dutch_pay_participants",
    foreignKeys = [
        ForeignKey(
            entity = DutchPayGroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DutchPayParticipantEntity(
    @PrimaryKey val participantId: Long,
    val groupId: Long,
    val userId: String,
    val joinMethod: String,
    val paymentStatus: String,
    val transferTransactionId: String?,
    val joinedAt: String,
    val paidAt: String?,
    val createdAt: String,
    val updatedAt: String
)

@RequiresApi(Build.VERSION_CODES.O)
fun DutchPayParticipantEntity.toDomain() = DutchPayParticipant(
    participantId = participantId,
    groupId = groupId,
    userId = userId,
    user = null, // DAO에서 join으로 채움
    joinMethod = JoinMethod.valueOf(joinMethod),
    paymentStatus = ParticipantPaymentStatus.valueOf(paymentStatus),
    transferTransactionId = transferTransactionId,
    joinedAt = LocalDateTime.parse(joinedAt),
    paidAt = paidAt?.let { LocalDateTime.parse(it) },
    createdAt = LocalDateTime.parse(createdAt),
    updatedAt = LocalDateTime.parse(updatedAt)
)

fun DutchPayParticipant.toEntity() = groupId?.let {
    DutchPayParticipantEntity(
        participantId = participantId ?: 0L,
        groupId = it,
        userId = userId,
        joinMethod = joinMethod.name,
        paymentStatus = paymentStatus.name,
        transferTransactionId = transferTransactionId,
        joinedAt = joinedAt.toString(),
        paidAt = paidAt?.toString(),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}