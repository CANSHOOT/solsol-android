package com.heyyoung.solsol.feature.settlement.data.repository

/**
 * 정산 Repository 구현체
 * - 온라인: 서버에서 최신 데이터 가져와 Room에 캐시
 * - 오프라인: Room 캐시 데이터 사용
 * - 네트워크 오류 시 자동으로 로컬 데이터로 fallback
 */
import android.os.Build
import androidx.annotation.RequiresApi
import com.heyyoung.solsol.feature.settlement.data.remote.SettlementApiService
import com.heyyoung.solsol.feature.settlement.data.remote.dto.*
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementGroup
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementParticipant
import com.heyyoung.solsol.feature.settlement.domain.model.PaymentResult
import com.heyyoung.solsol.feature.settlement.domain.repository.SettlementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject

class SettlementRepositoryImpl @Inject constructor(
    private val apiService: SettlementApiService
) : SettlementRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createSettlement(settlement: SettlementGroup): Result<SettlementGroup> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateSettlementRequest(
                    paymentId = settlement.paymentId,
                    groupName = settlement.groupName,
                    totalAmount = settlement.totalAmount,
                    participantCount = settlement.participantCount,
                    participantUserIds = settlement.participants.map { it.userId }
                )
                
                val response = apiService.createSettlement(request)
                val domain = response.toDomain()
                
                Result.success(domain)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getSettlementById(groupId: Long): Result<SettlementGroup> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSettlementById(groupId)
                val domain = response.toDomain()
                
                Result.success(domain)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun joinSettlement(groupId: Long, userId: String, amount: BigDecimal): Result<SettlementParticipant> {
        return withContext(Dispatchers.IO) {
            try {
                val request = JoinSettlementRequest(joinMethod = "SEARCH", amount) // 기본값 SEARCH 사용
                val response = apiService.joinSettlement(groupId, userId, request)
                val domain = response.toDomain()
                
                Result.success(domain)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun paySettlement(groupId: Long, accountNumber: String, transactionSummary: String): Result<PaymentResult> {
        return withContext(Dispatchers.IO) {
            try {
                val request = SendPaymentRequest(accountNumber, transactionSummary)
                val response = apiService.sendPayment(groupId, request)
                
                val paymentResult = PaymentResult(
                    transactionId = response.transactionId,
                    amount = response.amount,
                    status = response.status,
                    message = response.message
                )
                
                Result.success(paymentResult)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getSettlementHistory(userId: Long): Result<List<SettlementGroup>> {
        return withContext(Dispatchers.IO) {
            try {
                val userIdString = userId.toString()
                val response = apiService.getSettlementHistory(userIdString)
                val domains = response.map { it.toDomain() }
                
                Result.success(domains)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getUserParticipations(userId: Long): Result<List<SettlementParticipant>> {
        return withContext(Dispatchers.IO) {
            try {
                // userId를 String으로 변환
                val userIdString = userId.toString()
                val response = apiService.getUserParticipations(userIdString)
                val domains = response.map { it.toDomain() }
                
                Result.success(domains)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}