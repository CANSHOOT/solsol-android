package com.heyyoung.solsol.feature.dutchpay.presentation.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayGroup
import com.heyyoung.solsol.feature.dutchpay.domain.usecase.GetDutchPayHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 더치페이 내역 화면 ViewModel
 * - 사용자가 받은 정산 요청 목록 조회
 */
@HiltViewModel
class DutchPayHistoryViewModel @Inject constructor(
    private val getDutchPayHistoryUseCase: GetDutchPayHistoryUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "DutchPayHistoryVM"
    }
    
    private val _uiState = MutableStateFlow(DutchPayHistoryUiState())
    val uiState: StateFlow<DutchPayHistoryUiState> = _uiState.asStateFlow()
    
    fun loadDutchPayHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val currentUserId = getCurrentUserUseCase.getCurrentUserId()
                if (currentUserId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "로그인이 필요합니다"
                    )
                    return@launch
                }
                
                // 임시로 userId를 Long으로 변환 (실제로는 API 스펙에 맞춰 수정 필요)
                val userId = currentUserId.hashCode().toLong()
                
                getDutchPayHistoryUseCase(userId).fold(
                    onSuccess = { dutchPayList ->
                        Log.d(TAG, "✅ 더치페이 내역 로드 성공: ${dutchPayList.size}개")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            dutchPayList = dutchPayList,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ 더치페이 내역 로드 실패", error)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "더치페이 내역을 불러올 수 없습니다"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ 더치페이 내역 로드 중 예외 발생", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "오류가 발생했습니다"
                )
            }
        }
    }
}

data class DutchPayHistoryUiState(
    val isLoading: Boolean = false,
    val dutchPayList: List<DutchPayGroup> = emptyList(),
    val error: String? = null
)