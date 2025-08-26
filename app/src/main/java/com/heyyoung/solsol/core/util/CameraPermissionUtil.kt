package com.heyyoung.solsol.core.util

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

object CameraPermissionUtil {

    /**
     * QRScanScreen 에서 쓰는 그대로의 콜백 형태로 구현.
     * - 처음 진입 시 1회 권한 요청
     * - 승인: onPermissionGranted()
     * - 거부(재요청 가능): onPermissionDenied()
     * - 영구 거부(다시는 묻지 않음): onPermissionDeniedPermanently()
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun RequestCameraPermission(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit,
        onPermissionDeniedPermanently: () -> Unit
    ) {
        val permissionState = rememberPermissionState(Manifest.permission.CAMERA)
        val requestedOnce = remember { mutableStateOf(false) }

        // 권한 상태가 바뀔 때마다 콜백 정리
        when (val status = permissionState.status) {
            PermissionStatus.Granted -> {
                onPermissionGranted()
            }
            is PermissionStatus.Denied -> {
                // 처음이면 1회 요청
                LaunchedEffect(Unit) {
                    if (!requestedOnce.value) {
                        requestedOnce.value = true
                        permissionState.launchPermissionRequest()
                    } else {
                        // 이미 한 번 요청했고 여전히 거부 상태
                        if (status.shouldShowRationale) {
                            onPermissionDenied()
                        } else {
                            // 보통 "다시는 묻지 않음" 케이스
                            onPermissionDeniedPermanently()
                        }
                    }
                }
            }
        }
    }
}
