package com.heyyoung.solsol.feature.auth.domain.model

/**
 * 인증된 사용자 도메인 모델
 */
data class AuthUser(
    val userId: String,
    val name: String,
    val email: String,
    val studentNumber: String,
    val departmentName: String,
    val councilId: Long,
    val isCouncilOfficer: Boolean
)

/**
 * 로그인 응답 모델
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val name: String
)

/**
 * 회원가입 응답 모델
 */
data class SignUpResult(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val name: String
)