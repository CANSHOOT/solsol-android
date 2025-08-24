package com.heyyoung.solsol.feature.auth.data.remote.dto

import com.heyyoung.solsol.feature.auth.domain.model.AuthTokens
import com.heyyoung.solsol.feature.auth.domain.model.AuthUser
import com.heyyoung.solsol.feature.auth.domain.model.SignUpResult

/**
 * 로그인 요청 DTO
 */
data class LoginRequest(
    val email: String
)

/**
 * 로그인 응답 DTO
 */
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val name: String
)

/**
 * 회원가입 요청 DTO
 */
data class SignUpRequest(
    val email: String,
    val name: String,
    val studentNumber: String,
    val departmentName: String,
    val councilId: Long,
    val isCouncilOfficer: Boolean = false
)

/**
 * 회원가입 응답 DTO
 */
data class SignUpResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val name: String
)

/**
 * 토큰 갱신 요청 DTO
 */
data class RefreshTokenRequest(
    val refreshToken: String
)

/**
 * 토큰 갱신 응답 DTO
 */
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val name: String
)

/**
 * 사용자 정보 응답 DTO
 */
data class UserInfoResponse(
    val userId: String,
    val name: String,
    val email: String,
    val studentNumber: String,
    val departmentName: String,
    val councilId: Long,
    val isCouncilOfficer: Boolean
)

// Extension functions for mapping
fun LoginResponse.toDomain() = AuthTokens(
    accessToken = accessToken,
    refreshToken = refreshToken,
    userId = userId,
    name = name
)

fun SignUpResponse.toDomain() = SignUpResult(
    accessToken = accessToken,
    refreshToken = refreshToken,
    userId = userId,
    name = name
)

fun RefreshTokenResponse.toDomain() = AuthTokens(
    accessToken = accessToken,
    refreshToken = refreshToken,
    userId = userId,
    name = name
)

fun UserInfoResponse.toDomain() = AuthUser(
    userId = userId,
    name = name,
    email = email,
    studentNumber = studentNumber,
    departmentName = departmentName,
    councilId = councilId,
    isCouncilOfficer = isCouncilOfficer
)