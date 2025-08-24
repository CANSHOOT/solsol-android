package com.heyyoung.solsol.feature.dutchpay.domain.usecase

import com.heyyoung.solsol.feature.dutchpay.domain.model.PaymentResult
import com.heyyoung.solsol.feature.dutchpay.domain.repository.DutchPayRepository
import javax.inject.Inject

/**
 * 더치페이 송금 처리 UseCase
 * - 계좌번호 및 거래내역 검증
 * - 은행 API를 통한 송금 처리
 * - 결제 결과 반환 (성공/실패)
 */
class SendDutchPaymentUseCase @Inject constructor(
    private val dutchPayRepository: DutchPayRepository
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
        
        return dutchPayRepository.payDutchPay(groupId, accountNumber, transactionSummary)
    }
    
    private fun isValidAccountNumber(accountNumber: String): Boolean {
        // 계좌번호 형식 검증 (숫자만, 10-20자리)
        return accountNumber.all { it.isDigit() } && accountNumber.length in 10..20
    }
}