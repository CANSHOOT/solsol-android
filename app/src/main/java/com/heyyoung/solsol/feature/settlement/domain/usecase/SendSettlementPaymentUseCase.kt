package com.heyyoung.solsol.feature.settlement.domain.usecase

import com.heyyoung.solsol.feature.settlement.domain.model.PaymentResult
import com.heyyoung.solsol.feature.settlement.domain.repository.SettlementRepository
import javax.inject.Inject

/**
 * 정산 송금 처리 UseCase
 * - 계좌번호 및 거래내역 검증
 * - 은행 API를 통한 송금 처리
 * - 결제 결과 반환 (성공/실패)
 */
class SendSettlementPaymentUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {
    suspend operator fun invoke(
        groupId: Long,
        accountNumber: String,
        transactionSummary: String
    ): Result<PaymentResult> {
        // 입력값 검증
        if (accountNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("계좌번호를 입력해주세요"))
        }
        
        if (transactionSummary.isBlank()) {
            return Result.failure(IllegalArgumentException("거래내역을 입력해주세요"))
        }
        
        if (!isValidAccountNumber(accountNumber)) {
            return Result.failure(IllegalArgumentException("올바른 계좌번호를 입력해주세요"))
        }
        
        return settlementRepository.paySettlement(groupId, accountNumber, transactionSummary)
    }
    
    private fun isValidAccountNumber(accountNumber: String): Boolean {
        // 계좌번호 형식 검증 (숫자만, 10-20자리)
        return accountNumber.all { it.isDigit() } && accountNumber.length in 10..20
    }
}