package com.heyyoung.solsol.core.network

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyManager @Inject constructor() {

    private val TAG = "ApiKeyManager"

    // 제공받은 API 키 정보
    private val apiKeyInfo = ApiKeyInfo(
        managerId = "jungini0000@gmail.com",
        apiKey = "fbe9a613e9674dd5b987632a3b0866ae",
        creationDate = "20250814",
        expirationDate = "20260814"
    )

    init {
        Log.d(TAG, "API 키 관리자 초기화")
        Log.d(TAG, "관리자: ${apiKeyInfo.managerId}")
        Log.d(TAG, "API 키 만료일: ${apiKeyInfo.expirationDate}")

        // API 키 유효성 간단 체크
        if (isApiKeyExpired()) {
            Log.w(TAG, "경고: API 키가 만료되었을 수 있습니다")
        } else {
            Log.d(TAG, "API 키가 유효합니다")
        }
    }

    // 현재 사용할 API 키 반환
    fun getCurrentApiKey(): ApiKeyInfo {
        Log.v(TAG, "API 키 요청: ${apiKeyInfo.managerId}")
        return apiKeyInfo
    }

    fun getApiKeyInfo(): String {
        return "관리자: ${apiKeyInfo.managerId}, 만료일: ${apiKeyInfo.expirationDate}"
    }

    // API 키 만료 여부 확인
    private fun isApiKeyExpired(): Boolean {
        return try {
            val currentDate = getCurrentDateString()
            val isExpired = currentDate > apiKeyInfo.expirationDate

            if (isExpired) {
                Log.w(TAG, "API 키 만료: 현재 $currentDate > 만료일 ${apiKeyInfo.expirationDate}")
            }

            isExpired
        } catch (e: Exception) {
            Log.e(TAG, "날짜 비교 중 오류: ${e.message}")
            false
        }
    }

    // 현재 날짜를 YYYYMMDD 형식으로 반환
    private fun getCurrentDateString(): String {
        return try {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            String.format("%04d%02d%02d", year, month, day)
        } catch (e: Exception) {
            Log.e(TAG, "날짜 생성 오류: ${e.message}")
            "20250101"
        }
    }
}


data class ApiKeyInfo(
    val managerId: String,      // 관리자 이메일
    val apiKey: String,         // 실제 API 키
    val creationDate: String,   // 생성일 (YYYYMMDD)
    val expirationDate: String  // 만료일 (YYYYMMDD)
)