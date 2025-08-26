package com.heyyoung.solsol.core.network

import android.util.Log
import com.heyyoung.solsol.core.auth.TokenManager
import com.heyyoung.solsol.core.auth.UserInfo
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 백엔드 인증 API Repository
 *
 * Spring Boot 백엔드와 통신하여 인증 처리를 담당합니다.
 * - 로그인/회원가입
 * - JWT 토큰 관리
 * - 자동 토큰 갱신
 */
@Singleton
class BackendAuthRepository @Inject constructor(
    private val backendApiService: BackendApiService,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "BackendAuthRepository"
    }

    /**
     * 로그인 처리
     *
     * 1. 백엔드에 로그인 요청
     * 2. JWT 토큰 받아서 저장
     * 3. 사용자 정보 반환
     */
    suspend fun login(email: String, studentNumber: String): BackendApiResult<UserInfo> {
        Log.d(TAG, "로그인 처리 시작")
        Log.d(TAG, "이메일: $email")

        return try {
            val response = backendApiService.login(
                LoginRequest(
                    email = email,
                    studentNumber = studentNumber
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // 토큰과 사용자 정보 저장
                tokenManager.saveAuthData(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    userId = authResponse.userId,
                    userName = authResponse.name
                )

                val userInfo = UserInfo(
                    userId = authResponse.userId,
                    name = authResponse.name
                )

                Log.i(TAG, "로그인 성공: ${userInfo.name}")
                BackendApiResult.Success(userInfo)

            } else {
                val errorMessage = parseErrorMessage(response.code(), response.message())
                Log.e(TAG, "로그인 실패: $errorMessage")
                BackendApiResult.Error(errorMessage)
            }

        } catch (e: Exception) {
            Log.e(TAG, "로그인 중 예외: ${e.message}", e)
            BackendApiResult.Error("네트워크 연결을 확인해주세요")
        }
    }

    /**
     * 회원가입 처리
     *
     * 1. 백엔드에 회원가입 요청
     * 2. 성공 시 JWT 토큰 저장
     * 3. 사용자 정보 반환
     */
    suspend fun signup(signupData: SignupData): BackendApiResult<UserInfo> {
        Log.d(TAG, "회원가입 처리 시작")
        Log.d(TAG, "이메일: ${signupData.email}")
        Log.d(TAG, "이름: ${signupData.name}")

        return try {
            val response = backendApiService.signup(
                SignupRequest(
                    email = signupData.email,
                    studentNumber = signupData.studentNumber,
                    name = signupData.name,
                    departmentId = signupData.departmentId,
                    councilId = signupData.councilId,
                    isCouncilOfficer = signupData.isCouncilOfficer
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // 토큰과 사용자 정보 저장
                tokenManager.saveAuthData(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    userId = authResponse.userId,
                    userName = authResponse.name
                )

                val userInfo = UserInfo(
                    userId = authResponse.userId,
                    name = authResponse.name
                )

                Log.i(TAG, "회원가입 성공: ${userInfo.name}")
                BackendApiResult.Success(userInfo)

            } else {
                val errorMessage = parseErrorMessage(response.code(), response.message())
                Log.e(TAG, "회원가입 실패: $errorMessage")
                BackendApiResult.Error(errorMessage)
            }

        } catch (e: Exception) {
            Log.e(TAG, "회원가입 중 예외: ${e.message}", e)
            BackendApiResult.Error("네트워크 연결을 확인해주세요")
        }
    }

    /**
     * 로그아웃 처리
     *
     * 1. 저장된 토큰과 사용자 정보 삭제
     * 2. 로그인 화면으로 이동 가능 상태로 변경
     */
    suspend fun logout(): BackendApiResult<Unit> {
        Log.d(TAG, "로그아웃 처리")

        return try {
            tokenManager.clearAuthData()
            Log.i(TAG, "로그아웃 완료")
            BackendApiResult.Success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "로그아웃 중 오류: ${e.message}", e)
            BackendApiResult.Error("로그아웃 처리 중 오류가 발생했습니다")
        }
    }

    /**
     * 현재 로그인 상태 확인
     */
    suspend fun getCurrentUser(): BackendApiResult<UserInfo> {
        Log.d(TAG, "현재 사용자 정보 조회")

        return try {
            val userInfo = tokenManager.getCurrentUserInfo()
            val isLoggedIn = tokenManager.isLoggedIn().first()

            if (isLoggedIn && userInfo != null) {
                Log.d(TAG, "로그인된 사용자: ${userInfo.name}")
                BackendApiResult.Success(userInfo)
            } else {
                Log.d(TAG, "로그인되지 않은 상태")
                BackendApiResult.Error("로그인이 필요합니다")
            }

        } catch (e: Exception) {
            Log.e(TAG, "사용자 정보 조회 실패: ${e.message}", e)
            BackendApiResult.Error("사용자 정보를 조회할 수 없습니다")
        }
    }

    /**
     * JWT 토큰 자동 갱신
     *
     * API 호출 시 401 Unauthorized가 발생하면 자동으로 호출됩니다.
     */
    suspend fun refreshToken(): BackendApiResult<Unit> {
        Log.d(TAG, "토큰 갱신 시작")

        return try {
            val refreshToken = tokenManager.getCurrentRefreshToken()

            if (refreshToken.isNullOrBlank()) {
                Log.w(TAG, "리프레시 토큰이 없음")
                return BackendApiResult.Error("다시 로그인해주세요")
            }

            val response = backendApiService.refreshToken(
                RefreshTokenRequest(refreshToken = refreshToken)
            )

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // 새 토큰들과 사용자 정보 저장
                tokenManager.saveAuthData(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    userId = authResponse.userId,
                    userName = authResponse.name
                )

                Log.i(TAG, "토큰 갱신 완료")
                BackendApiResult.Success(Unit)

            } else {
                Log.e(TAG, "토큰 갱신 실패: ${response.code()} ${response.message()}")
                BackendApiResult.Error("토큰 갱신에 실패했습니다. 다시 로그인해주세요")
            }

        } catch (e: Exception) {
            Log.e(TAG, "토큰 갱신 중 예외: ${e.message}", e)
            BackendApiResult.Error("네트워크 연결을 확인해주세요")
        }
    }

    /**
     * HTTP 에러 코드에 따른 사용자 친화적 메시지 생성
     */
    private fun parseErrorMessage(code: Int, message: String?): String {
        return when (code) {
            400 -> "입력 정보를 확인해주세요"
            401 -> "이메일 또는 학번이 올바르지 않습니다"
            403 -> "접근 권한이 없습니다"
            404 -> "사용자를 찾을 수 없습니다"
            409 -> "이미 가입된 이메일입니다"
            500 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요"
            else -> message ?: "알 수 없는 오류가 발생했습니다"
        }
    }
}

/**
 * 백엔드 API 결과 래퍼
 */
sealed class BackendApiResult<T> {
    data class Success<T>(val data: T) : BackendApiResult<T>()
    data class Error<T>(val message: String) : BackendApiResult<T>()
}

/**
 * 회원가입 데이터
 */
data class SignupData(
    val email: String,
    val studentNumber: String,
    val name: String,
    val departmentId: Int,
    val councilId: Int,
    val isCouncilOfficer: Boolean
)