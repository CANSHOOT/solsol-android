package com.heyyoung.solsol.feature.dutchpay.data.remote.dto

import com.heyyoung.solsol.feature.dutchpay.domain.model.User

data class UserDto(
    val userId: String, // API 스펙에 따라 이메일 형태
    val studentNumber: String,
    val name: String,
    val departmentId: Long,
    val departmentName: String,
    val councilId: Long,
    val accountNo: String,
    val accountBalance: Long,
    val councilOfficer: Boolean = false
)

fun UserDto.toDomain() = User(
    userId = userId, // 이메일 형태의 String ID 그대로 사용
    studentNumber = studentNumber,
    name = name,
    departmentId = departmentId,
    departmentName = departmentName,
    councilId = councilId,
    accountNo = accountNo,
    accountBalance = accountBalance,
    councilOfficer = councilOfficer
)