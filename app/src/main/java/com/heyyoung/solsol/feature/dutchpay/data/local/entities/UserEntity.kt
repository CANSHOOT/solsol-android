package com.heyyoung.solsol.feature.dutchpay.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heyyoung.solsol.feature.dutchpay.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String, // 이메일 형태의 ID
    val studentNumber: String,
    val name: String,
    val departmentId: Long,
    val departmentName: String,
    val councilId: Long,
    val accountNo: String,
    val accountBalance: Long,
    val councilOfficer: Boolean = false
)

fun UserEntity.toDomain() = User(
    userId = userId,
    studentNumber = studentNumber,
    name = name,
    departmentId = departmentId,
    departmentName = departmentName,
    councilId = councilId,
    accountNo = accountNo,
    accountBalance = accountBalance,
    councilOfficer = councilOfficer
)

fun User.toEntity() = UserEntity(
    userId = userId,
    studentNumber = studentNumber,
    name = name,
    departmentId = departmentId,
    departmentName = departmentName,
    councilId = councilId,
    accountNo = accountNo,
    accountBalance = accountBalance,
    councilOfficer = councilOfficer
)