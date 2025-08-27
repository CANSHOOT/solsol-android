package com.heyyoung.solsol.feature.settlement.domain.nearby

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NearbyPermissionManager @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "NearbyPermissionManager"
        
        // 필요한 권한들을 API 레벨별로 정의
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val BLUETOOTH_PERMISSIONS_LEGACY = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
        
        // Android 12 (API 31) 이상에서 필요한 권한
        val BLUETOOTH_PERMISSIONS_API_31 = if (Build.VERSION.SDK_INT >= 31) {
            arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else emptyArray()
        
        // Android 13 (API 33) 이상에서 필요한 WiFi 권한
        val WIFI_PERMISSIONS_API_33 = if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else emptyArray()
        
        // WiFi 관련 권한
        val WIFI_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )
    }
    
    /**
     * 현재 API 레벨에서 필요한 모든 권한 목록 반환
     */
    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()
        
        // 위치 권한 (필수)
        permissions.addAll(LOCATION_PERMISSIONS)
        
        // WiFi 권한 (필수)
        permissions.addAll(WIFI_PERMISSIONS)
        
        // Bluetooth 권한
        if (Build.VERSION.SDK_INT >= 31) {
            // Android 12 이상
            permissions.addAll(BLUETOOTH_PERMISSIONS_API_31)
            Log.d(TAG, "Android 12+ Bluetooth 권한 추가")
        } else {
            // Android 11 이하
            permissions.addAll(BLUETOOTH_PERMISSIONS_LEGACY)
            Log.d(TAG, "Legacy Bluetooth 권한 추가")
        }
        
        // WiFi 권한 (Android 13+)
        if (Build.VERSION.SDK_INT >= 33) {
            permissions.addAll(WIFI_PERMISSIONS_API_33)
            Log.d(TAG, "Android 13+ WiFi 권한 추가")
        }
        
        Log.d(TAG, "필요한 권한 목록: ${permissions.joinToString(", ")}")
        return permissions.toTypedArray()
    }
    
    /**
     * 특정 권한이 허용되었는지 확인
     */
    fun isPermissionGranted(permission: String): Boolean {
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "권한 확인 - $permission: ${if (granted) "허용됨" else "거부됨"}")
        return granted
    }
    
    /**
     * 모든 필수 권한이 허용되었는지 확인
     */
    fun areAllPermissionsGranted(): Boolean {
        val requiredPermissions = getRequiredPermissions()
        val allGranted = requiredPermissions.all { isPermissionGranted(it) }
        
        Log.d(TAG, "모든 권한 확인 결과: ${if (allGranted) "모두 허용됨" else "일부 거부됨"}")
        
        if (!allGranted) {
            val deniedPermissions = requiredPermissions.filter { !isPermissionGranted(it) }
            Log.w(TAG, "거부된 권한들: ${deniedPermissions.joinToString(", ")}")
        }
        
        return allGranted
    }
    
    /**
     * 위치 권한이 허용되었는지 확인
     */
    fun isLocationPermissionGranted(): Boolean {
        return LOCATION_PERMISSIONS.any { isPermissionGranted(it) }
    }
    
    /**
     * Bluetooth 권한이 허용되었는지 확인
     */
    fun isBluetoothPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 31) {
            BLUETOOTH_PERMISSIONS_API_31.all { isPermissionGranted(it) }
        } else {
            BLUETOOTH_PERMISSIONS_LEGACY.all { isPermissionGranted(it) }
        }
    }
    
    /**
     * WiFi 권한이 허용되었는지 확인
     */
    fun isWifiPermissionGranted(): Boolean {
        val basicWifiPermissions = WIFI_PERMISSIONS.all { isPermissionGranted(it) }
        val api33WifiPermissions = if (Build.VERSION.SDK_INT >= 33) {
            WIFI_PERMISSIONS_API_33.all { isPermissionGranted(it) }
        } else true
        
        return basicWifiPermissions && api33WifiPermissions
    }
    
    /**
     * 거부된 권한 목록 반환
     */
    fun getDeniedPermissions(): List<String> {
        return getRequiredPermissions().filter { !isPermissionGranted(it) }
    }
    
    /**
     * 권한 상태를 문자열로 반환 (디버깅용)
     */
    fun getPermissionStatusSummary(): String {
        val summary = StringBuilder()
        summary.append("=== Nearby API 권한 상태 ===\n")
        
        summary.append("위치 권한: ${if (isLocationPermissionGranted()) "✅" else "❌"}\n")
        summary.append("Bluetooth 권한: ${if (isBluetoothPermissionGranted()) "✅" else "❌"}\n")
        summary.append("WiFi 권한: ${if (isWifiPermissionGranted()) "✅" else "❌"}\n")
        
        val deniedPermissions = getDeniedPermissions()
        if (deniedPermissions.isNotEmpty()) {
            summary.append("\n거부된 권한:\n")
            deniedPermissions.forEach { permission ->
                summary.append("- $permission\n")
            }
        }
        
        return summary.toString()
    }
    
    /**
     * 권한 상태 로그 출력
     */
    fun logPermissionStatus() {
        Log.d(TAG, getPermissionStatusSummary())
    }
    
    /**
     * 사용자에게 보여줄 권한 설명 메시지 생성
     */
    fun getPermissionRationaleMessage(): String {
        val deniedPermissions = getDeniedPermissions()
        
        if (deniedPermissions.isEmpty()) {
            return "모든 권한이 허용되었습니다."
        }
        
        val messages = mutableListOf<String>()
        
        if (!isLocationPermissionGranted()) {
            messages.add("• 위치 권한: 주변 기기를 찾기 위해 필요합니다")
        }
        
        if (!isBluetoothPermissionGranted()) {
            messages.add("• Bluetooth 권한: 근거리 기기와 연결하기 위해 필요합니다")
        }
        
        if (!isWifiPermissionGranted()) {
            messages.add("• WiFi 권한: WiFi를 통한 기기 검색에 필요합니다")
        }
        
        return "정산 참여자를 주변에서 찾기 위해 다음 권한이 필요합니다:\n\n${messages.joinToString("\n")}"
    }
}