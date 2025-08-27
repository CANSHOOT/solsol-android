package com.heyyoung.solsol.feature.studentcouncil.presentation

import android.Manifest
import android.content.Context
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// ReceiptParser 연동
import com.heyyoung.solsol.feature.studentcouncil.presentation.ReceiptParser
import com.heyyoung.solsol.feature.studentcouncil.presentation.ReceiptFields

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
                if (uiState.isProcessing) ProcessingOverlay()
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

            // OCR 결과 표시 (필드 길게 눌러 수정)
            ocrResult?.let { result ->
                OcrResultCardEditable(
                    result = result,
                    onChange = { updated -> ocrResult = updated },
                    onRetake = { ocrResult = null },
                    onConfirm = {
                        ocrResult?.let(onOcrResult)
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun OcrResultCardEditable(
    result: OcrResult,
    onChange: (OcrResult) -> Unit,
    onRetake: () -> Unit,
    onConfirm: () -> Unit
) {
    // 편집 다이얼로그 상태
    var editTarget by remember { mutableStateOf<EditTarget?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

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
                text = "영수증 인식 완료 (필드를 길게 눌러 수정)",
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
                // 상호명 (Long-press)
                ResultRow(
                    label = "상호명",
                    value = result.storeName,
                    onLongPress = { editTarget = EditTarget.StoreName(result.storeName) }
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 금액 (Long-press)
                ResultRow(
                    label = "금액",
                    value = "${String.format("%,d", result.amount)}원",
                    onLongPress = { editTarget = EditTarget.Amount(result.amount) }
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 날짜 (Long-press → DatePicker)
                ResultRow(
                    label = "날짜",
                    value = result.date,
                    onLongPress = { showDatePicker = true }
                )
                Spacer(modifier = Modifier.height(12.dp))
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

    // ───── 텍스트/숫자 편집 다이얼로그 ─────
    when (val t = editTarget) {
        is EditTarget.StoreName -> {
            EditTextDialog(
                title = "상호명 수정",
                initial = t.current,
                keyboardType = KeyboardType.Text,
                onDismiss = { editTarget = null },
                onConfirm = { new ->
                    onChange(result.copy(storeName = new.ifBlank { result.storeName }))
                    editTarget = null
                }
            )
        }
        is EditTarget.Amount -> {
            EditTextDialog(
                title = "금액 수정",
                initial = result.amount.toString(),
                keyboardType = KeyboardType.Number,
                onDismiss = { editTarget = null },
                onConfirm = { new ->
                    val parsed = new.filter { it.isDigit() }.toLongOrNull()
                    if (parsed != null) onChange(result.copy(amount = parsed))
                    editTarget = null
                }
            )
        }
        null -> Unit
    }

    // ───── 날짜 선택 다이얼로그 (Material3 DatePicker) ─────
    if (showDatePicker) {
        val initialMillis = remember(result.date) { parseDateToMillis(result.date) ?: System.currentTimeMillis() }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@TextButton
                    val local = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    onChange(result.copy(date = local.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ResultRow(
    label: String,
    value: String,
    onLongPress: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress
            )
            .padding(vertical = 2.dp)
    ) {
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
        Text(
            text = "길게 눌러 수정",
            fontSize = 11.sp,
            color = Color(0xFF9AA0A6)
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

// OCR 처리 + ReceiptParser 적용
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
                // ★ ReceiptParser 로 파싱
                val fields = ReceiptParser.parse(visionText.text)
                val result = mapReceiptFieldsToOcrResult(fields, fallbackText = visionText.text)
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

// ReceiptParser → OcrResult 매핑
private fun mapReceiptFieldsToOcrResult(fields: ReceiptFields, fallbackText: String): OcrResult {
    val store = fields.merchant?.takeIf { it.isNotBlank() }
        ?: fallbackText.lineSequence().firstOrNull()?.trim().orEmpty().ifBlank { "상호명 불명" }

    val amount = fields.total
        ?.replace(Regex("[^\\d]"), "")
        ?.toLongOrNull()
        ?: 0L

    val dateIso = fields.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

    return OcrResult(
        amount = amount,
        storeName = store,
        date = dateIso, // ISO-8601(yyyy-MM-dd)로 유지
        description = "${store} 지출"
    )
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

// 편집 타겟 정의
private sealed interface EditTarget {
    data class StoreName(val current: String) : EditTarget
    data class Amount(val current: Long) : EditTarget
}

@Composable
private fun EditTextDialog(
    title: String,
    initial: String,
    keyboardType: KeyboardType,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) { Text("확인") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

// 날짜 문자열 → epoch millis (여러 포맷 허용)
private fun parseDateToMillis(s: String): Long? {
    val candidates = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,                 // yyyy-MM-dd
        DateTimeFormatter.ofPattern("yyyy.MM.dd"),        // yyyy.MM.dd
        DateTimeFormatter.ofPattern("yyyy/M/d"),          // yyyy/M/d
        DateTimeFormatter.ofPattern("yyyy.M.d")           // yyyy.M.d
    )
    for (fmt in candidates) {
        try {
            val local = java.time.LocalDate.parse(s.trim(), fmt)
            return local.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) { }
    }
    return null
}