package com.heyyoung.solsol.feature.settlement.domain.repository

import com.heyyoung.solsol.feature.settlement.domain.model.SettlementGroup
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementParticipant
import com.heyyoung.solsol.feature.settlement.domain.model.PaymentResult

interface SettlementRepository {
    suspend fun createSettlement(settlement: SettlementGroup): Result<SettlementGroup>
    suspend fun getSettlementById(groupId: Long): Result<SettlementGroup>
    suspend fun joinSettlement(groupId: Long, userId: String): Result<SettlementParticipant>
    suspend fun paySettlement(groupId: Long, accountNumber: String, transactionSummary: String): Result<PaymentResult>
    suspend fun getSettlementHistory(userId: Long): Result<List<SettlementGroup>>
    suspend fun getUserParticipations(userId: Long): Result<List<SettlementParticipant>>
}