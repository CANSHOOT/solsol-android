package com.heyyoung.solsol.feature.settlement.domain.repository

import com.heyyoung.solsol.feature.settlement.domain.model.SettlementGroup
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementParticipant
import com.heyyoung.solsol.feature.settlement.domain.model.PaymentResult
import com.heyyoung.solsol.feature.settlement.domain.model.InvitationResult

interface SettlementRepository {
    suspend fun createSettlement(settlement: SettlementGroup): Result<SettlementGroup>
    suspend fun getSettlementById(groupId: Long): Result<SettlementGroup>
    suspend fun joinSettlement(groupId: Long, userId: Long): Result<SettlementParticipant>
    suspend fun paySettlement(groupId: Long, accountNumber: String, transactionSummary: String): Result<PaymentResult>
    suspend fun getSettlementHistory(userId: Long): Result<List<SettlementGroup>>
    suspend fun getUserParticipations(userId: Long): Result<List<SettlementParticipant>>
    
    // 정산 요청 알림 발송
    suspend fun sendSettlementInvitations(
        groupId: Long, 
        participantUserIds: List<String>, 
        message: String? = null
    ): Result<InvitationResult>
}