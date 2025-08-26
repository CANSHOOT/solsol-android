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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
    onOcrResult: (OcrResult) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var uiState by remember { mutableStateOf(OcrCameraUiState()) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var ocrResult by remember { mutableStateOf<OcrResult?>(null) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // 권한 요청 처리
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        uiState = uiState.copy(
            hasPermission = isGranted,
            showPermissionDialog = !isGranted
        )
        Log.d(TAG, "카메라 권한: ${if (isGranted) "승인" else "거부"}")
    }

    // 초기 권한 확인
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            uiState = uiState.copy(hasPermission = true)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // 카메라 초기화
    LaunchedEffect(uiState.hasPermission) {
        if (uiState.hasPermission) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                cameraProvider = cameraProviderFuture.get()
            }, ContextCompat.getMainExecutor(context))
        }
    }

    // 리소스 정리
    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraProvider?.unbindAll()
                cameraExecutor.shutdown()
            } catch (_: Exception) { }
        }
    }

    // 권한 다이얼로그
    if (uiState.showPermissionDialog) {
        PermissionDialog(
            onDismiss = onNavigateBack,
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
        // 상단 바
        TopBar(onNavigateBack = {
            cameraProvider?.unbindAll()
            onNavigateBack()
        })

        if (uiState.hasPermission && cameraProvider != null) {
            // 메인 카메라 영역
            Box(modifier = Modifier.weight(1f)) {
                CameraPreview(
                    cameraProvider = cameraProvider!!,
                    lifecycleOwner = lifecycleOwner,
                    onImageCaptureReady = { imageCapture = it }
                )

                // 처리 중 오버레이
                if (uiState.isProcessing) {
                    ProcessingOverlay()
                }

                // 가이드 프레임
                CameraGuideFrame()
            }

            // 하단 컨트롤
            if (ocrResult == null) {
                CameraControls(
                    isProcessing = uiState.isProcessing,
                    onCaptureClick = {
                        captureAndProcessImage(
                            imageCapture = imageCapture,
                            context = context,
                            cameraExecutor = cameraExecutor,
                            onProcessingStart = {
                                uiState = uiState.copy(isProcessing = true)
                            },
                            onResult = { result ->
                                uiState = uiState.copy(isProcessing = false)
                                ocrResult = result
                            },
                            onError = {
                                uiState = uiState.copy(isProcessing = false)
                            }
                        )
                    }
                )
            }

            // OCR 결과 표시
            ocrResult?.let { result ->
                OcrResultCard(
                    result = result,
                    onRetake = { ocrResult = null },
                    onConfirm = {
                        onOcrResult(result)
                        onNavigateBack()
                    }
                )
            }
        } else {
            // 로딩/권한 없음 상태
            LoadingState(hasPermission = uiState.hasPermission)
        }
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "뒤로",
                tint = Color.White
            )
        }

        Text(
            text = "영수증 스캔",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.size(48.dp))
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
            val preview = Preview.Builder().build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
                onImageCaptureReady(imageCapture)
            } catch (e: Exception) {
                Log.e(TAG, "카메라 바인딩 실패", e)
            }

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun ProcessingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF8B5FBF),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "영수증을 분석하고 있습니다...",
                    fontSize = 16.sp,
                    color = Color(0xFF1C1C1E)
                )
            }
        }
    }
}

@Composable
private fun CameraGuideFrame() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 가이드 프레임
        Box(
            modifier = Modifier
                .size(width = 320.dp, height = 200.dp)
                .background(Color.Transparent)
        ) {
            // 모서리 가이드
            listOf(
                Alignment.TopStart, Alignment.TopEnd,
                Alignment.BottomStart, Alignment.BottomEnd
            ).forEach { alignment ->
                Box(
                    modifier = Modifier
                        .align(alignment)
                        .size(24.dp)
                        .background(
                            Color.White,
                            when (alignment) {
                                Alignment.TopStart -> RoundedCornerShape(topStart = 12.dp)
                                Alignment.TopEnd -> RoundedCornerShape(topEnd = 12.dp)
                                Alignment.BottomStart -> RoundedCornerShape(bottomStart = 12.dp)
                                else -> RoundedCornerShape(bottomEnd = 12.dp)
                            }
                        )
                )
            }
        }

        // 안내 텍스트
        Text(
            text = "영수증을 프레임에 맞춰 주세요",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-140).dp)
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
            onClick = { if (!isProcessing) onCaptureClick() },
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
private fun OcrResultCard(
    result: OcrResult,
    onRetake: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // 결과 헤더
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.AddCircle,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "영수증 인식 완료",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 인식 결과
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ResultRow("상호명", result.storeName)
                Spacer(modifier = Modifier.height(12.dp))
                ResultRow("금액", "${String.format("%,d", result.amount)}원")
                Spacer(modifier = Modifier.height(12.dp))
                ResultRow("날짜", result.date)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF8B5FBF)
                )
            ) {
                Text("다시 촬영")
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5FBF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("등록하기")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1C1C1E)
        )
    }
}

@Composable
private fun LoadingState(hasPermission: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (!hasPermission) "카메라 권한이 필요합니다" else "카메라를 준비하고 있습니다...",
                color = Color.White,
                fontSize = 16.sp
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
        title = { Text("카메라 권한 필요") },
        text = { Text("영수증을 촬영하려면 카메라 권한이 필요합니다.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("허용") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

// 이미지 촬영 및 OCR 처리
private fun captureAndProcessImage(
    imageCapture: ImageCapture?,
    context: Context,
    cameraExecutor: ExecutorService,
    onProcessingStart: () -> Unit,
    onResult: (OcrResult) -> Unit,
    onError: () -> Unit
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

    onProcessingStart()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "촬영 실패: ${exception.message}", exception)
                onError()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let { uri ->
                    processImageWithOcr(context, uri, onResult, onError)
                } ?: onError()
            }
        }
    )
}

// OCR 처리
private fun processImageWithOcr(
    context: Context,
    imageUri: Uri,
    onResult: (OcrResult) -> Unit,
    onError: () -> Unit
) {
    try {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val rotatedBitmap = rotateImageIfRequired(context, bitmap, imageUri)
        val image = InputImage.fromBitmap(rotatedBitmap, 0)

        val recognizer = TextRecognition.getClient(
            KoreanTextRecognizerOptions.Builder().build()
        )

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val result = parseReceiptText(visionText.text)
                Log.d(TAG, "OCR 성공: $result")
                onResult(result)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "OCR 실패", e)
                onError()
            }
    } catch (e: Exception) {
        Log.e(TAG, "이미지 처리 실패", e)
        onError()
    }
}

// 영수증 텍스트 파싱
private fun parseReceiptText(text: String): OcrResult {
    val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

    // 금액 찾기
    val amountRegex = """(\d{1,3}(,\d{3})*|\d+)원?""".toRegex()
    val amounts = lines.flatMap { line ->
        amountRegex.findAll(line).map { match ->
            match.groupValues[1].replace(",", "").toLongOrNull() ?: 0L
        }
    }.filter { it > 0 }
    val amount = amounts.maxOrNull() ?: 0L

    // 상호명 (첫 번째 줄)
    val storeName = lines.firstOrNull() ?: "상호명 불명"

    // 날짜 찾기
    val dateRegex = """(\d{4})[.\-/](\d{1,2})[.\-/](\d{1,2})|\d{2}/\d{2}/\d{2}|\d{4}\d{2}\d{2}""".toRegex()
    var date = ""
    for (line in lines) {
        val dateMatch = dateRegex.find(line)
        if (dateMatch != null) {
            date = formatDate(dateMatch.value)
            break
        }
    }

    if (date.isEmpty()) {
        date = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())
    }

    return OcrResult(
        amount = amount,
        storeName = storeName,
        date = date,
        description = "${storeName} 지출"
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
        return bitmap
    }
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}