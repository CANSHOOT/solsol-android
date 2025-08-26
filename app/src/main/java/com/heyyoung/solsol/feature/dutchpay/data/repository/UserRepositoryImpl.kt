package com.heyyoung.solsol.feature.dutchpay.data.repository

import com.heyyoung.solsol.feature.dutchpay.data.local.DutchPayDao
import com.heyyoung.solsol.feature.dutchpay.data.local.entities.toDomain
import com.heyyoung.solsol.feature.dutchpay.data.local.entities.toEntity
import com.heyyoung.solsol.feature.dutchpay.data.remote.DutchPayApiService
import com.heyyoung.solsol.feature.dutchpay.data.remote.dto.toDomain
import com.heyyoung.solsol.feature.dutchpay.domain.model.User
import com.heyyoung.solsol.feature.dutchpay.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: DutchPayApiService,
    private val dao: DutchPayDao
) : UserRepository {

    override suspend fun searchUsers(query: String): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                // 서버에서 검색
                val response = apiService.searchUsers(query)
                val domains = response.map { it.toDomain() }
                
                // 로컬 캐시에 저장
                dao.insertUsers(domains.map { it.toEntity() })
                
                Result.success(domains)
            } catch (e: Exception) {
                // 네트워크 오류 시 로컬에서 검색
                try {
                    val localEntities = dao.searchUsers(query)
                    val domains = localEntities.map { it.toDomain() }
                    Result.success(domains)
                } catch (localE: Exception) {
                    Result.failure(e)
                }
            }
        }
    }

    override suspend fun getUserById(userId: Long): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // 먼저 로컬에서 조회
                val localEntity = dao.getUserById(userId)
                if (localEntity != null) {
                    return@withContext Result.success(localEntity.toDomain())
                }
                
                // 로컬에 없으면 서버에서 조회 (userId를 String으로 변환)
                val userIdString = userId.toString()
                val response = apiService.getUserById(userIdString)
                val domain = response.toDomain()
                
                // 로컬 캐시에 저장
                dao.insertUsers(listOf(domain.toEntity()))
                
                Result.success(domain)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getUserByStringId(userId: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // String userId로 서버에서 직접 조회
                val response = apiService.getUserById(userId)
                val domain = response.toDomain()
                
                // 로컬 캐시에 저장
                dao.insertUsers(listOf(domain.toEntity()))
                
                Result.success(domain)
            } catch (e: Exception) {
                // 네트워크 오류 시 로컬에서 검색 시도
                try {
                    val localEntities = dao.searchUsers(userId)
                    val matchedUser = localEntities.find { it.userId == userId }
                    if (matchedUser != null) {
                        Result.success(matchedUser.toDomain())
                    } else {
                        Result.failure(NoSuchElementException("해당 사용자를 찾을 수 없습니다"))
                    }
                } catch (localE: Exception) {
                    Result.failure(e)
                }
            }
        }
    }

    override suspend fun getUserByStudentNumber(studentNumber: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // 학번으로 검색하여 첫 번째 결과 반환
                val response = apiService.searchUsers(studentNumber)
                val user = response.find { it.studentNumber == studentNumber }
                
                if (user != null) {
                    val domain = user.toDomain()
                    dao.insertUsers(listOf(domain.toEntity()))
                    Result.success(domain)
                } else {
                    Result.failure(NoSuchElementException("해당 학번의 사용자를 찾을 수 없습니다"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}