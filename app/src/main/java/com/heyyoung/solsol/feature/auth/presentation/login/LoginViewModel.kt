package com.heyyoung.solsol.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.feature.auth.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 로그인 ViewModel
 * - 이메일 입력 상태 관리
 * - 로그인 요청 처리
 * - UI 상태 관리 (로딩, 에러, 성공)
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { 
            it.copy(
                email = email,
                errorMessage = null // 입력 시 에러 메시지 초기화
            ) 
        }
    }

    fun onLoginClicked() {
        val currentState = _uiState.value
        
        if (currentState.email.isBlank()) {
            _uiState.update { 
                it.copy(errorMessage = "이메일을 입력해주세요") 
            }
            return
        }

        _uiState.update { 
            it.copy(
                isLoading = true,
                errorMessage = null
            ) 
        }

        viewModelScope.launch {
            loginUseCase(currentState.email).fold(
                onSuccess = { tokens ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isLoginSuccess = true
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "로그인에 실패했습니다"
                        ) 
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

/**
 * 로그인 화면 UI 상태
 */
data class LoginUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val errorMessage: String? = null
)