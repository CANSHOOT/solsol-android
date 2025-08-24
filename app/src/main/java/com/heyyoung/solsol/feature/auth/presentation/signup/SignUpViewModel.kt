package com.heyyoung.solsol.feature.auth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.feature.auth.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 회원가입 ViewModel
 * - 사용자 입력 상태 관리
 * - 입력값 검증
 * - 회원가입 요청 처리
 */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { 
            it.copy(
                email = email,
                errorMessage = null
            ) 
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { 
            it.copy(
                name = name,
                errorMessage = null
            ) 
        }
    }

    fun onStudentNumberChanged(studentNumber: String) {
        _uiState.update { 
            it.copy(
                studentNumber = studentNumber,
                errorMessage = null
            ) 
        }
    }

    fun onDepartmentNameChanged(departmentName: String) {
        _uiState.update { 
            it.copy(
                departmentName = departmentName,
                errorMessage = null
            ) 
        }
    }

    fun onCouncilIdChanged(councilId: Long) {
        _uiState.update { 
            it.copy(
                councilId = councilId,
                errorMessage = null
            ) 
        }
    }

    fun onCouncilOfficerChanged(isCouncilOfficer: Boolean) {
        _uiState.update { 
            it.copy(
                isCouncilOfficer = isCouncilOfficer,
                errorMessage = null
            ) 
        }
    }

    fun onSignUpClicked() {
        val currentState = _uiState.value

        _uiState.update { 
            it.copy(
                isLoading = true,
                errorMessage = null
            ) 
        }

        viewModelScope.launch {
            signUpUseCase(
                email = currentState.email,
                name = currentState.name,
                studentNumber = currentState.studentNumber,
                departmentName = currentState.departmentName,
                councilId = currentState.councilId,
                isCouncilOfficer = currentState.isCouncilOfficer
            ).fold(
                onSuccess = { signUpResult ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isSignUpSuccess = true
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "회원가입에 실패했습니다"
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
 * 회원가입 화면 UI 상태
 */
data class SignUpUiState(
    val email: String = "",
    val name: String = "",
    val studentNumber: String = "",
    val departmentName: String = "",
    val councilId: Long = 1L,
    val isCouncilOfficer: Boolean = false,
    val isLoading: Boolean = false,
    val isSignUpSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val canSignUp: Boolean
        get() = email.isNotBlank() && 
                name.isNotBlank() && 
                studentNumber.isNotBlank() && 
                departmentName.isNotBlank()
}