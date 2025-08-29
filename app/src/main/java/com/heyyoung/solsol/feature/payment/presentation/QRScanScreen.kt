package com.heyyoung.solsol.feature.payment.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.BackHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.heyyoung.solsol.R
import com.heyyoung.solsol.core.util.CameraPermissionUtil
import com.heyyoung.solsol.feature.payment.presentation.components.CameraPreview
import com.heyyoung.solsol.feature.payment.presentation.components.QRScanTopBar
import com.heyyoung.solsol.feature.payment.presentation.components.UsageBottomSheet
import com.heyyoung.solsol.feature.qr.QROverlay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScanScreen(
    onNavigateBack: () -> Unit = {},
    onQRScanned: (String) -> Unit = {},
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

    // 하드웨어/제스처 뒤로가기 버튼 처리
    BackHandler {
        onNavigateBack()
    }

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
                onQRScanned(qrData)
            }
        )

        // 권한 요청 다이얼로그
        if (showPermissionDialog) {
            ModernPermissionDialog(
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
            ModernPermissionWaitingScreen()
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
 * 현대적인 권한 대기 화면
 */
@Composable
private fun ModernPermissionWaitingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(40.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = colorResource(id = R.color.solsol_purple_30),
                    ambientColor = colorResource(id = R.color.solsol_purple_30)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.solsol_white_50)
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.solsol_purple),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "카메라 권한을 확인하는 중...",
                    color = colorResource(id = R.color.solsol_white),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "잠시만 기다려주세요",
                    color = colorResource(id = R.color.solsol_white),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 현대적인 권한 요청 다이얼로그
 */
@Composable
private fun ModernPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = colorResource(id = R.color.solsol_purple_30),
                    ambientColor = colorResource(id = R.color.solsol_purple_30)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.solsol_card_white)
            )
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 아이콘 영역
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(32.dp),
                            spotColor = colorResource(id = R.color.solsol_purple_30)
                        )
                        .background(
                            color = colorResource(id = R.color.solsol_purple),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📷",
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "카메라 권한 필요",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.solsol_dark_text),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "QR 코드 스캔을 위해 카메라 권한이 필요합니다.\n설정에서 권한을 허용해주세요.",
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.solsol_gray_text),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 취소 버튼
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            colorResource(id = R.color.solsol_light_gray)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorResource(id = R.color.solsol_gray_text)
                        )
                    ) {
                        Text(
                            text = "취소",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 확인 버튼
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = colorResource(id = R.color.solsol_purple_30)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.solsol_purple)
                        )
                    ) {
                        Text(
                            text = "설정으로 이동",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.solsol_white)
                        )
                    }
                }
            }
        }
    }
}