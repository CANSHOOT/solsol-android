package com.heyyoung.solsol.feature.settlement.domain.nearby

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.heyyoung.solsol.feature.settlement.domain.model.*
import com.heyyoung.solsol.core.network.BackendAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NearbyConnectionsManager @Inject constructor(
    private val context: Context,
    private val backendAuthRepository: BackendAuthRepository
) {
    companion object {
        private const val TAG = "NearbyConnectionsManager"
        private const val SERVICE_ID = "com.heyyoung.solsol.settlement" // 앱 고유 ID
        private val STRATEGY = Strategy.P2P_STAR // 스타 토폴로지 사용
        private const val DISCOVERY_TIMEOUT_MS = 30_000L // 30초 타임아웃
        private const val USER_CLEANUP_INTERVAL_MS = 60_000L // 1분마다 정리
        private const val USER_EXPIRY_TIME_MS = 120_000L // 2분 후 만료
    }
    
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    // 상태 관리
    private val _discoveryState = MutableStateFlow(NearbyDiscoveryState())
    val discoveryState: StateFlow<NearbyDiscoveryState> = _discoveryState.asStateFlow()
    
    // 발견된 사용자들 (중복 제거)
    private val _discoveredUsers = MutableStateFlow<List<NearbyUser>>(emptyList())
    val discoveredUsers: StateFlow<List<NearbyUser>> = _discoveredUsers.asStateFlow()
    
    // 현재 사용자 프로필
    private var currentUserProfile: UserProfile? = null
    
    // 타이머 관리
    private var discoveryTimeoutJob: Job? = null
    private var userCleanupJob: Job? = null
    
    /**
     * 현재 사용자 프로필 로드 및 초기화
     */
    suspend fun initialize() {
        Log.d(TAG, "NearbyConnectionsManager 초기화 시작")
        try {
            when (val result = backendAuthRepository.getMyProfile()) {
                is com.heyyoung.solsol.core.network.BackendApiResult.Success -> {
                    currentUserProfile = result.data.toUserProfile()
                    Log.d(TAG, "사용자 프로필 로드 성공: ${currentUserProfile?.name}")
                }
                is com.heyyoung.solsol.core.network.BackendApiResult.Error -> {
                    Log.e(TAG, "사용자 프로필 로드 실패: ${result.message}")
                    // 실패 시 더미 프로필 (테스트용)
                    currentUserProfile = UserProfile(
                        userId = "test@ssafy.com",
                        name = "테스트 사용자",
                        department = "컴퓨터공학과",
                        studentNumber = "20251234"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "초기화 중 예외 발생: ${e.message}")
            updateErrorState("초기화에 실패했습니다: ${e.message}")
        }
    }
    
    /**
     * 주변 기기 검색 시작
     */
    fun startDiscovery() {
        Log.d(TAG, "🔍 주변 기기 검색 시작")
        
        if (currentUserProfile == null) {
            Log.e(TAG, "사용자 프로필이 로드되지 않음")
            updateErrorState("사용자 정보를 불러올 수 없습니다")
            return
        }
        
        _discoveryState.value = _discoveryState.value.copy(
            status = NearbyConnectionStatus.DISCOVERING,
            isSearching = true,
            error = null
        )
        
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build()
        
        connectionsClient
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Discovery 시작 성공")
                // 광고도 함께 시작 (다른 기기가 나를 찾을 수 있도록)
                startAdvertising()
                // 타임아웃과 정리 작업 시작
                startDiscoveryTimeout()
                startUserCleanup()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Discovery 시작 실패: ${exception.message}")
                updateErrorState("주변 기기 검색을 시작할 수 없습니다: ${exception.message}")
            }
    }
    
    /**
     * 자신을 광고하여 다른 기기가 발견할 수 있게 함
     */
    private fun startAdvertising() {
        Log.d(TAG, "📡 광고 시작")
        
        currentUserProfile?.let { profile ->
            val advertisingOptions = AdvertisingOptions.Builder()
                .setStrategy(STRATEGY)
                .build()
            
            // 사용자 프로필을 JSON으로 직렬화하여 광고
            val endpointName = profile.toJsonString()
            Log.d(TAG, "광고할 사용자 정보: $endpointName")
            
            connectionsClient
                .startAdvertising(endpointName, SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener {
                    Log.d(TAG, "✅ Advertising 시작 성공")
                    _discoveryState.value = _discoveryState.value.copy(
                        status = NearbyConnectionStatus.ADVERTISING
                    )
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "❌ Advertising 시작 실패: ${exception.message}")
                    updateErrorState("광고 시작 실패: ${exception.message}")
                }
        } ?: run {
            Log.e(TAG, "광고할 사용자 프로필이 없음")
            updateErrorState("사용자 정보를 불러올 수 없습니다")
        }
    }
    
    /**
     * 검색 및 광고 중지
     */
    fun stopDiscovery() {
        Log.d(TAG, "🛑 검색 및 광고 중지")
        
        // 타이머 작업들 취소
        discoveryTimeoutJob?.cancel()
        userCleanupJob?.cancel()
        
        connectionsClient.stopDiscovery()
        connectionsClient.stopAdvertising()
        connectionsClient.stopAllEndpoints()
        
        _discoveryState.value = _discoveryState.value.copy(
            status = NearbyConnectionStatus.IDLE,
            isSearching = false
        )
        
        // 발견된 사용자 목록 초기화
        _discoveredUsers.value = emptyList()
        
        Log.d(TAG, "✅ 모든 Nearby 연결 종료됨")
    }
    
    /**
     * 검색 타임아웃 설정 (30초 후 자동 중지)
     */
    private fun startDiscoveryTimeout() {
        discoveryTimeoutJob = coroutineScope.launch {
            delay(DISCOVERY_TIMEOUT_MS)
            Log.d(TAG, "⏰ 검색 타임아웃 - 자동 중지")
            stopDiscovery()
        }
    }
    
    /**
     * 만료된 사용자 정리 (주기적으로 실행)
     */
    private fun startUserCleanup() {
        userCleanupJob = coroutineScope.launch {
            while (true) {
                delay(USER_CLEANUP_INTERVAL_MS)
                cleanupExpiredUsers()
            }
        }
    }
    
    /**
     * 만료된 사용자 제거
     */
    private fun cleanupExpiredUsers() {
        val currentTime = System.currentTimeMillis()
        val activeUsers = _discoveredUsers.value.filter { user ->
            val isNotExpired = (currentTime - user.discoveredAt) < USER_EXPIRY_TIME_MS
            if (!isNotExpired) {
                Log.d(TAG, "🧹 만료된 사용자 제거: ${user.userProfile.name}")
            }
            isNotExpired
        }
        
        if (activeUsers.size != _discoveredUsers.value.size) {
            _discoveredUsers.value = activeUsers
            _discoveryState.value = _discoveryState.value.copy(
                discoveredUsers = activeUsers
            )
            Log.d(TAG, "🧹 사용자 정리 완료: ${activeUsers.size}명 유지")
        }
    }
    
    /**
     * 발견된 사용자 추가/업데이트
     */
    private fun addDiscoveredUser(nearbyUser: NearbyUser) {
        coroutineScope.launch {
            val currentUsers = _discoveredUsers.value.toMutableList()
            
            // 이미 존재하는 사용자인지 확인 (endpointId 기준)
            val existingIndex = currentUsers.indexOfFirst { it.endpointId == nearbyUser.endpointId }
            
            if (existingIndex != -1) {
                // 기존 사용자 업데이트
                currentUsers[existingIndex] = nearbyUser
                Log.d(TAG, "👤 사용자 정보 업데이트: ${nearbyUser.userProfile.name}")
            } else {
                // 새 사용자 추가
                currentUsers.add(nearbyUser)
                Log.d(TAG, "🆕 새 사용자 발견: ${nearbyUser.userProfile.name}")
            }
            
            _discoveredUsers.value = currentUsers
            
            // discovery 상태도 업데이트
            _discoveryState.value = _discoveryState.value.copy(
                discoveredUsers = currentUsers
            )
        }
    }
    
    /**
     * 사용자 제거 (연결 끊어졌을 때)
     */
    private fun removeDiscoveredUser(endpointId: String) {
        coroutineScope.launch {
            val currentUsers = _discoveredUsers.value.toMutableList()
            val removedUser = currentUsers.find { it.endpointId == endpointId }
            
            if (removedUser != null) {
                currentUsers.removeAll { it.endpointId == endpointId }
                _discoveredUsers.value = currentUsers
                
                Log.d(TAG, "👋 사용자 제거됨: ${removedUser.userProfile.name}")
                
                _discoveryState.value = _discoveryState.value.copy(
                    discoveredUsers = currentUsers
                )
            }
        }
    }
    
    /**
     * 에러 상태 업데이트
     */
    private fun updateErrorState(errorMessage: String) {
        _discoveryState.value = _discoveryState.value.copy(
            status = NearbyConnectionStatus.ERROR,
            error = errorMessage,
            isSearching = false
        )
    }
    
    /**
     * 기기 발견 콜백
     */
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "🔍 기기 발견: $endpointId, 이름: ${info.endpointName}")
            
            try {
                // JSON에서 사용자 프로필 파싱
                val userProfile = UserProfileCompanion.fromJsonString(info.endpointName)
                
                // 자기 자신은 제외
                if (userProfile.userId == currentUserProfile?.userId) {
                    Log.d(TAG, "⏭️ 자기 자신 발견 - 무시함")
                    return
                }
                
                // 호환성 확인
                if (currentUserProfile?.let { userProfile.isCompatibleWith(it) } != true) {
                    Log.w(TAG, "⚠️ 호환되지 않는 앱 버전: ${userProfile.appVersion}")
                }
                
                val nearbyUser = NearbyUser(
                    endpointId = endpointId,
                    userProfile = userProfile,
                    distance = "근거리", // Nearby API에서는 정확한 거리 제공하지 않음
                    isConnected = false,
                    discoveredAt = System.currentTimeMillis()
                )
                
                addDiscoveredUser(nearbyUser)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 사용자 프로필 파싱 실패: ${e.message}")
                Log.d(TAG, "원본 데이터: ${info.endpointName}")
            }
        }
        
        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "📡 기기 연결 끊어짐: $endpointId")
            removeDiscoveredUser(endpointId)
        }
    }
    
    /**
     * 연결 생명주기 콜백 (광고용)
     */
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d(TAG, "🤝 연결 요청 받음: $endpointId")
            // 정산 앱에서는 자동 연결하지 않고 사용자가 선택할 때만 연결
            // 여기서는 단순히 발견만 하므로 연결 요청을 거부
            connectionsClient.rejectConnection(endpointId)
        }
        
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Log.d(TAG, "🔗 연결 결과: $endpointId, 상태: ${result.status}")
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d(TAG, "✅ 연결 성공: $endpointId")
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d(TAG, "❌ 연결 거부됨: $endpointId")
                }
                else -> {
                    Log.w(TAG, "⚠️ 연결 실패: $endpointId")
                }
            }
        }
        
        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "👋 연결 해제됨: $endpointId")
            // 연결이 해제되어도 발견된 사용자 목록에서는 제거하지 않음
            // (단순 발견 목적이므로)
        }
    }
    
    /**
     * 현재 검색 중인지 확인
     */
    fun isDiscovering(): Boolean {
        return _discoveryState.value.isSearching
    }
    
    /**
     * 발견된 사용자 수 반환
     */
    fun getDiscoveredUserCount(): Int {
        return _discoveredUsers.value.size
    }
    
    /**
     * 특정 사용자 정보 가져오기
     */
    fun getUserById(endpointId: String): NearbyUser? {
        return _discoveredUsers.value.find { it.endpointId == endpointId }
    }
    
    /**
     * 검색 결과 초기화
     */
    fun clearDiscoveredUsers() {
        _discoveredUsers.value = emptyList()
        _discoveryState.value = _discoveryState.value.copy(
            discoveredUsers = emptyList()
        )
        Log.d(TAG, "🧹 발견된 사용자 목록 초기화")
    }
}