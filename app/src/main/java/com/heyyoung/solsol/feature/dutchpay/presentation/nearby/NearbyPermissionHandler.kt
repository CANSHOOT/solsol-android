package com.heyyoung.solsol.feature.dutchpay.presentation.nearby

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckNearbyPermissions(
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: (List<String>) -> Unit
) {
    val context = LocalContext.current
    
    // 필요한 권한 목록
    val requiredPermissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        add(Manifest.permission.ACCESS_WIFI_STATE)
        add(Manifest.permission.CHANGE_WIFI_STATE)
        
        // Android 12 이상에서 필요한 권한들
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
        }
        
        // Android 13 이상에서 필요한 권한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
    }

    // 권한 상태 관리
    val multiplePermissionsState = rememberMultiplePermissionsState(requiredPermissions)
    
    // 블루투스 활성화 확인
    var isBluetoothEnabled by remember { mutableStateOf(isBluetoothEnabled(context)) }
    
    // 위치 서비스 활성화 확인
    var isLocationEnabled by remember { mutableStateOf(isLocationEnabled(context)) }
    
    // 블루투스 활성화 런처
    val bluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isBluetoothEnabled = isBluetoothEnabled(context)
        Log.d("NearbyPermissionHandler", "블루투스 활성화 결과: $isBluetoothEnabled")
    }
    
    // 위치 서비스 활성화 런처
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLocationEnabled = isLocationEnabled(context)
        Log.d("NearbyPermissionHandler", "위치 서비스 활성화 결과: $isLocationEnabled")
    }

    // 권한 체크 및 처리
    LaunchedEffect(
        multiplePermissionsState.allPermissionsGranted,
        isBluetoothEnabled,
        isLocationEnabled
    ) {
        Log.d("NearbyPermissionHandler", "권한 상태 체크")
        Log.d("NearbyPermissionHandler", "- 모든 권한 허용: ${multiplePermissionsState.allPermissionsGranted}")
        Log.d("NearbyPermissionHandler", "- 블루투스 활성화: $isBluetoothEnabled")
        Log.d("NearbyPermissionHandler", "- 위치 서비스 활성화: $isLocationEnabled")
        
        when {
            multiplePermissionsState.allPermissionsGranted && isBluetoothEnabled && isLocationEnabled -> {
                Log.d("NearbyPermissionHandler", "✅ 모든 권한 및 설정이 준비됨")
                onPermissionsGranted()
            }
            !multiplePermissionsState.allPermissionsGranted -> {
                val deniedPermissions = multiplePermissionsState.permissions
                    .filter { !it.status.isGranted }
                    .map { it.permission }
                
                Log.d("NearbyPermissionHandler", "❌ 거절된 권한: $deniedPermissions")
                onPermissionsDenied(deniedPermissions)
            }
            !isBluetoothEnabled -> {
                Log.d("NearbyPermissionHandler", "📶 블루투스 비활성화 - 활성화 요청")
                requestBluetoothEnable(bluetoothLauncher)
            }
            !isLocationEnabled -> {
                Log.d("NearbyPermissionHandler", "📍 위치 서비스 비활성화 - 활성화 요청")
                requestLocationEnable(locationLauncher)
            }
        }
    }

    // 권한 요청
    LaunchedEffect(Unit) {
        if (!multiplePermissionsState.allPermissionsGranted) {
            Log.d("NearbyPermissionHandler", "권한 요청 시작")
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    }
}

/**
 * 블루투스 활성화 상태 확인
 */
private fun isBluetoothEnabled(context: Context): Boolean {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter = bluetoothManager?.adapter
    return bluetoothAdapter?.isEnabled == true
}

/**
 * 위치 서비스 활성화 상태 확인
 */
private fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return LocationManagerCompat.isLocationEnabled(locationManager)
}

/**
 * 블루투스 활성화 요청
 */
private fun requestBluetoothEnable(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    launcher.launch(enableBluetoothIntent)
}

/**
 * 위치 서비스 활성화 요청
 */
private fun requestLocationEnable(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val locationSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    launcher.launch(locationSettingsIntent)
}

/**
 * 권한 상태 체크 결과를 나타내는 데이터 클래스
 */
data class NearbyPermissionState(
    val hasAllPermissions: Boolean,
    val isBluetoothEnabled: Boolean,
    val isLocationEnabled: Boolean,
    val deniedPermissions: List<String> = emptyList()
) {
    val isReady: Boolean
        get() = hasAllPermissions && isBluetoothEnabled && isLocationEnabled
}

/**
 * Nearby Connections에 필요한 모든 권한과 설정을 체크
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberNearbyPermissionState(): NearbyPermissionState {
    val context = LocalContext.current
    
    // 필요한 권한 목록
    val requiredPermissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        add(Manifest.permission.ACCESS_WIFI_STATE)
        add(Manifest.permission.CHANGE_WIFI_STATE)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
    }

    val multiplePermissionsState = rememberMultiplePermissionsState(requiredPermissions)
    
    var isBluetoothEnabled by remember { mutableStateOf(isBluetoothEnabled(context)) }
    var isLocationEnabled by remember { mutableStateOf(isLocationEnabled(context)) }
    
    // 주기적으로 블루투스와 위치 서비스 상태를 업데이트
    LaunchedEffect(Unit) {
        // 상태 업데이트 로직 (필요시 추가)
    }
    
    return NearbyPermissionState(
        hasAllPermissions = multiplePermissionsState.allPermissionsGranted,
        isBluetoothEnabled = isBluetoothEnabled,
        isLocationEnabled = isLocationEnabled,
        deniedPermissions = multiplePermissionsState.permissions
            .filter { !it.status.isGranted }
            .map { it.permission }
    )
}