package com.heyyoung.solsol.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.core.network.BackendAuthRepository
import com.heyyoung.solsol.core.network.BackendApiResult
import com.heyyoung.solsol.core.network.AccountDto
import com.heyyoung.solsol.feature.settlement.domain.nearby.NearbyConnectionsManager
import com.heyyoung.solsol.feature.settlement.domain.nearby.NearbyPermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: BackendAuthRepository,
    private val nearbyConnectionsManager: NearbyConnectionsManager,
    private val nearbyPermissionManager: NearbyPermissionManager
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _studentName = MutableStateFlow<String?>(null)
    val studentName: StateFlow<String?> = _studentName

    private val _studentNumber = MutableStateFlow<String?>(null)
    val studentNumber: StateFlow<String?> = _studentNumber

    val isLoading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    // BT 광고 상태 관리
    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising

    private val _advertisingError = MutableStateFlow<String?>(null)
    val advertisingError: StateFlow<String?> = _advertisingError

    // 계좌 정보 상태 관리
    private val _accountInfo = MutableStateFlow<AccountDto?>(null)
    val accountInfo: StateFlow<AccountDto?> = _accountInfo

    private val _accountError = MutableStateFlow<String?>(null)
    val accountError: StateFlow<String?> = _accountError

    private var isNearbyInitialized = false


    fun loadProfile() {
        viewModelScope.launch {
            isLoading.value = true
            // 프로필 정보 로드
            when (val result = repo.getMyProfile()) {
                is BackendApiResult.Success -> {
                    _studentName.value = result.data.name
                    _studentNumber.value = result.data.studentNumber
                    error.value = null
                }
                is BackendApiResult.Error<*> -> {
                    error.value = result.message
                }
            }
            
            // 계좌 정보 로드
            loadAccountInfo()
            
            isLoading.value = false
        }
    }

    /**
     * 계좌 정보 로드
     */
    private suspend fun loadAccountInfo() {
        when (val result = repo.getMyAccount()) {
            is BackendApiResult.Success -> {
                _accountInfo.value = result.data
                _accountError.value = null
                Log.d(TAG, "계좌 정보 로드 성공: ${result.data.accountNo}")
            }
            is BackendApiResult.Error<*> -> {
                _accountError.value = result.message
                Log.e(TAG, "계좌 정보 로드 실패: ${result.message}")
            }
        }
    }

    /**
     * 계좌 정보 새로고침
     */
    fun refreshAccountInfo() {
        viewModelScope.launch {
            loadAccountInfo()
        }
    }

    /**
     * BT 광고 토글 (피정산자용)
     */
    fun toggleBtAdvertising() {
        Log.d(TAG, "BT 광고 토글 요청 - 현재 상태: ${_isAdvertising.value}")
        
        if (_isAdvertising.value) {
            stopBtAdvertising()
        } else {
            startBtAdvertising()
        }
    }
    
    /**
     * BT 광고 시작
     */
    private fun startBtAdvertising() {
        Log.d(TAG, "BT 광고 시작 요청")
        
        // 권한 확인
        if (!nearbyPermissionManager.areAllPermissionsGranted()) {
            _advertisingError.value = "Bluetooth 및 위치 권한이 필요합니다"
            Log.w(TAG, "권한이 없어 광고 시작 불가")
            return
        }
        
        viewModelScope.launch {
            try {
                // NearbyConnectionsManager 초기화
                if (!isNearbyInitialized) {
                    nearbyConnectionsManager.initialize()
                    isNearbyInitialized = true
                    Log.d(TAG, "NearbyConnectionsManager 초기화 완료")
                }
                
                // 광고 시작
                nearbyConnectionsManager.startAdvertisingOnly()
                _isAdvertising.value = true
                _advertisingError.value = null
                Log.i(TAG, "BT 광고 시작됨")
                
            } catch (e: Exception) {
                _advertisingError.value = "광고 시작 실패: ${e.message}"
                Log.e(TAG, "BT 광고 시작 실패", e)
            }
        }
    }
    
    /**
     * BT 광고 중지
     */
    private fun stopBtAdvertising() {
        Log.d(TAG, "BT 광고 중지 요청")
        
        viewModelScope.launch {
            try {
                nearbyConnectionsManager.stopAdvertisingOnly()
                _isAdvertising.value = false
                _advertisingError.value = null
                Log.i(TAG, "BT 광고 중지됨")
                
            } catch (e: Exception) {
                _advertisingError.value = "광고 중지 실패: ${e.message}"
                Log.e(TAG, "BT 광고 중지 실패", e)
            }
        }
    }
    
    /**
     * 광고 에러 메시지 클리어
     */
    fun clearAdvertisingError() {
        _advertisingError.value = null
    }

    /**
     * 간단한 로그아웃 처리
     */
    fun logout(onLogoutComplete: () -> Unit) {
        Log.d(TAG, "로그아웃 시작")

        viewModelScope.launch {
            // 광고 중지
            if (_isAdvertising.value) {
                stopBtAdvertising()
            }
            
            repo.logout() // 토큰 삭제
            
            // 로컬 상태 초기화
            _studentName.value = null
            _studentNumber.value = null
            error.value = null
            _isAdvertising.value = false
            _advertisingError.value = null
            _accountInfo.value = null
            _accountError.value = null
            
            Log.i(TAG, "로그아웃 완료")
            onLogoutComplete() // 로그인 화면으로 이동
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // ViewModel 정리 시 광고 중지
        if (_isAdvertising.value) {
            viewModelScope.launch {
                try {
                    nearbyConnectionsManager.stopAdvertisingOnly()
                } catch (e: Exception) {
                    Log.e(TAG, "ViewModel 정리 중 광고 중지 실패", e)
                }
            }
        }
        Log.d(TAG, "HomeViewModel cleared")
    }
}
