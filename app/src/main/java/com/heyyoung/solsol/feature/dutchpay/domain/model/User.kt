package com.heyyoung.solsol.feature.dutchpay.domain.model

data class User(
    val userId: String, // 이메일 형태의 ID
    val studentNumber: String,
    val name: String,
    val departmentId: Long,
    val departmentName: String,
    val councilId: Long,
    val accountNo: String,
    val accountBalance: Long,
    val councilOfficer: Boolean = false
)