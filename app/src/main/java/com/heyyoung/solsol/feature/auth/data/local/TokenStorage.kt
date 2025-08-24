package com.heyyoung.solsol.feature.auth.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.heyyoung.solsol.feature.auth.domain.model.AuthTokens
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 토큰 저장소
 * - 암호화된 SharedPreferences를 사용하여 토큰 안전 저장
 * - 액세스 토큰, 리프레시 토큰, 사용자 정보 관리
 */
@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val PREFS_NAME = "solsol_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // EncryptedSharedPreferences 초기화 실패 시 일반 SharedPreferences 사용
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
    
    /**
     * 토큰 저장
     */
    fun saveTokens(tokens: AuthTokens) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
            .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
            .putString(KEY_USER_ID, tokens.userId)
            .putString(KEY_USER_NAME, tokens.name)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }
    
    /**
     * 액세스 토큰 조회
     */
    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }
    
    /**
     * 리프레시 토큰 조회
     */
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * 사용자 ID 조회
     */
    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }
    
    /**
     * 사용자 이름 조회
     */
    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }
    
    /**
     * 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) &&
                !getAccessToken().isNullOrBlank() &&
                !getRefreshToken().isNullOrBlank()
    }
    
    /**
     * 액세스 토큰만 업데이트 (토큰 갱신 시)
     */
    fun updateAccessToken(accessToken: String) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .apply()
    }
    
    /**
     * 모든 토큰 및 사용자 정보 삭제 (로그아웃)
     */
    fun clearTokens() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }
    
    /**
     * 저장된 토큰 정보 반환
     */
    fun getStoredTokens(): AuthTokens? {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        val userId = getUserId()
        val name = getUserName()
        
        return if (!accessToken.isNullOrBlank() && 
                  !refreshToken.isNullOrBlank() && 
                  !userId.isNullOrBlank() && 
                  !name.isNullOrBlank()) {
            AuthTokens(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = userId,
                name = name
            )
        } else {
            null
        }
    }
}