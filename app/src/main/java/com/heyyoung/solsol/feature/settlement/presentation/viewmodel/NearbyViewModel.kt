package com.heyyoung.solsol.feature.settlement.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.feature.settlement.domain.nearby.NearbyConnectionsManager
import com.heyyoung.solsol.feature.settlement.domain.nearby.NearbyPermissionManager
import com.heyyoung.solsol.feature.settlement.domain.model.NearbyUser
import com.heyyoung.solsol.feature.settlement.domain.model.NearbyDiscoveryState
import com.heyyoung.solsol.feature.settlement.domain.model.Person
import com.heyyoung.solsol.feature.settlement.domain.model.toPerson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val nearbyConnectionsManager: NearbyConnectionsManager,
    private val nearbyPermissionManager: NearbyPermissionManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "NearbyViewModel"
    }
    
    // Nearby 관련 상태들
    val discoveryState: StateFlow<NearbyDiscoveryState> = nearbyConnectionsManager.discoveryState
    val discoveredUsers: StateFlow<List<NearbyUser>> = nearbyConnectionsManager.discoveredUsers
    
    // UI 상태
    private val _isBottomSheetVisible = MutableStateFlow(false)
    val isBottomSheetVisible: StateFlow<Boolean> = _isBottomSheetVisible.asStateFlow()
    
    private val _selectedUsers = MutableStateFlow<List<NearbyUser>>(emptyList())
    val selectedUsers: StateFlow<List<NearbyUser>> = _selectedUsers.asStateFlow()
    
    // 권한 관련 상태
    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.Unknown)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()
    
    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()
    
    private val _permissionMessage = MutableStateFlow("")
    val permissionMessage: StateFlow<String> = _permissionMessage.asStateFlow()
    
    private var isInitialized = false
    
    /**
     * ViewModel 초기화
     */
    fun initialize() {
        if (!isInitialized) {
            Log.d(TAG, "NearbyViewModel 초기화")
            viewModelScope.launch {
                try {
                    nearbyConnectionsManager.initialize()
                    isInitialized = true
                    Log.d(TAG, "NearbyConnectionsManager 초기화 완료")
                } catch (e: Exception) {
                    Log.e(TAG, "초기화 실패: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 주변 기기 검색 시작
     */
    fun startNearbySearch() {
        Log.d(TAG, "주변 기기 검색 시작 요청")
        
        if (!isInitialized) {
            Log.w(TAG, "아직 초기화되지 않음")
            initialize()
            return
        }
        
        // 권한 확인
        if (!nearbyPermissionManager.areAllPermissionsGranted()) {
            Log.w(TAG, "권한이 허용되지 않음 - 권한 요청 다이얼로그 표시")
            showPermissionRequest()
            return
        }
        
        try {
            nearbyConnectionsManager.startDiscovery()
            _permissionState.value = PermissionState.Granted
        } catch (e: Exception) {
            Log.e(TAG, "검색 시작 실패: ${e.message}")
        }
    }
    
    /**
     * 주변 기기 검색 중지
     */
    fun stopNearbySearch() {
        Log.d(TAG, "주변 기기 검색 중지 요청")
        
        try {
            nearbyConnectionsManager.stopDiscovery()
        } catch (e: Exception) {
            Log.e(TAG, "검색 중지 실패: ${e.message}")
        }
    }
    
    /**
     * 바텀시트 표시/숨김
     */
    fun showBottomSheet() {
        _isBottomSheetVisible.value = true
    }
    
    fun hideBottomSheet() {
        _isBottomSheetVisible.value = false
        // 바텀시트 닫을 때 검색도 중지
        stopNearbySearch()
    }
    
    /**
     * 사용자 선택
     */
    fun selectUser(nearbyUser: NearbyUser) {
        val currentSelected = _selectedUsers.value.toMutableList()
        
        // 중복 선택 방지 (userId 기준)
        val alreadySelected = currentSelected.any { it.userProfile.userId == nearbyUser.userProfile.userId }
        
        if (!alreadySelected) {
            currentSelected.add(nearbyUser)
            _selectedUsers.value = currentSelected
            Log.d(TAG, "사용자 선택됨: ${nearbyUser.userProfile.name}")
        } else {
            Log.d(TAG, "이미 선택된 사용자: ${nearbyUser.userProfile.name}")
        }
    }
    
    /**
     * 사용자 선택 해제
     */
    fun deselectUser(nearbyUser: NearbyUser) {
        val currentSelected = _selectedUsers.value.toMutableList()
        currentSelected.removeAll { it.userProfile.userId == nearbyUser.userProfile.userId }
        _selectedUsers.value = currentSelected
        Log.d(TAG, "사용자 선택 해제됨: ${nearbyUser.userProfile.name}")
    }
    
    /**
     * 선택된 사용자들을 Person 리스트로 변환
     */
    fun getSelectedPersons(): List<Person> {
        return _selectedUsers.value.map { it.toPerson() }
    }
    
    /**
     * 선택된 사용자 목록 초기화
     */
    fun clearSelectedUsers() {
        _selectedUsers.value = emptyList()
        Log.d(TAG, "선택된 사용자 목록 초기화")
    }
    
    /**
     * 권한 상태 업데이트
     */
    fun updatePermissionState(state: PermissionState) {
        _permissionState.value = state
        Log.d(TAG, "권한 상태 업데이트: $state")
    }
    
    /**
     * 권한 요청 다이얼로그 표시
     */
    private fun showPermissionRequest() {
        _permissionMessage.value = nearbyPermissionManager.getPermissionRationaleMessage()
        _showPermissionDialog.value = true
        _permissionState.value = PermissionState.RequestRequired
        Log.d(TAG, "권한 요청 다이얼로그 표시")
    }
    
    /**
     * 권한 다이얼로그 숨김
     */
    fun hidePermissionDialog() {
        _showPermissionDialog.value = false
        Log.d(TAG, "권한 다이얼로그 숨김")
    }
    
    /**
     * 권한 요청 결과 처리
     */
    fun onPermissionResult(granted: Boolean) {
        hidePermissionDialog()
        
        if (granted) {
            Log.d(TAG, "권한이 허용됨 - 검색 시작")
            _permissionState.value = PermissionState.Granted
            // 권한이 허용되면 자동으로 검색 시작
            startNearbySearch()
        } else {
            Log.w(TAG, "권한이 거부됨")
            _permissionState.value = PermissionState.Denied
        }
    }
    
    /**
     * 권한 확인 및 상태 업데이트
     */
    fun checkPermissions(): Boolean {
        val allGranted = nearbyPermissionManager.areAllPermissionsGranted()
        _permissionState.value = if (allGranted) {
            PermissionState.Granted
        } else {
            PermissionState.Denied
        }
        
        nearbyPermissionManager.logPermissionStatus()
        Log.d(TAG, "권한 확인 완료: ${if (allGranted) "모두 허용됨" else "일부 거부됨"}")
        
        return allGranted
    }
    
    /**
     * 필요한 권한 목록 반환
     */
    fun getRequiredPermissions(): Array<String> {
        return nearbyPermissionManager.getRequiredPermissions()
    }
    
    /**
     * 현재 검색 중인지 확인
     */
    fun isSearching(): Boolean {
        return nearbyConnectionsManager.isDiscovering()
    }
    
    /**
     * 발견된 사용자 수 반환
     */
    fun getDiscoveredUserCount(): Int {
        return nearbyConnectionsManager.getDiscoveredUserCount()
    }
    
    /**
     * 검색 결과 초기화
     */
    fun clearDiscoveredUsers() {
        nearbyConnectionsManager.clearDiscoveredUsers()
        Log.d(TAG, "검색 결과 초기화")
    }
    
    /**
     * 리소스 정리
     */
    fun cleanup() {
        Log.d(TAG, "NearbyViewModel 정리 시작")
        stopNearbySearch()
        clearSelectedUsers()
        clearDiscoveredUsers()
        _isBottomSheetVisible.value = false
        _showPermissionDialog.value = false
        isInitialized = false
    }
    
    override fun onCleared() {
        super.onCleared()
        cleanup()
        Log.d(TAG, "NearbyViewModel cleared")
    }
}

/**
 * 권한 상태
 */
enum class PermissionState {
    Unknown,
    Granted,
    Denied,
    PermanentlyDenied,
    RequestRequired
}