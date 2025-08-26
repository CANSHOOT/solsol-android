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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "OcrCameraScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrCameraScreen(
    onNavigateBack: () -> Unit = {},
    onOcrResult: (OcrResult) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var uiState by remember { mutableStateOf(OcrCameraUiState()) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // 카메라 실행기
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // 권한 요청
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

    // 권한 확인 및 카메라 초기화
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

    // CameraProvider 초기화
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
        PermissionDialog(
            onDismiss = { onNavigateBack() },
            onConfirm = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
                uiState = uiState.copy(showPermissionDialog = false)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 상단 앱바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    Log.d(TAG, "뒤로가기 클릭")
                    onNavigateBack()
                }
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "뒤로",
                    tint = Color.White
                )
            }

            Text(
                text = "영수증 촬영",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.size(48.dp)) // 균형을 위한 공간
        }

        if (uiState.hasPermission && cameraProvider != null) {
            // 카메라 프리뷰
            Box(
                modifier = Modifier.weight(1f)
            ) {
                CameraPreview(
                    cameraProvider = cameraProvider!!,
                    lifecycleOwner = lifecycleOwner,
                    onImageCaptureReady = { capture ->
                        imageCapture = capture
                    }
                )

                // OCR 처리 중 오버레이
                if (uiState.isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
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
                                text = "영수증을 분석중입니다...",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // 가이드 프레임
                CameraGuideFrame()
            }

            // 하단 촬영 컨트롤
            CameraControls(
                isProcessing = uiState.isProcessing,
                onCaptureClick = {
                    Log.d(TAG, "촬영 버튼 클릭")
                    captureImage(
                        imageCapture = imageCapture,
                        context = context,
                        cameraExecutor = cameraExecutor
                    ) { imageUri ->
                        uiState = uiState.copy(isProcessing = true)
                        processImageWithOcr(
                            context = context,
                            imageUri = imageUri,
                            onResult = { result ->
                                uiState = uiState.copy(isProcessing = false)
                                onOcrResult(result)
                                onNavigateBack()
                            },
                            onError = { error ->
                                Log.e(TAG, "OCR 처리 실패: $error")
                                uiState = uiState.copy(isProcessing = false)
                            }
                        )
                    }
                }
            )
        } else {
            // 권한이 없거나 카메라 초기화 중
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
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

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                onImageCaptureReady(imageCapture)
                Log.d(TAG, "카메라 바인딩 완료")
            } catch (e: Exception) {
                Log.e(TAG, "카메라 바인딩 실패", e)
            }

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun CameraGuideFrame() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 200.dp)
                .background(
                    Color.Transparent,
                    RoundedCornerShape(16.dp)
                )
        ) {
            // 모서리 가이드 라인들
            listOf(
                Alignment.TopStart,
                Alignment.TopEnd,
                Alignment.BottomStart,
                Alignment.BottomEnd
            ).forEach { alignment ->
                Box(
                    modifier = Modifier
                        .align(alignment)
                        .size(20.dp)
                        .background(
                            Color.White,
                            when (alignment) {
                                Alignment.TopStart -> RoundedCornerShape(topStart = 16.dp)
                                Alignment.TopEnd -> RoundedCornerShape(topEnd = 16.dp)
                                Alignment.BottomStart -> RoundedCornerShape(bottomStart = 16.dp)
                                Alignment.BottomEnd -> RoundedCornerShape(bottomEnd = 16.dp)
                                else -> RoundedCornerShape(0.dp)
                            }
                        )
                )
            }
        }

        Text(
            text = "영수증을 프레임 안에 맞춰주세요",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-120).dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CameraControls(
    isProcessing: Boolean,
    onCaptureClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        FloatingActionButton(
            onClick = {
                if (!isProcessing) {
                    onCaptureClick()
                }
            },
            modifier = Modifier.size(72.dp),
            containerColor = if (isProcessing) Color.Gray else Color.White,
            contentColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "촬영",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

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
            Text("영수증을 촬영하려면 카메라 권한이 필요합니다.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("허용")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

// 이미지 촬영 함수
private fun captureImage(
    imageCapture: ImageCapture?,
    context: Context,
    cameraExecutor: ExecutorService,
    onImageSaved: (Uri) -> Unit
) {
    val imageCapture = imageCapture ?: return

    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.KOREA)
        .format(System.currentTimeMillis())

    val contentValues = android.content.ContentValues().apply {
        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SolSol")
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions,
        cameraExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "이미지 촬영 실패: ${exception.message}", exception)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = output.savedUri ?: return
                Log.d(TAG, "이미지 촬영 성공: $savedUri")
                onImageSaved(savedUri)
            }
        }
    )
}

// ML Kit OCR 처리 함수
private fun processImageWithOcr(
    context: Context,
    imageUri: Uri,
    onResult: (OcrResult) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // 이미지 회전 보정
        val rotatedBitmap = rotateImageIfRequired(context, bitmap, imageUri)

        val image = InputImage.fromBitmap(rotatedBitmap, 0)

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                Log.d(TAG, "OCR 처리 성공")
                Log.d(TAG, "인식된 전체 텍스트: ${visionText.text}")

                val ocrResult = parseReceiptText(visionText.text)
                Log.d(TAG, "파싱 결과: $ocrResult")

                onResult(ocrResult)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "OCR 처리 실패", e)
                onError(e.message ?: "OCR 처리 중 오류가 발생했습니다")
            }

    } catch (e: Exception) {
        Log.e(TAG, "이미지 처리 실패", e)
        onError(e.message ?: "이미지 처리 중 오류가 발생했습니다")
    }
}

// 이미지 회전 보정
private fun rotateImageIfRequired(context: Context, bitmap: Bitmap, imageUri: Uri): Bitmap {
    try {
        val input = context.contentResolver.openInputStream(imageUri) ?: return bitmap
        val exif = ExifInterface(input)
        input.close()

        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    } catch (e: Exception) {
        Log.w(TAG, "이미지 회전 보정 실패", e)
        return bitmap
    }
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

// 영수증 텍스트 파싱
private fun parseReceiptText(text: String): OcrResult {
    val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

    var amount = 0L
    var storeName = ""
    var date = ""
    var description = ""

    // 금액 찾기 (가장 큰 숫자를 총액으로 가정)
    val amountRegex = """(\d{1,3}(,\d{3})*|\d+)원?""".toRegex()
    val amounts = lines.flatMap { line ->
        amountRegex.findAll(line).map { match ->
            match.groupValues[1].replace(",", "").toLongOrNull() ?: 0L
        }
    }.filter { it > 0 }

    amount = amounts.maxOrNull() ?: 0L

    // 상호명 찾기 (첫 번째 줄 또는 특정 패턴)
    storeName = when {
        lines.isNotEmpty() -> lines[0]
        else -> "상호명 불명"
    }

    // 날짜 찾기 (다양한 날짜 형식)
    val dateRegex = """(\d{4})[.\-/](\d{1,2})[.\-/](\d{1,2})|\d{2}/\d{2}/\d{2}|\d{4}\d{2}\d{2}""".toRegex()
    for (line in lines) {
        val dateMatch = dateRegex.find(line)
        if (dateMatch != null) {
            date = formatDate(dateMatch.value)
            break
        }
    }

    if (date.isEmpty()) {
        // 날짜를 찾지 못하면 오늘 날짜 사용
        val today = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())
        date = today
    }

    // 설명은 상호명 사용 또는 "영수증 지출"로 기본값
    description = if (storeName.isNotEmpty() && storeName != "상호명 불명") {
        "${storeName} 지출"
    } else {
        "영수증 지출"
    }

    Log.d(TAG, "파싱된 데이터 - 금액: $amount, 상호: $storeName, 날짜: $date")

    return OcrResult(
        amount = amount,
        storeName = storeName,
        date = date,
        description = description
    )
}

private fun formatDate(dateStr: String): String {
    return try {
        when {
            dateStr.matches("""\d{4}[.\-/]\d{1,2}[.\-/]\d{1,2}""".toRegex()) -> {
                dateStr.replace("-", ".").replace("/", ".")
            }
            dateStr.matches("""\d{2}/\d{2}/\d{2}""".toRegex()) -> {
                val parts = dateStr.split("/")
                "20${parts[0]}.${parts[1].padStart(2, '0')}.${parts[2].padStart(2, '0')}"
            }
            dateStr.matches("""\d{8}""".toRegex()) -> {
                "${dateStr.substring(0,4)}.${dateStr.substring(4,6)}.${dateStr.substring(6,8)}"
            }
            else -> dateStr
        }
    } catch (e: Exception) {
        Log.w(TAG, "날짜 형식 변환 실패: $dateStr", e)
        dateStr
    }
}

data class OcrCameraUiState(
    val hasPermission: Boolean = false,
    val showPermissionDialog: Boolean = false,
    val isProcessing: Boolean = false
)