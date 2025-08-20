package com.heyyoung.solsol.feature.dutchpay.domain.model

data class Account(
    val accountId: Long,
    val userId: Long,
    val accountNumber: String,
    val accountType: AccountType,
    val balance: Double
)

enum class AccountType {
    PERSONAL,
    STUDENT_COUNCIL
}