package com.heyyoung.solsol.feature.dutchpay.presentation.nearby

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.heyyoung.solsol.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.heyyoung.solsol.feature.dutchpay.domain.model.User
import com.heyyoung.solsol.feature.dutchpay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val userRepository: UserRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "NearbyViewModel"
        private const val SERVICE_ID = "com.heyyoung.solsol.dutchpay"
        private val STRATEGY = Strategy.P2P_STAR
    }

    private val connectionsClient = Nearby.getConnectionsClient(context)

    // 현재 사용자 정보
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // 상태 관리
    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val _discoveredUsers = MutableStateFlow<Set<User>>(emptySet())
    val discoveredUsers: StateFlow<Set<User>> = _discoveredUsers.asStateFlow()

    private val _connectedEndpoint = MutableStateFlow<String?>(null)
    val connectedEndpoint: StateFlow<String?> = _connectedEndpoint.asStateFlow()

    // 발견 콜백
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "🔍 기기 발견: $endpointId, 이름: ${info.endpointName}")
            
            // 자신이 아닌 경우에만 처리
            val myName = getCurrentUserName()
            Log.d(TAG, "🔍 내 이름: '$myName', 발견된 이름: '${info.endpointName}'")
            if (info.endpointName != myName) {
                // endpointName은 "사용자이름|사용자ID|학번|학과" 형태로 전송됨
                val userInfo = info.endpointName.split("|")
                Log.d(TAG, "📄 사용자 정보 파싱: $userInfo")
                if (userInfo.size >= 4) {
                    val discoveredUser = User(
                        userId = userInfo[1],
                        name = userInfo[0],
                        studentNumber = userInfo[2],
                        departmentId = 0L, // 기본값
                        departmentName = userInfo[3],
                        councilId = 0L, // 기본값
                        accountNo = "", // 기본값
                        accountBalance = 0L, // 기본값
                        councilOfficer = false // 기본값
                    )
                    
                    _discoveredUsers.value = _discoveredUsers.value + discoveredUser
                    Log.d(TAG, "👤 사용자 추가: ${discoveredUser.name} (${discoveredUser.studentNumber})")
                    Toast.makeText(context, "👤 ${discoveredUser.name} 발견!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "❌ 잘못된 사용자 정보 형식: ${info.endpointName}")
                    Toast.makeText(context, "❌ 잘못된 사용자 데이터: ${info.endpointName}", Toast.LENGTH_LONG).show()
                }
            } else {
                Log.d(TAG, "🙅 자기 자신으로 인식하여 무시: '${info.endpointName}'")
                Toast.makeText(context, "🙅 자기 자신 기기는 무시합니다", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "📱 기기 연결 끊김: $endpointId")
            // 특정 endpointId와 연결된 사용자를 목록에서 제거해야 하지만
            // 현재 구조상 endpointId와 User의 매핑이 없으므로 전체 목록을 재구성
        }
    }

    // 연결 콜백 (광고 시에도 사용됨)
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "🤝 연결 요청받음: $endpointId")
            // 자동으로 연결 수락 (보안상 실제로는 사용자 확인 필요)
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d(TAG, "✅ 연결 성공: $endpointId")
                    _connectedEndpoint.value = endpointId
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d(TAG, "❌ 연결 거절됨: $endpointId")
                }
                else -> {
                    Log.d(TAG, "🔄 연결 실패: $endpointId, 코드: ${result.status.statusCode}")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "🔌 연결 해제됨: $endpointId")
            _connectedEndpoint.value = null
        }
    }

    // 페이로드 콜백 (메시지 교환용 - 필요시 구현)
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            // 추후 메시지 교환이 필요한 경우 구현
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // 전송 상태 업데이트 처리
        }
    }

    /**
     * 현재 사용자 정보 로드 - users/{userId} API 사용
     */
    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                // 1. 먼저 현재 로그인된 userId 확인
                val currentUserId = getCurrentUserUseCase.getCurrentUserId()
                if (currentUserId.isNullOrEmpty()) {
                    Log.e(TAG, "❌ 로그인되지 않음")
                    Toast.makeText(context, "❌ 로그인이 필요합니다", Toast.LENGTH_LONG).show()
                    return@launch
                }
                
                // 2. userId를 Long으로 변환 (이메일에서 숫자 추출 또는 다른 방법 필요)
                // 현재는 String userId를 그대로 사용하고 비동기 방식으로 대체
                
                // 3. UserRepository를 통해 users/{userId} API로 완전한 사용자 정보 조회
                val userResult = userRepository.getUserByStringId(currentUserId)
                userResult.fold(
                    onSuccess = { dutchPayUser ->
                        Toast.makeText(context, "👤 사용자: ${dutchPayUser.name}", Toast.LENGTH_SHORT).show()
                        _currentUser.value = dutchPayUser
                        Log.d(TAG, "👤 현재 사용자 로드 완료: ${dutchPayUser.name} (${dutchPayUser.studentNumber})")
                        Log.d(TAG, "💳 계좌 정보: ${dutchPayUser.accountNo}, 잔액: ${dutchPayUser.accountBalance}")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ users/{userId} API 실패, auth/me로 대체 시도")
                        
                        // 폴백: auth/me API 사용
                        val authUser = getCurrentUserUseCase().getOrNull()
                        if (authUser != null) {
                            Toast.makeText(context, "⚠️ 기본 사용자 정보 사용: ${authUser.name}", Toast.LENGTH_SHORT).show()
                            
                            // AuthUser를 DutchPay User로 변환
                            val dutchPayUser = User(
                                userId = authUser.userId,
                                studentNumber = authUser.studentNumber,
                                name = authUser.name,
                                departmentId = 0L,
                                departmentName = authUser.departmentName,
                                councilId = authUser.councilId,
                                accountNo = "", // auth/me에서는 없음
                                accountBalance = 0L, // auth/me에서는 없음
                                councilOfficer = authUser.isCouncilOfficer
                            )
                            _currentUser.value = dutchPayUser
                            Log.d(TAG, "👤 대체 사용자 로드 완료: ${dutchPayUser.name} (${dutchPayUser.studentNumber})")
                        } else {
                            Log.e(TAG, "❌ auth/me도 실패")
                            Toast.makeText(context, "❌ 모든 사용자 정보 로드 실패", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ 사용자 정보 로드 실패: ${e.message}", e)
                Toast.makeText(context, "❌ 사용자 정보 로드 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 광고 시작 - 다른 기기가 이 기기를 발견할 수 있게 함
     */
    fun startAdvertising() {
        if (_isAdvertising.value) {
            Log.d(TAG, "📢 이미 광고 중")
            return
        }

        val userName = getCurrentUserName()
        if (userName.isEmpty()) {
            Log.e(TAG, "📢 광고 시작 실패: 사용자 정보 없음")
            Toast.makeText(context, "❌ 사용자 정보가 없어서 광고 시작 실패", Toast.LENGTH_LONG).show()
            return
        }

        viewModelScope.launch {
            try {
                val advertisingOptions = AdvertisingOptions.Builder()
                    .setStrategy(STRATEGY)
                    .build()

                connectionsClient.startAdvertising(
                    userName,
                    SERVICE_ID,
                    connectionLifecycleCallback,
                    advertisingOptions
                ).addOnSuccessListener(OnSuccessListener<Void> {
                    Log.d(TAG, "📢 광고 시작 성공")
                    Toast.makeText(context, "📢 내 정보 공유 시작", Toast.LENGTH_SHORT).show()
                    _isAdvertising.value = true
                }).addOnFailureListener(OnFailureListener { exception ->
                    Log.e(TAG, "📢 광고 시작 실패", exception)
                    Toast.makeText(context, "❌ 광고 시작 실패: ${exception.message}", Toast.LENGTH_LONG).show()
                    _isAdvertising.value = false
                })
            } catch (e: Exception) {
                Log.e(TAG, "📢 광고 시작 예외", e)
                _isAdvertising.value = false
            }
        }
    }

    /**
     * 주변 기기 탐색 시작
     */
    fun startDiscovery() {
        if (_isDiscovering.value) {
            Log.d(TAG, "🔍 이미 탐색 중")
            return
        }

        viewModelScope.launch {
            try {
                val discoveryOptions = DiscoveryOptions.Builder()
                    .setStrategy(STRATEGY)
                    .build()

                _discoveredUsers.value = emptySet() // 기존 목록 초기화

                connectionsClient.startDiscovery(
                    SERVICE_ID,
                    endpointDiscoveryCallback,
                    discoveryOptions
                ).addOnSuccessListener(OnSuccessListener<Void> {
                    Log.d(TAG, "🔍 탐색 시작 성공")
                    Toast.makeText(context, "🔍 주변 검색 시작", Toast.LENGTH_SHORT).show()
                    _isDiscovering.value = true
                }).addOnFailureListener(OnFailureListener { exception ->
                    Log.e(TAG, "🔍 탐색 시작 실패", exception)
                    Toast.makeText(context, "❌ 검색 시작 실패: ${exception.message}", Toast.LENGTH_LONG).show()
                    _isDiscovering.value = false
                })
            } catch (e: Exception) {
                Log.e(TAG, "🔍 탐색 시작 예외", e)
                _isDiscovering.value = false
            }
        }
    }

    /**
     * 광고 중지
     */
    fun stopAdvertising() {
        if (!_isAdvertising.value) return

        connectionsClient.stopAdvertising()
        _isAdvertising.value = false
        Log.d(TAG, "📢 광고 중지")
    }

    /**
     * 탐색 중지
     */
    fun stopDiscovery() {
        if (!_isDiscovering.value) return

        connectionsClient.stopDiscovery()
        _isDiscovering.value = false
        Log.d(TAG, "🔍 탐색 중지")
    }

    /**
     * 모든 연결 해제
     */
    fun stopAllConnections() {
        connectionsClient.stopAllEndpoints()
        _connectedEndpoint.value = null
        Log.d(TAG, "🔌 모든 연결 해제")
    }

    /**
     * 발견된 사용자 목록 초기화
     */
    fun clearDiscoveredUsers() {
        _discoveredUsers.value = emptySet()
    }

    /**
     * 현재 사용자 이름을 형식에 맞게 반환
     * 형식: "사용자이름|사용자ID|학번|학과"
     */
    private fun getCurrentUserName(): String {
        return _currentUser.value?.let { user ->
            "${user.name}|${user.userId}|${user.studentNumber}|${user.departmentName}"
        } ?: ""
    }

    override fun onCleared() {
        super.onCleared()
        stopAdvertising()
        stopDiscovery()
        stopAllConnections()
    }
}