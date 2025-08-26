package com.heyyoung.solsol.core.auth

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

// DataStore 확장 프로퍼티
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

/**
 * JWT 토큰 및 사용자 정보 관리
 *
 * DataStore를 사용하여 토큰을 안전하게 저장/관리합니다.
 * - 액세스 토큰 (짧은 만료시간)
 * - 리프레시 토큰 (긴 만료시간)
 * - 사용자 정보 (이메일, 이름)
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TokenManager"

        // DataStore 키들
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    /**
     * 로그인 성공 시 토큰과 사용자 정보 저장
     */
    suspend fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        userId: String,
        userName: String
    ) {
        Log.d(TAG, "인증 데이터 저장 시작")
        Log.d(TAG, "사용자: $userName ($userId)")

        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[USER_ID_KEY] = userId
            preferences[USER_NAME_KEY] = userName
        }

        Log.i(TAG, "인증 데이터 저장 완료")
    }

    /**
     * 액세스 토큰 조회
     */
    fun getAccessToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }

    /**
     * 리프레시 토큰 조회
     */
    fun getRefreshToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }
    }

    /**
     * 사용자 정보 조회
     */
    fun getUserInfo(): Flow<UserInfo?> {
        return context.dataStore.data.map { preferences ->
            val userId = preferences[USER_ID_KEY]
            val userName = preferences[USER_NAME_KEY]

            if (userId != null && userName != null) {
                UserInfo(userId = userId, name = userName)
            } else {
                null
            }
        }
    }

    /**
     * 로그인 여부 확인
     */
    fun isLoggedIn(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            val accessToken = preferences[ACCESS_TOKEN_KEY]
            val refreshToken = preferences[REFRESH_TOKEN_KEY]
            !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()
        }
    }

    /**
     * 토큰 갱신 시 액세스 토큰 업데이트
     */
    suspend fun updateAccessToken(newAccessToken: String) {
        Log.d(TAG, "액세스 토큰 갱신")

        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = newAccessToken
        }

        Log.i(TAG, "액세스 토큰 갱신 완료")
    }

    /**
     * 로그아웃 시 모든 데이터 삭제
     */
    suspend fun clearAuthData() {
        Log.d(TAG, "인증 데이터 삭제 시작")

        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_NAME_KEY)
        }

        Log.i(TAG, "로그아웃 완료 - 모든 인증 데이터 삭제됨")
    }

    // (교체) 현재 저장된 리프레시 토큰 즉시 조회
    suspend fun getCurrentRefreshToken(): String? {
        return try {
            context.dataStore.data
                .map { prefs -> prefs[REFRESH_TOKEN_KEY] }
                .first()
        } catch (e: Exception) {
            Log.e(TAG, "리프레시 토큰 조회 실패: ${e.message}")
            null
        }
    }

    // (교체) 현재 저장된 사용자 정보 즉시 조회
    suspend fun getCurrentUserInfo(): UserInfo? {
        return try {
            context.dataStore.data
                .map { prefs ->
                    val id = prefs[USER_ID_KEY]
                    val name = prefs[USER_NAME_KEY]
                    if (id != null && name != null) UserInfo(id, name) else null
                }
                .first()
        } catch (e: Exception) {
            Log.e(TAG, "사용자 정보 조회 실패: ${e.message}")
            null
        }
    }

}

/**
 * 사용자 정보 데이터 클래스
 */
data class UserInfo(
    val userId: String,     // 이메일
    val name: String        // 사용자 이름
)