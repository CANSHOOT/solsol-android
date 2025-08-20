package com.heyyoung.solsol.feature.dutchpay.presentation.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayGroup
import com.heyyoung.solsol.feature.dutchpay.domain.model.ParticipantPaymentStatus
import com.heyyoung.solsol.feature.dutchpay.domain.usecase.SendDutchPaymentUseCase
import com.heyyoung.solsol.feature.dutchpay.domain.repository.DutchPayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DutchPaymentViewModel @Inject constructor(
    private val sendDutchPaymentUseCase: SendDutchPaymentUseCase,
    private val dutchPayRepository: DutchPayRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId = savedStateHandle.get<Long>("groupId") ?: 0L
    private val currentUserId = 1L // TODO: 실제 현재 사용자 ID 가져오기

    private val _uiState = MutableStateFlow(DutchPaymentUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDutchPayDetails()
    }

    private fun loadDutchPayDetails() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            dutchPayRepository.getDutchPayById(groupId).fold(
                onSuccess = { dutchPay ->
                    val currentParticipant = dutchPay.participants.find { it.userId == currentUserId }
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            dutchPay = dutchPay,
                            currentParticipant = currentParticipant,
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

    fun onSendPayment() {
        val currentState = _uiState.value
        val participant = currentState.currentParticipant ?: return
        
        if (!currentState.canPay) return
        
        _uiState.update { it.copy(isPaymentLoading = true, error = null) }
        
        viewModelScope.launch {
            sendDutchPaymentUseCase(
                groupId = groupId,
                participantId = participant.participantId ?: 0L
            ).fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isPaymentLoading = false,
                            isPaymentSuccess = true
                        )
                    }
                    // 결제 후 데이터 다시 로드
                    loadDutchPayDetails()
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
    val canPay: Boolean = false,
    val isPaymentLoading: Boolean = false,
    val isPaymentSuccess: Boolean = false,
    val error: String? = null
)