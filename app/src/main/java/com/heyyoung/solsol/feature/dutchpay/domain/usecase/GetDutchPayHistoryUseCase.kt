package com.heyyoung.solsol.feature.dutchpay.domain.usecase

import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayGroup
import com.heyyoung.solsol.feature.dutchpay.domain.repository.DutchPayRepository
import javax.inject.Inject

class GetDutchPayHistoryUseCase @Inject constructor(
    private val dutchPayRepository: DutchPayRepository
) {
    suspend operator fun invoke(userId: Long): Result<List<DutchPayGroup>> {
        return dutchPayRepository.getDutchPayHistory(userId)
    }
}