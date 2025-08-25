package com.heyyoung.solsol.feature.dutchpay.domain.repository

import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayGroup
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayParticipant
import com.heyyoung.solsol.feature.dutchpay.domain.model.PaymentResult
import com.heyyoung.solsol.feature.dutchpay.domain.model.InvitationResult

interface DutchPayRepository {
    suspend fun createDutchPay(dutchPay: DutchPayGroup): Result<DutchPayGroup>
    suspend fun getDutchPayById(groupId: Long): Result<DutchPayGroup>
    suspend fun joinDutchPay(groupId: Long, userId: Long): Result<DutchPayParticipant>
    suspend fun payDutchPay(groupId: Long, accountNumber: String, transactionSummary: String): Result<PaymentResult>
    suspend fun getDutchPayHistory(userId: Long): Result<List<DutchPayGroup>>
    suspend fun getUserParticipations(userId: Long): Result<List<DutchPayParticipant>>
    
    // 정산 요청 알림 발송
    suspend fun sendDutchPayInvitations(
        groupId: Long, 
        participantUserIds: List<String>, 
        message: String? = null
    ): Result<InvitationResult>
}