package com.heyyoung.solsol.feature.dutchpay.data.remote.dto

import com.heyyoung.solsol.feature.dutchpay.domain.model.User

data class UserDto(
    val userId: String, // API 스펙에 따라 이메일 형태로 변경
    val studentNumber: String,
    val name: String,
    val departmentId: Long,
    val departmentName: String,
    val councilId: Long,
    val isCouncilOfficer: Boolean
)

fun UserDto.toDomain() = User(
    userId = userId.hashCode().toLong(), // String을 Long으로 변환
    studentNumber = studentNumber,
    name = name,
    email = userId, // userId가 이메일 형태
    departmentName = departmentName
)