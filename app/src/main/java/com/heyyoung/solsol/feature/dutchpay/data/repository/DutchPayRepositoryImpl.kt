package com.heyyoung.solsol.feature.dutchpay.data.repository

import com.heyyoung.solsol.feature.dutchpay.data.local.DutchPayDao
import com.heyyoung.solsol.feature.dutchpay.data.local.entities.toDomain
import com.heyyoung.solsol.feature.dutchpay.data.local.entities.toEntity
import com.heyyoung.solsol.feature.dutchpay.data.remote.DutchPayApiService
import com.heyyoung.solsol.feature.dutchpay.data.remote.dto.*
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayGroup
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayParticipant
import com.heyyoung.solsol.feature.dutchpay.domain.repository.DutchPayRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DutchPayRepositoryImpl @Inject constructor(
    private val apiService: DutchPayApiService,
    private val dao: DutchPayDao
) : DutchPayRepository {

    override suspend fun createDutchPay(dutchPay: DutchPayGroup): Result<DutchPayGroup> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateDutchPayRequest(
                    organizerId = dutchPay.organizerId,
                    paymentId = dutchPay.paymentId,
                    groupName = dutchPay.groupName,
                    totalAmount = dutchPay.totalAmount,
                    participantUserIds = emptyList() // 생성 시에는 빈 리스트
                )
                
                val response = apiService.createDutchPay(request)
                val domain = response.toDomain()
                
                // 로컬 캐시에 저장
                dao.insertDutchPay(domain.toEntity())
                
                Result.success(domain)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getDutchPayById(groupId: Long): Result<DutchPayGroup> {
        return withContext(Dispatchers.IO) {
            try {
                // 먼저 로컬에서 조회
                val localEntity = dao.getDutchPayById(groupId)
                if (localEntity != null) {
                    val domain = localEntity.toDomain()
                    return@withContext Result.success(domain)
                }
                
                // 로컬에 없으면 서버에서 조회
                val response = apiService.getDutchPayById(groupId)
                val domain = response.toDomain()
                
                // 로컬 캐시에 저장
                dao.insertDutchPay(domain.toEntity())
                
                Result.success(domain)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun joinDutchPay(groupId: Long, userId: Long): Result<DutchPayParticipant> {
        return withContext(Dispatchers.IO) {
            try {
                val request = JoinDutchPayRequest(groupId, userId)
                val response = apiService.joinDutchPay(groupId, request)
                val domain = response.toDomain()
                
                Result.success(domain)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun payDutchPay(groupId: Long, participantId: Long): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val request = SendPaymentRequest(groupId, participantId)
                val response = apiService.sendPayment(groupId, request)
                
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getDutchPayHistory(userId: Long): Result<List<DutchPayGroup>> {
        return withContext(Dispatchers.IO) {
            try {
                // 서버에서 최신 데이터 조회
                val response = apiService.getDutchPayHistory(userId)
                val domains = response.map { it.toDomain() }
                
                // 로컬 캐시 업데이트
                domains.forEach { dao.insertDutchPay(it.toEntity()) }
                
                Result.success(domains)
            } catch (e: Exception) {
                // 네트워크 오류 시 로컬 데이터 반환
                try {
                    val asOrganizer = dao.getDutchPayHistoryAsOrganizer(userId)
                    val asParticipant = dao.getDutchPayHistoryAsParticipant(userId)
                    val localEntities = (asOrganizer + asParticipant)
                        .distinctBy { it.groupId }
                        .sortedByDescending { it.createdAt }
                    val domains = localEntities.map { it.toDomain() }
                    Result.success(domains)
                } catch (localE: Exception) {
                    Result.failure(e)
                }
            }
        }
    }

    override suspend fun getUserParticipations(userId: Long): Result<List<DutchPayParticipant>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserParticipations(userId)
                val domains = response.map { it.toDomain() }
                
                Result.success(domains)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}