package com.heyyoung.solsol.feature.dutchpay.domain.repository

import com.heyyoung.solsol.feature.dutchpay.domain.model.User

interface UserRepository {
    suspend fun searchUsers(query: String): Result<List<User>>
    suspend fun getUserById(userId: Long): Result<User>
    suspend fun getUserByStudentNumber(studentNumber: String): Result<User>
}