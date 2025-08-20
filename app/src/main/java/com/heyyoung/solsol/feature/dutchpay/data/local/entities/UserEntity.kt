package com.heyyoung.solsol.feature.dutchpay.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heyyoung.solsol.feature.dutchpay.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: Long,
    val userKey: String,
    val studentNumber: String,
    val name: String,
    val departmentId: Long,
    val departmentName: String,
    val phoneNumber: String,
    val email: String
)

fun UserEntity.toDomain() = User(
    userId = userId,
    userKey = userKey,
    studentNumber = studentNumber,
    name = name,
    departmentId = departmentId,
    departmentName = departmentName,
    phoneNumber = phoneNumber,
    email = email
)

fun User.toEntity() = UserEntity(
    userId = userId,
    userKey = userKey,
    studentNumber = studentNumber,
    name = name,
    departmentId = departmentId,
    departmentName = departmentName,
    phoneNumber = phoneNumber,
    email = email
)