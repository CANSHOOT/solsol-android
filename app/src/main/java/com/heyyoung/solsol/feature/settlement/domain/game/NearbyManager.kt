package com.heyyoung.solsol.feature.settlement.domain.game

import android.app.Application
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NearbyManager private constructor(private val app: Application) {
    
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
    
    var localEndpointName: String = "User"
    
    companion object {
        @Volatile
        private var INSTANCE: NearbyManager? = null
        
        fun get(app: Application): NearbyManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NearbyManager(app).also { INSTANCE = it }
            }
        }
    }
    
    fun startAdvertising(serviceId: String, roomTitle: String) {
        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startAdvertising(
            roomTitle, serviceId, connectionLifecycleCallback, options
        ).addOnSuccessListener {
            _isAdvertising.value = true
        }.addOnFailureListener {
            _isAdvertising.value = false
        }
    }
    
    fun startDiscovery(serviceId: String) {
        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startDiscovery(serviceId, endpointDiscoveryCallback, options)
            .addOnSuccessListener {
                _isDiscovering.value = true
            }.addOnFailureListener {
                _isDiscovering.value = false
            }
    }
    
    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
        _isAdvertising.value = false
    }
    
    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        _isDiscovering.value = false
        _discoveredRooms.value = emptyMap()
    }
    
    fun requestConnection(endpointId: String) {
        connectionsClient.requestConnection(
            localEndpointName, endpointId, connectionLifecycleCallback
        )
    }
    
    fun sendTo(endpointId: String, message: String) {
        val payload = Payload.fromBytes(message.toByteArray())
        connectionsClient.sendPayload(endpointId, payload)
    }
    
    fun sendToAll(message: String) {
        val payload = Payload.fromBytes(message.toByteArray())
        val endpointIds = _connected.value.keys.toList()
        if (endpointIds.isNotEmpty()) {
            connectionsClient.sendPayload(endpointIds, payload)
        }
    }
    
    fun disconnect(endpointId: String) {
        connectionsClient.disconnectFromEndpoint(endpointId)
    }
    
    fun disconnectAll() {
        connectionsClient.stopAllEndpoints()
        _connected.value = emptyMap()
        _discoveredRooms.value = emptyMap()
        _isAdvertising.value = false
        _isDiscovering.value = false
    }
    
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }
        
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    val currentConnected = _connected.value.toMutableMap()
                    currentConnected[endpointId] = "Connected"
                    _connected.value = currentConnected
                    
                    val hello = Msg.Hello(localEndpointName).toJson()
                    sendTo(endpointId, hello)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    val currentConnected = _connected.value.toMutableMap()
                    currentConnected.remove(endpointId)
                    _connected.value = currentConnected
                }
                else -> {
                    val currentConnected = _connected.value.toMutableMap()
                    currentConnected.remove(endpointId)
                    _connected.value = currentConnected
                }
            }
        }
        
        override fun onDisconnected(endpointId: String) {
            val currentConnected = _connected.value.toMutableMap()
            currentConnected.remove(endpointId)
            _connected.value = currentConnected
        }
    }
    
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val currentDiscovered = _discoveredRooms.value.toMutableMap()
            currentDiscovered[endpointId] = info.endpointName
            _discoveredRooms.value = currentDiscovered
        }
        
        override fun onEndpointLost(endpointId: String) {
            val currentDiscovered = _discoveredRooms.value.toMutableMap()
            currentDiscovered.remove(endpointId)
            _discoveredRooms.value = currentDiscovered
        }
    }
    
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes ->
                val message = String(bytes)
                _messages.value = Pair(endpointId, message)
            }
        }
        
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        }
    }
}