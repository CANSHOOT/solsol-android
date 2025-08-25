package com.heyyoung.solsol.feature.dutchpay.presentation.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayGroup
import com.heyyoung.solsol.feature.dutchpay.domain.model.ParticipantPaymentStatus
import com.heyyoung.solsol.feature.dutchpay.domain.model.PaymentResult
import com.heyyoung.solsol.feature.dutchpay.domain.usecase.JoinDutchPayUseCase
import com.heyyoung.solsol.feature.dutchpay.domain.usecase.SendDutchPaymentUseCase
import com.heyyoung.solsol.feature.dutchpay.domain.repository.DutchPayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DutchPaymentViewModel @Inject constructor(
    private val sendDutchPaymentUseCase: SendDutchPaymentUseCase,
    private val joinDutchPayUseCase: JoinDutchPayUseCase,
    private val dutchPayRepository: DutchPayRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId = savedStateHandle.get<String>("groupId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(DutchPaymentUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDutchPayDetails()
    }

    private fun loadDutchPayDetails() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            val currentUserId = getCurrentUserUseCase.getCurrentUserId()?.hashCode()?.toLong() ?: 0L
            
            dutchPayRepository.getDutchPayById(groupId).fold(
                onSuccess = { dutchPay ->
                    val currentParticipant = dutchPay.participants.find { it.userId.equals(currentUserId)}
                    val isOrganizer = dutchPay.organizerId.equals(currentUserId)
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            dutchPay = dutchPay,
                            currentParticipant = currentParticipant,
                            isOrganizer = isOrganizer,
                            canJoin = currentParticipant == null && !isOrganizer,
                            canPay = currentParticipant?.paymentStatus == ParticipantPaymentStatus.PENDING
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "더치페이 정보를 불러올 수 없습니다"
                        )
                    }
                }
            )
        }
    }

    fun onJoinDutchPay() {
        val currentUserId = getCurrentUserUseCase.getCurrentUserId()?.hashCode()?.toLong() ?: return
        
        _uiState.update { it.copy(isJoinLoading = true, error = null) }
        
        viewModelScope.launch {
            joinDutchPayUseCase(groupId, currentUserId).fold(
                onSuccess = {
                    _uiState.update { it.copy(isJoinLoading = false) }
                    loadDutchPayDetails()
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isJoinLoading = false,
                            error = error.message ?: "참여에 실패했습니다"
                        )
                    }
                }
            )
        }
    }
    
    fun onAccountNumberChanged(accountNumber: String) {
        _uiState.update { it.copy(accountNumber = accountNumber) }
    }
    
    fun onTransactionSummaryChanged(summary: String) {
        _uiState.update { it.copy(transactionSummary = summary) }
    }
    
    fun onSendPayment() {
        val currentState = _uiState.value
        
        if (!currentState.canPay) return
        if (currentState.accountNumber.isBlank() || currentState.transactionSummary.isBlank()) {
            _uiState.update { it.copy(error = "계좌번호와 거래내역을 입력해주세요") }
            return
        }
        
        _uiState.update { it.copy(isPaymentLoading = true, error = null) }
        
        viewModelScope.launch {
            sendDutchPaymentUseCase(
                groupId = groupId,
                accountNumber = currentState.accountNumber,
                transactionSummary = currentState.transactionSummary
            ).fold(
                onSuccess = { paymentResult ->
                    _uiState.update { 
                        it.copy(
                            isPaymentLoading = false,
                            paymentResult = paymentResult,
                            isPaymentSuccess = paymentResult.isSuccess
                        )
                    }
                    if (paymentResult.isSuccess) {
                        loadDutchPayDetails()
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isPaymentLoading = false,
                            error = error.message ?: "송금에 실패했습니다"
                        )
                    }
                }
            )
        }
    }

    fun refresh() {
        loadDutchPayDetails()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class DutchPaymentUiState(
    val isLoading: Boolean = true,
    val dutchPay: DutchPayGroup? = null,
    val currentParticipant: com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayParticipant? = null,
    val isOrganizer: Boolean = false,
    val canJoin: Boolean = false,
    val canPay: Boolean = false,
    val accountNumber: String = "",
    val transactionSummary: String = "",
    val isJoinLoading: Boolean = false,
    val isPaymentLoading: Boolean = false,
    val isPaymentSuccess: Boolean = false,
    val paymentResult: PaymentResult? = null,
    val error: String? = null
)