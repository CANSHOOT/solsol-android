package com.heyyoung.solsol.feature.payment.presentation.components

import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

private const val TAG = "CameraPreview"

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    onQRCodeScanned: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val onScanned = rememberUpdatedState(onQRCodeScanned)

    // 한 번만 스캔
    val scannedOnce = remember { mutableStateOf(false) }

    // 분석 전용 스레드 (메인 차단 방지)
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // 프리뷰 뷰와 바인딩된 프로바이더를 기억해 두었다가 해제
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var boundProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // ML Kit 스캐너(재사용)
    val scanner = remember {
        val opts = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(opts)
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // View가 만들어지면 카메라 시작 (한 번만)
        LaunchedEffect(previewView) {
            val view = previewView ?: return@LaunchedEffect
            val provider = ProcessCameraProvider.getInstance(context).get()
            boundProvider = provider

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                try {
                    if (scannedOnce.value) return@setAnalyzer

                    val media = imageProxy.image ?: return@setAnalyzer
                    val image = InputImage.fromMediaImage(
                        media,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    scanner.process(image)
                        .addOnSuccessListener { list ->
                            val raw = list.firstOrNull { it.rawValue != null }?.rawValue
                            if (!raw.isNullOrEmpty() && !scannedOnce.value) {
                                scannedOnce.value = true
                                Log.d(TAG, "QR detected: $raw")
                                onScanned.value(raw)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "barcode processing failed", e)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } catch (t: Throwable) {
                    imageProxy.close()
                }
            }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera bind failed", e)
            }
        }

        // 가짜 스캔 버튼(노트북/에뮬 테스트용, 유지)
        Column(
            Modifier.align(Alignment.Center).padding(top = 160.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (!scannedOnce.value) {
                        scannedOnce.value = true
                        onScanned.value("TEST_QR_CODE_12345")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            ) { Text("테스트 QR 스캔", color = Color.White) }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { boundProvider?.unbindAll() } catch (_: Exception) {}
            cameraExecutor.shutdown()
        }
    }
}
