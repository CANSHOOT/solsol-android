package com.heyyoung.solsol.feature.settlement.data.repository

import com.heyyoung.solsol.feature.settlement.data.remote.SettlementApiService
import com.heyyoung.solsol.feature.settlement.data.remote.dto.toDomain
import com.heyyoung.solsol.feature.settlement.domain.model.User
import com.heyyoung.solsol.feature.settlement.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: SettlementApiService
) : UserRepository {

    override suspend fun searchUsers(query: String): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchUsers(query)
                val domains = response.map { it.toDomain() }
                
                Result.success(domains)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getUserById(userId: Long): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val userIdString = userId.toString()
                val response = apiService.getUserById(userIdString)
                val domain = response.toDomain()
                
                Result.success(domain)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getUserByStringId(userId: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserById(userId)
                val domain = response.toDomain()
                
                Result.success(domain)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getUserByStudentNumber(studentNumber: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchUsers(studentNumber)
                val user = response.find { it.studentNumber == studentNumber }
                
                if (user != null) {
                    val domain = user.toDomain()
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