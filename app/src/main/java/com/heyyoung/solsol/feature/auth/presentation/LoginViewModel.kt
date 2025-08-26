package com.heyyoung.solsol.feature.auth.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.core.auth.UserInfo
import com.heyyoung.solsol.core.network.BackendAuthRepository
import com.heyyoung.solsol.core.network.BackendApiResult
import com.heyyoung.solsol.core.network.SignupData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: BackendAuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun login(email: String, studentNumber: String) {
        Log.d(TAG, "로그인 시작: $email")

        val v = validateInput(email, studentNumber)
        if (!v.isValid) {
            uiState = uiState.copy(isLoading = false, errorMessage = v.errorMessage)
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, isLoginSuccess = false)

        viewModelScope.launch {
            when (val result = repository.login(email, studentNumber)) {
                is BackendApiResult.Success -> {
                    Log.i(TAG, "로그인 성공: ${result.data.name}")
                    uiState = uiState.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        errorMessage = null,
                        userInfo = result.data
                    )
                }
                is BackendApiResult.Error -> {
                    Log.e(TAG, "로그인 실패: ${result.message}")
                    uiState = uiState.copy(
                        isLoading = false,
                        isLoginSuccess = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun register(email: String, studentNumber: String) {
        Log.d(TAG, "회원가입 시도(간단 버전)")

        val v = validateInput(email, studentNumber)
        if (!v.isValid) {
            uiState = uiState.copy(isLoading = false, errorMessage = v.errorMessage)
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, isLoginSuccess = false)

        viewModelScope.launch {
            // 해커톤용: 고정값 최소 입력(피그마/도메인에 맞게 나중에 바꿔도 됨)
            val signup = SignupData(
                email = email,
                studentNumber = studentNumber,
                name = email.substringBefore("@"),
                departmentId = 1,
                councilId = 1,
                isCouncilOfficer = false
            )

            when (val result = repository.signup(signup)) {
                is BackendApiResult.Success -> {
                    Log.i(TAG, "회원가입 성공: ${result.data.name}")
                    uiState = uiState.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        errorMessage = null,
                        userInfo = result.data
                    )
                }
                is BackendApiResult.Error -> {
                    Log.e(TAG, "회원가입 실패: ${result.message}")
                    uiState = uiState.copy(
                        isLoading = false,
                        isLoginSuccess = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    fun resetLoginState() {
        uiState = LoginUiState()
    }

    fun getCurrentLoginResult(): UserInfo? = uiState.userInfo
    fun isLoggedIn(): Boolean = uiState.isLoginSuccess && uiState.userInfo != null

    private fun validateInput(email: String, studentNumber: String): ValidationResult {
        if (email.isBlank()) return ValidationResult(false, "이메일을 입력해주세요")
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        if (!emailRegex.matches(email)) return ValidationResult(false, "올바른 이메일 형식이 아닙니다")

        if (studentNumber.isBlank()) return ValidationResult(false, "학번을 입력해주세요")
        if (!studentNumber.all { it.isDigit() }) return ValidationResult(false, "학번은 숫자만 입력 가능합니다")
        if (!studentNumber.startsWith("20")) return ValidationResult(false, "올바른 학번 형식이 아닙니다 (20으로 시작)")

        return ValidationResult(true, null)
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val errorMessage: String? = null,
    val userInfo: UserInfo? = null
)

private data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)
