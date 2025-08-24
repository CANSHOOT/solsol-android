package com.heyyoung.solsol.feature.dutchpay.domain.model

data class User(
    val userId: Long,
    val studentNumber: String,
    val name: String,
    val email: String,
    val departmentName: String
)