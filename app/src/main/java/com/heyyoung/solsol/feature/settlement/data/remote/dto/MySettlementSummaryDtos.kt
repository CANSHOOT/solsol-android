package com.heyyoung.solsol.feature.settlement.data.remote.dto

import java.math.BigDecimal

data class MySettlementSummaryResponseDto(
    val payables: List<PayableItemDto> = emptyList(),
    val receivables: List<ReceivableItemDto> = emptyList(),
    val payableTotal: BigDecimal = BigDecimal.ZERO,
    val receivableTotal: BigDecimal = BigDecimal.ZERO
)

data class PayableItemDto(
    val groupId: Long,
    val groupName: String,
    val organizerUserId: String,
    val organizerName: String,
    val settlementAmount: BigDecimal,
    val status: String // "진행중" | "완료" (or "PENDING" | "COMPLETED")
)

data class ReceivableItemDto(
    val groupId: Long,
    val groupName: String,
    val userId: String,     // 나에게 줄 사람
    val userName: String,   // 표시 이름
    val settlementAmount: BigDecimal,
    val status: String? = null
)
