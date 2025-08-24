package com.heyyoung.solsol.feature.auth.domain.usecase

import com.heyyoung.solsol.feature.auth.domain.model.SignUpResult
import com.heyyoung.solsol.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 회원가입 UseCase
 * - 입력값 검증 (이메일, 이름, 학번 등)
 * - 이메일 중복 확인
 * - 회원가입 API 호출
 */
class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        name: String,
        studentNumber: String,
        departmentName: String,
        councilId: Long,
        isCouncilOfficer: Boolean = false
    ): Result<SignUpResult> {
        // 입력값 검증
        val validationError = validateInput(email, name, studentNumber, departmentName)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }
        
        // 이메일 중복 확인
        val emailCheckResult = authRepository.checkEmailExists(email)
        if (emailCheckResult.isFailure) {
            return Result.failure(emailCheckResult.exceptionOrNull()!!)
        }
        
        if (emailCheckResult.getOrNull() == true) {
            return Result.failure(IllegalArgumentException("이미 가입된 이메일입니다"))
        }
        
        return authRepository.signUp(
            email = email,
            name = name,
            studentNumber = studentNumber,
            departmentName = departmentName,
            councilId = councilId,
            isCouncilOfficer = isCouncilOfficer
        )
    }
    
    private fun validateInput(
        email: String,
        name: String,
        studentNumber: String,
        departmentName: String
    ): String? {
        return when {
            email.isBlank() -> "이메일을 입력해주세요"
            !isValidEmail(email) -> "올바른 이메일 형식을 입력해주세요"
            name.isBlank() -> "이름을 입력해주세요"
            name.length < 2 -> "이름은 2글자 이상 입력해주세요"
            studentNumber.isBlank() -> "학번을 입력해주세요"
            !isValidStudentNumber(studentNumber) -> "올바른 학번을 입력해주세요"
            departmentName.isBlank() -> "학과를 입력해주세요"
            else -> null
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidStudentNumber(studentNumber: String): Boolean {
        // 학번 형식 검증 (예: 2024001234)
        return studentNumber.length in 8..10 && studentNumber.all { it.isDigit() }
    }
}