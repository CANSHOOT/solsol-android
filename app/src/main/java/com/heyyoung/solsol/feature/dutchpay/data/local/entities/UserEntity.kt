package com.heyyoung.solsol.feature.dutchpay.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heyyoung.solsol.feature.dutchpay.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: Long,
    val studentNumber: String,
    val name: String,
    val email: String,
    val departmentName: String
)

fun UserEntity.toDomain() = User(
    userId = userId,
    studentNumber = studentNumber,
    name = name,
    email = email,
    departmentName = departmentName
)

fun User.toEntity() = UserEntity(
    userId = userId,
    studentNumber = studentNumber,
    name = name,
    email = email,
    departmentName = departmentName
)