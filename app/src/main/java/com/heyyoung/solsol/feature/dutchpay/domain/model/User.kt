package com.heyyoung.solsol.feature.dutchpay.domain.model

data class User(
    val userId: Long,
    val userKey: String,
    val studentNumber: String,
    val name: String,
    val departmentId: Long,
    val departmentName: String,
    val phoneNumber: String,
    val email: String
)