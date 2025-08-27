package com.heyyoung.solsol.feature.settlement.domain.usecase

import com.heyyoung.solsol.feature.settlement.domain.model.SettlementGroup
import com.heyyoung.solsol.feature.settlement.domain.repository.SettlementRepository
import javax.inject.Inject

class GetSettlementHistoryUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {
    suspend operator fun invoke(userId: Long): Result<List<SettlementGroup>> {
        return settlementRepository.getSettlementHistory(userId)
    }
}