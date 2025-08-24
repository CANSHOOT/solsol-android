package com.heyyoung.solsol.feature.dutchpay.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.heyyoung.solsol.feature.dutchpay.domain.model.User
import com.heyyoung.solsol.feature.dutchpay.domain.usecase.CreateDutchPayUseCase
import com.heyyoung.solsol.feature.dutchpay.domain.usecase.SearchUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 더치페이 생성 화면 ViewModel
 * - 참여자 실시간 검색 (300ms debounce)
 * - 1인당 금액 자동 계산 및 UI 상태 관리
 * - 입력값 검증 및 더치페이 생성 처리
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class CreateDutchPayViewModel @Inject constructor(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val createDutchPayUseCase: CreateDutchPayUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateDutchPayUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    
    val searchResults = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                flow {
                    searchUsersUseCase(query).fold(
                        onSuccess = { emit(it) },
                        onFailure = { emit(emptyList()) }
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onGroupNameChanged(groupName: String) {
        _uiState.update { it.copy(groupName = groupName) }
    }

    fun onTotalAmountChanged(totalAmount: String) {
        val amount = totalAmount.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(totalAmount = amount, totalAmountText = totalAmount) }
        updateAmountPerPerson()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onParticipantAdded(user: User) {
        val currentState = _uiState.value
        if (!currentState.selectedParticipants.any { it.userId == user.userId }) {
            _uiState.update { 
                it.copy(selectedParticipants = it.selectedParticipants + user) 
            }
            updateAmountPerPerson()
        }
    }

    fun onParticipantRemoved(user: User) {
        _uiState.update { 
            it.copy(selectedParticipants = it.selectedParticipants.filter { participant -> 
                participant.userId != user.userId 
            }) 
        }
        updateAmountPerPerson()
    }

    private fun updateAmountPerPerson() {
        val currentState = _uiState.value
        val totalParticipants = currentState.selectedParticipants.size + 1 // +1 for organizer
        val amountPerPerson = if (totalParticipants > 0 && currentState.totalAmount > 0) {
            kotlin.math.ceil(currentState.totalAmount / totalParticipants * 100) / 100
        } else {
            0.0
        }
        _uiState.update { it.copy(amountPerPerson = amountPerPerson) }
    }

    fun onCreateDutchPay() {
        val currentState = _uiState.value
        
        if (!validateInput(currentState)) return
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            val currentUserId = getCurrentUserUseCase.getCurrentUserId()?.toLongOrNull() ?: 1L
            
            createDutchPayUseCase(
                organizerId = currentUserId,
                paymentId = 1L, // TODO: 실제 결제 ID 사용
                groupName = currentState.groupName,
                totalAmount = currentState.totalAmount,
                participantUserIds = currentState.selectedParticipants.map { it.userId }
            ).fold(
                onSuccess = { dutchPay ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            isSuccess = true,
                            createdDutchPayId = dutchPay.groupId
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message ?: "더치페이 생성에 실패했습니다"
                        ) 
                    }
                }
            )
        }
    }

    private fun validateInput(state: CreateDutchPayUiState): Boolean {
        return when {
            state.groupName.isBlank() -> {
                _uiState.update { it.copy(error = "그룹명을 입력해주세요") }
                false
            }
            state.totalAmount <= 0 -> {
                _uiState.update { it.copy(error = "올바른 금액을 입력해주세요") }
                false
            }
            state.selectedParticipants.isEmpty() -> {
                _uiState.update { it.copy(error = "참여자를 선택해주세요") }
                false
            }
            else -> true
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CreateDutchPayUiState(
    val groupName: String = "",
    val totalAmount: Double = 0.0,
    val totalAmountText: String = "",
    val selectedParticipants: List<User> = emptyList(),
    val amountPerPerson: Double = 0.0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val createdDutchPayId: Long? = null
)