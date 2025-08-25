package com.heyyoung.solsol.feature.auth.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.core.network.ApiResult
import com.heyyoung.solsol.core.network.LoginResult
import com.heyyoung.solsol.core.network.ShinhanApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: ShinhanApiRepository
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    // UI 상태 관리
    var uiState by mutableStateOf(LoginUiState())
        private set

    /**
     * 로그인 처리
     *
     * 1. 입력값 검증
     * 2. Repository를 통한 API 호출
     * 3. 결과에 따른 상태 업데이트
     */
    fun login(email: String, studentNumber: String) {
        Log.d(TAG, "로그인 시작")
        Log.d(TAG, "이메일: $email")
        Log.d(TAG, "학번: ${studentNumber.length}자리")

        // 입력값 검증
        val validationResult = validateInput(email, studentNumber)
        if (!validationResult.isValid) {
            Log.w(TAG, "입력값 검증 실패: ${validationResult.errorMessage}")
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = validationResult.errorMessage
            )
            return
        }

        Log.d(TAG, "입력값 검증 통과")

        // 로딩 시작
        uiState = uiState.copy(
            isLoading = true,
            errorMessage = null,
            isLoginSuccess = false
        )

        // 비동기 API 호출
        viewModelScope.launch {
            try {
                Log.d(TAG, "Repository API 호출 시작")

                val result = repository.loginUser(email, studentNumber)

                when (result) {
                    is ApiResult.Success -> {
                        Log.i(TAG, "로그인 성공")
                        handleLoginSuccess(result.data)
                    }

                    is ApiResult.Error -> {
                        Log.e(TAG, "로그인 실패: ${result.message}")
                        handleLoginError(result.message)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "로그인 중 예외 발생: ${e.message}", e)
                handleLoginError("네트워크 연결을 확인해주세요")
            }
        }
    }

    /**
     * 회원가입 처리
     *
     * 신한은행 API에서는 로그인과 회원가입이 동일한 프로세스입니다.
     * (사용자가 없으면 자동으로 생성)
     */
    fun register(email: String, studentNumber: String) {
        Log.d(TAG, "회원가입 시도 (로그인과 동일한 프로세스)")
        login(email, studentNumber)
    }

    /**
     * 로그인 성공 처리
     */
    private fun handleLoginSuccess(loginResult: LoginResult) {
        Log.i(TAG, "로그인 처리 완료")
        Log.d(TAG, "사용자: ${loginResult.studentName}")
        Log.d(TAG, "사용자 키: ${loginResult.userKey}")
        Log.d(TAG, "계좌번호: ${loginResult.accountNo ?: "없음"}")
        Log.d(TAG, "잔액: ${loginResult.balance}원")

        uiState = uiState.copy(
            isLoading = false,
            isLoginSuccess = true,
            errorMessage = null,
            loginResult = loginResult
        )
    }

    /**
     * 로그인 실패 처리
     */
    private fun handleLoginError(message: String) {
        Log.e(TAG, "로그인 실패 처리: $message")

        uiState = uiState.copy(
            isLoading = false,
            isLoginSuccess = false,
            errorMessage = message
        )
    }

    /**
     * 입력값 검증
     */
    private fun validateInput(email: String, studentNumber: String): ValidationResult {
        Log.d(TAG, "입력값 검증 시작")

        // 이메일 검증
        if (email.isBlank()) {
            return ValidationResult(false, "이메일을 입력해주세요")
        }

        if (!isValidEmail(email)) {
            return ValidationResult(false, "올바른 이메일 형식이 아닙니다")
        }

        // 학번 검증
        if (studentNumber.isBlank()) {
            return ValidationResult(false, "학번을 입력해주세요")
        }

        if (!studentNumber.all { it.isDigit() }) {
            return ValidationResult(false, "학번은 숫자만 입력 가능합니다")
        }

        if (!studentNumber.startsWith("20")) {
            return ValidationResult(false, "올바른 학번 형식이 아닙니다 (20으로 시작)")
        }

        Log.d(TAG, "입력값 검증 완료 - 통과")
        return ValidationResult(true, null)
    }

    /**
     * 이메일 유효성 검사
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        val isValid = emailRegex.matches(email)

        Log.v(TAG, "이메일 검증: $email -> ${if (isValid) "유효" else "무효"}")
        return isValid
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        Log.d(TAG, "에러 메시지 초기화")
        uiState = uiState.copy(errorMessage = null)
    }

    /**
     * 로그인 상태 초기화 (로그아웃 시 사용)
     */
    fun resetLoginState() {
        Log.d(TAG, "로그인 상태 초기화")
        uiState = LoginUiState()
    }

    /**
     * 현재 로그인 결과 조회 (다른 화면에서 사용)
     */
    fun getCurrentLoginResult(): LoginResult? {
        return uiState.loginResult
    }

    /**
     * 로그인 성공 여부 확인
     */
    fun isLoggedIn(): Boolean {
        return uiState.isLoginSuccess && uiState.loginResult != null
    }
}

/**
 * 로그인 UI 상태
 */
data class LoginUiState(
    val isLoading: Boolean = false,         // 로딩 중인지
    val isLoginSuccess: Boolean = false,    // 로그인 성공 여부
    val errorMessage: String? = null,       // 에러 메시지
    val loginResult: LoginResult? = null    // 로그인 결과 데이터
)

/**
 * 입력값 검증 결과
 */
private data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)