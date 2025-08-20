package com.heyyoung.solsol.feature.dutchpay.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayGroup
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayStatus
import java.time.LocalDateTime

@Entity(tableName = "dutch_pay_groups")
data class DutchPayGroupEntity(
    @PrimaryKey val groupId: Long,
    val organizerId: Long,
    val paymentId: Long,
    val groupName: String,
    val totalAmount: Double,
    val participantCount: Int,
    val amountPerPerson: Double,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

fun DutchPayGroupEntity.toDomain() = DutchPayGroup(
    groupId = groupId,
    organizerId = organizerId,
    paymentId = paymentId,
    groupName = groupName,
    totalAmount = totalAmount,
    participantCount = participantCount,
    amountPerPerson = amountPerPerson,
    status = DutchPayStatus.valueOf(status),
    participants = emptyList(), // DAO에서 join으로 채움
    organizer = null, // DAO에서 join으로 채움
    createdAt = LocalDateTime.parse(createdAt),
    updatedAt = LocalDateTime.parse(updatedAt)
)

fun DutchPayGroup.toEntity() = DutchPayGroupEntity(
    groupId = groupId ?: 0L,
    organizerId = organizerId,
    paymentId = paymentId,
    groupName = groupName,
    totalAmount = totalAmount,
    participantCount = participantCount,
    amountPerPerson = amountPerPerson,
    status = status.name,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)