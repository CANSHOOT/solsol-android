package com.heyyoung.solsol.feature.dutchpay.domain.usecase

import com.heyyoung.solsol.feature.dutchpay.domain.model.User
import com.heyyoung.solsol.feature.dutchpay.domain.repository.UserRepository
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(query: String): Result<List<User>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        
        if (query.length < 2) {
            return Result.success(emptyList())
        }
        
        return userRepository.searchUsers(query.trim())
    }
}