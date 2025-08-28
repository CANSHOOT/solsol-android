package com.heyyoung.solsol.feature.settlement.domain.game

import android.app.Application
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.heyyoung.solsol.core.auth.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NearbyManager private constructor(
    private val app: Application,
    private val tokenManager: TokenManager // ✅ 추가
) {

    companion object {
        private const val TAG = "NearbyManager"

        @Volatile
        private var INSTANCE: NearbyManager? = null

        fun get(app: Application, tokenManager: TokenManager): NearbyManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NearbyManager(app, tokenManager).also { INSTANCE = it }
            }
        }
    }

    private val connectionsClient by lazy { Nearby.getConnectionsClient(app) }
    private val strategy = Strategy.P2P_CLUSTER

    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val _discoveredRooms = MutableStateFlow<Map<String, String>>(emptyMap())
    val discoveredRooms: StateFlow<Map<String, String>> = _discoveredRooms.asStateFlow()

    private val _connected = MutableStateFlow<Map<String, String>>(emptyMap())
    val connected: StateFlow<Map<String, String>> = _connected.asStateFlow()

    private val _messages = MutableStateFlow<Pair<String, String>?>(null)
    val messages: StateFlow<Pair<String, String>?> = _messages.asStateFlow()

    /** 사용자 표시 이름(플레이어 이름) */
    var localEndpointName: String? = "User"

    /** 광고 시작 성공/실패 콜백 (선택) */
    var onAdvertisingEvent: ((success: Boolean, message: String?) -> Unit)? = null

    /** 광고 시작: endpointName(=playerName)으로 시작해야 함 */
    fun startAdvertising(serviceId: String, endpointName: String? = null) {
        val name = endpointName ?: localEndpointName ?: "Unknown"
        localEndpointName = name

        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
        Log.d(TAG, "startAdvertising(): endpointName=$name, serviceId=$serviceId")

        connectionsClient
            .startAdvertising(name, serviceId, connectionLifecycleCallback, options)
            .addOnSuccessListener {
                _isAdvertising.value = true
                Log.d(TAG, "✅ Advertising started")
                onAdvertisingEvent?.invoke(true, null)
            }
            .addOnFailureListener { e ->
                _isAdvertising.value = false
                val msg = (e as? com.google.android.gms.common.api.ApiException)?.statusCode?.let { "statusCode=$it" }
                    ?: (e.message ?: "startAdvertising failure")
                Log.e(TAG, "❌ Advertising failed: $msg", e)
                onAdvertisingEvent?.invoke(false, msg)
            }
    }

    /** 방 검색 시작 */
    fun startDiscovery(serviceId: String) {
        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
        Log.d(TAG, "startDiscovery(): serviceId=$serviceId")

        connectionsClient
            .startDiscovery(serviceId, endpointDiscoveryCallback, options)
            .addOnSuccessListener {
                _isDiscovering.value = true
                Log.d(TAG, "✅ Discovery started")
            }
            .addOnFailureListener { e ->
                _isDiscovering.value = false
                Log.e(TAG, "❌ Discovery failed: ${e.message}", e)
            }
    }

    fun stopAdvertising() {
        Log.d(TAG, "stopAdvertising()")
        connectionsClient.stopAdvertising()
        _isAdvertising.value = false
    }

    fun stopDiscovery() {
        Log.d(TAG, "stopDiscovery()")
        connectionsClient.stopDiscovery()
        _isDiscovering.value = false
        _discoveredRooms.value = emptyMap()
    }

    /** 참가자 측: 특정 엔드포인트에 연결 요청 */
    fun requestConnection(endpointId: String) {
        val name = localEndpointName ?: "User"
        Log.d(TAG, "requestConnection(): to=$endpointId, localName=$name")

        connectionsClient
            .requestConnection(name, endpointId, connectionLifecycleCallback)
            .addOnSuccessListener { Log.d(TAG, "requestConnection(): pending…") }
            .addOnFailureListener { e -> Log.e(TAG, "requestConnection() failed: ${e.message}", e) }
    }

    /** 단일 전송 */
    fun sendTo(endpointId: String, message: String) {
        val payload = Payload.fromBytes(message.toByteArray())
        Log.d(TAG, "sendTo(): to=$endpointId, bytes=${message.toByteArray().size}")
        connectionsClient.sendPayload(endpointId, payload)
    }

    /** 전체 전송 */
    fun sendToAll(message: String) {
        val endpointIds = _connected.value.keys.toList()
        if (endpointIds.isEmpty()) {
            Log.w(TAG, "sendToAll(): no connected endpoints")
            return
        }
        val payload = Payload.fromBytes(message.toByteArray())
        Log.d(TAG, "sendToAll(): targets=${endpointIds.size}, bytes=${message.toByteArray().size}")
        connectionsClient.sendPayload(endpointIds, payload)
    }

    fun disconnect(endpointId: String) {
        Log.d(TAG, "disconnect(): $endpointId")
        connectionsClient.disconnectFromEndpoint(endpointId)
        val current = _connected.value.toMutableMap()
        current.remove(endpointId)
        _connected.value = current
    }

    fun disconnectAll() {
        Log.d(TAG, "disconnectAll()")
        connectionsClient.stopAllEndpoints()
        _connected.value = emptyMap()
        _discoveredRooms.value = emptyMap()
        _isAdvertising.value = false
        _isDiscovering.value = false
    }

    // ---- Callbacks ----

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "onConnectionInitiated(): from=$endpointId, remoteName=${info.endpointName}")
            // 정책에 맞게 승인/거절. 현재는 자동 승인
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Log.d(TAG, "onConnectionResult(): $endpointId, status=${result.status.statusCode}")
            val current = _connected.value.toMutableMap()

            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    current[endpointId] = "Connected"
                    _connected.value = current

                    // ✅ userId 불러오기
                    CoroutineScope(Dispatchers.IO).launch {
                        val userInfo = tokenManager.getCurrentUserInfo()
                        val hello = Msg.Hello(
                            name = localEndpointName ?: "User",
                            userId = userInfo?.userId ?: "unknown"
                        ).toJson()

                        sendTo(endpointId, hello)
                    }
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    current.remove(endpointId); _connected.value = current
                }
                else -> {
                    current.remove(endpointId); _connected.value = current
                }
            }
        }
        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "onDisconnected(): $endpointId")
            val current = _connected.value.toMutableMap()
            current.remove(endpointId)
            _connected.value = current
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "onEndpointFound(): id=$endpointId, name=${info.endpointName}, serviceId=${info.serviceId}")
            val current = _discoveredRooms.value.toMutableMap()
            current[endpointId] = info.endpointName
            _discoveredRooms.value = current
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "onEndpointLost(): id=$endpointId")
            val current = _discoveredRooms.value.toMutableMap()
            current.remove(endpointId)
            _discoveredRooms.value = current
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val bytes = payload.asBytes() ?: return
                val message = String(bytes)
                Log.d(TAG, "onPayloadReceived(): from=$endpointId, bytes=${bytes.size}")
                _messages.value = Pair(endpointId, message)
            } else {
                Log.d(TAG, "onPayloadReceived(): non-bytes type=${payload.type}")
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // 필요 시 전송 진행률/상태 반영
        }
    }
}
