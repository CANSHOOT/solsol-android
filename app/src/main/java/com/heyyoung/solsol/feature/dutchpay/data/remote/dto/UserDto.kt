package com.heyyoung.solsol.feature.dutchpay.data.remote.dto

import com.heyyoung.solsol.feature.dutchpay.domain.model.User

data class UserDto(
    val userId: Long,
    val userKey: String,
    val studentNumber: String,
    val name: String,
    val departmentId: Long,
    val departmentName: String,
    val phoneNumber: String,
    val email: String
)

fun UserDto.toDomain() = User(
    userId = userId,
    userKey = userKey,
    studentNumber = studentNumber,
    name = name,
    departmentId = departmentId,
    departmentName = departmentName,
    phoneNumber = phoneNumber,
    email = email
)