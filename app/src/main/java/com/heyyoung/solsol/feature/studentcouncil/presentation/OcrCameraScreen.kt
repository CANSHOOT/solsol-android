package com.heyyoung.solsol.feature.studentcouncil.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "OcrCameraScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrCameraScreen(
    onNavigateBack: () -> Unit = {},
    // 기존 시그니처 유지(호환용)
    onOcrResult: (OcrResult) -> Unit = {},
    // 테스트 페이지로 바로 이동하고 싶을 때 사용 (imageUri, ocrText, parsed)
    onNavigateToTest: ((Uri, String, ReceiptFields) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var uiState by remember { mutableStateOf(OcrCameraUiState()) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "카메라 권한 승인")
            uiState = uiState.copy(hasPermission = true)
        } else {
            Log.w(TAG, "카메라 권한 거부")
            uiState = uiState.copy(hasPermission = false, showPermissionDialog = true)
        }
    }

    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                Log.d(TAG, "카메라 권한 이미 있음")
                uiState = uiState.copy(hasPermission = true)
            }
            else -> {
                Log.d(TAG, "카메라 권한 요청")
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    LaunchedEffect(uiState.hasPermission) {
        if (uiState.hasPermission) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                cameraProvider = cameraProviderFuture.get()
                Log.d(TAG, "CameraProvider 준비완료")
            }, ContextCompat.getMainExecutor(context))
        }
    }

    if (uiState.showPermissionDialog) {
        AlertDialog(
            onDismissRequest = onNavigateBack,
            title = { Text("카메라 권한 필요") },
            text = { Text("영수증을 촬영하려면 카메라 권한이 필요합니다.") },
            confirmButton = {
                TextButton(onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                    uiState = uiState.copy(showPermissionDialog = false)
                }) { Text("허용") }
            },
            dismissButton = { TextButton(onClick = onNavigateBack) { Text("취소") } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 상단바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", tint = Color.White)
            }
            Text("영수증 촬영", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.size(48.dp))
        }

        if (uiState.hasPermission && cameraProvider != null) {
            Box(modifier = Modifier.weight(1f)) {
                CameraPreview(
                    cameraProvider = cameraProvider!!,
                    lifecycleOwner = lifecycleOwner
                ) { capture -> imageCapture = capture }

                if (uiState.isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("영수증을 분석중입니다...", color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }

            // 하단 촬영 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FloatingActionButton(
                    onClick = {
                        if (!uiState.isProcessing) {
                            captureImage(
                                imageCapture = imageCapture,
                                context = context,
                                cameraExecutor = cameraExecutor
                            ) { imageUri ->
                                uiState = uiState.copy(isProcessing = true)
                                processImageWithOcr(
                                    context = context,
                                    imageUri = imageUri,
                                    onParsed = { ocrText, parsed ->
                                        uiState = uiState.copy(isProcessing = false)
                                        // 테스트 페이지로
                                        onNavigateToTest?.invoke(imageUri, ocrText, parsed)
                                            ?: run {
                                                // 호환: OcrResult로 콜백
                                                val totalNum = parsed.total
                                                    ?.replace("[^0-9]".toRegex(), "")
                                                    ?.toLongOrNull() ?: 0L
                                                onOcrResult(
                                                    OcrResult(
                                                        amount = totalNum,
                                                        storeName = parsed.merchant ?: "상호명 불명",
                                                        date = parsed.date ?: "",
                                                        description = (parsed.merchant ?: "영수증") + " 지출"
                                                    )
                                                )
                                                onNavigateBack()
                                            }
                                    },
                                    onError = { err ->
                                        Log.e(TAG, "OCR 처리 실패: $err")
                                        uiState = uiState.copy(isProcessing = false)
                                    }
                                )
                            }
                        }
                    },
                    containerColor = if (uiState.isProcessing) Color.Gray else Color.White,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "촬영", modifier = Modifier.size(32.dp))
                }
            }
        } else {
            // 권한 없거나 준비중
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (!uiState.hasPermission) "카메라 권한 필요" else "카메라 준비중...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    AndroidView(
        factory = { context ->
            val previewView = PreviewView(context)
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
                onImageCaptureReady(imageCapture)
            } catch (e: Exception) {
                Log.e(TAG, "카메라 바인딩 실패", e)
            }
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

/** 이미지 촬영 */
private fun captureImage(
    imageCapture: ImageCapture?,
    context: Context,
    cameraExecutor: ExecutorService,
    onImageSaved: (Uri) -> Unit
) {
    val cap = imageCapture ?: return
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.KOREA)
        .format(System.currentTimeMillis())

    val contentValues = android.content.ContentValues().apply {
        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SolSol")
    }

    val output = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    cap.takePicture(
        output,
        cameraExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "이미지 촬영 실패: ${exception.message}", exception)
            }
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let {
                    Log.d(TAG, "이미지 촬영 성공: $it")
                    onImageSaved(it)
                }
            }
        }
    )
}

/** ML Kit 한국어 OCR + ReceiptParser */
private fun processImageWithOcr(
    context: Context,
    imageUri: Uri,
    onParsed: (ocrText: String, parsed: ReceiptFields) -> Unit,
    onError: (String) -> Unit
) {
    try {
        context.contentResolver.openInputStream(imageUri).use { input ->
            val bitmap = BitmapFactory.decodeStream(input)
            val rotated = rotateImageIfRequired(context, bitmap, imageUri)
            val image = InputImage.fromBitmap(rotated, 0)

            val recognizer = TextRecognition.getClient(
                KoreanTextRecognizerOptions.Builder().build()
            )
            recognizer.process(image)
                .addOnSuccessListener { vt ->
                    val text = vt.text ?: ""
                    val parsed = ReceiptParser.parse(text)
                    Log.d(TAG, "OCR OK\n$text\nParsed: $parsed")
                    onParsed(text, parsed)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "OCR 실패", e)
                    onError(e.message ?: "OCR 처리 중 오류가 발생했습니다")
                }
        }
    } catch (e: Exception) {
        Log.e(TAG, "이미지 처리 실패", e)
        onError(e.message ?: "이미지 처리 중 오류가 발생했습니다")
    }
}

/** EXIF 회전 보정 */
private fun rotateImageIfRequired(context: Context, bitmap: Bitmap, imageUri: Uri): Bitmap {
    return try {
        val input = context.contentResolver.openInputStream(imageUri) ?: return bitmap
        val exif = ExifInterface(input)
        input.close()
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    } catch (e: Exception) {
        Log.w(TAG, "회전 보정 실패", e)
        bitmap
    }
}
private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val m = Matrix(); m.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
}

/** 기존 UI 상태 */
data class OcrCameraUiState(
    val hasPermission: Boolean = false,
    val showPermissionDialog: Boolean = false,
    val isProcessing: Boolean = false
)