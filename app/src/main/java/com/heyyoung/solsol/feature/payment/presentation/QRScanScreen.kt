package com.heyyoung.solsol.feature.payment.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.heyyoung.solsol.core.util.CameraPermissionUtil
import com.heyyoung.solsol.feature.payment.presentation.components.CameraPreview
import com.heyyoung.solsol.feature.payment.presentation.components.QROverlay
import com.heyyoung.solsol.feature.payment.presentation.components.QRScanTopBar
import com.heyyoung.solsol.feature.payment.presentation.components.UsageBottomSheet

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScanScreen(
    onNavigateBack: () -> Unit = {},
    onQRScanned: (String) -> Unit = {}, // ← 이 파라미터 추가
    modifier: Modifier = Modifier
) {
    val TAG = "QRScanScreen"

    // QR 스캔 상태 관리
    var scannedQRData by remember { mutableStateOf<String?>(null) }

    // 하단 시트 상태 관리
    var bottomSheetOffset by remember { mutableFloatStateOf(0f) }
    var isBottomSheetExpanded by remember { mutableStateOf(false) }

    // 권한 상태 관리
    var hasPermission by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    Log.d(TAG, "QR 스캔 화면 진입")

    // 카메라 권한 요청
    CameraPermissionUtil.RequestCameraPermission(
        onPermissionGranted = {
            Log.d(TAG, "카메라 권한 승인됨")
            hasPermission = true
        },
        onPermissionDenied = {
            Log.w(TAG, "카메라 권한 거부됨")
            showPermissionDialog = true
        },
        onPermissionDeniedPermanently = {
            Log.e(TAG, "카메라 권한 영구 거부됨")
            showPermissionDialog = true
        }
    )

    // QR 스캔 완료 시 외부로 전달 (MainActivity에서 처리)
    Box(modifier = modifier.fillMaxSize()) {
        QRScanLayout(
            hasPermission = hasPermission,
            bottomSheetOffset = bottomSheetOffset,
            onBottomSheetOffsetChange = { bottomSheetOffset = it },
            onBottomSheetExpandedChange = { isBottomSheetExpanded = it },
            onNavigateBack = onNavigateBack,
            onQRScanned = { qrData ->
                Log.d(TAG, "QR 스캔됨: $qrData")
                onQRScanned(qrData) // ← 외부(MainActivity)로 전달
            }
        )

        // 권한 요청 다이얼로그
        if (showPermissionDialog) {
            PermissionDialog(
                onDismiss = {
                    showPermissionDialog = false
                    onNavigateBack()
                },
                onConfirm = {
                    showPermissionDialog = false
                    // 설정 화면으로 이동 로직 추가 가능
                }
            )
        }
    }
}

/**
 * QR 스캔 레이아웃 구조
 */
@Composable
private fun QRScanLayout(
    hasPermission: Boolean,
    bottomSheetOffset: Float,
    onBottomSheetOffsetChange: (Float) -> Unit,
    onBottomSheetExpandedChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onQRScanned: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 카메라(배경)
        if (hasPermission) {
            CameraPreview(
                onQRCodeScanned = onQRScanned,
                modifier = Modifier.fillMaxSize()
            )
            QROverlay(Modifier.fillMaxSize())
        } else {
            PermissionWaitingScreen()
        }

        // 상단 앱바(오버레이)
        QRScanTopBar(
            onBackClick = onNavigateBack,
        )

        // 하단 시트(오버레이)
        UsageBottomSheet(
            offset = bottomSheetOffset,
            onOffsetChange = onBottomSheetOffsetChange,
            onExpandedChange = onBottomSheetExpandedChange,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
    }
}

/**
 * 권한 대기 화면
 */
@Composable
private fun PermissionWaitingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "카메라 권한을 확인하는 중...",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * 권한 요청 다이얼로그
 */
@Composable
private fun PermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("카메라 권한 필요")
        },
        text = {
            Text("QR 코드 스캔을 위해 카메라 권한이 필요합니다.\n설정에서 권한을 허용해주세요.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("설정으로 이동")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}