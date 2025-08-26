package com.heyyoung.solsol.feature.studentcouncil.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private const val TAG = "ExpenseRegisterScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCouncilExpenseRegisterScreen(
    onNavigateBack: () -> Unit = {},
    onExpenseRegistered: () -> Unit = {}
) {
    // UI 상태 관리
    var uiState by remember { mutableStateOf(ExpenseRegisterUiState()) }

    Log.d(TAG, "지출 등록 화면 진입")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("지출 등록") },
            navigationIcon = {
                IconButton(onClick = {
                    Log.d(TAG, "뒤로가기 클릭")
                    onNavigateBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1C1C1E),
                navigationIconContentColor = Color(0xFF1C1C1E)
            )
        )

        // 메인 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 안내 텍스트
            Text(
                text = "영수증을 촬영해주세요",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "OCR로 자동 인식합니다",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 영수증 촬영 버튼
            ReceiptCameraButton(
                hasImage = uiState.receiptImagePath != null,
                isProcessing = uiState.isOcrProcessing,
                onClick = {
                    Log.d(TAG, "영수증 촬영 버튼 클릭")
                    // OCR 카메라 화면으로 이동
                    // TODO: Navigation으로 OcrCameraScreen 호출
                    // 임시로 데모 데이터 표시
                    uiState = uiState.copy(isOcrProcessing = true)
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(2000)
                        val demoResult = OcrResult(
                            amount = 18000L,
                            storeName = "캠퍼스 카페",
                            date = "2025.03.15",
                            description = "커피 6잔"
                        )
                        uiState = uiState.copy(
                            isOcrProcessing = false,
                            receiptImagePath = "/temp/demo_receipt.jpg",
                            amount = demoResult.amount.toString(),
                            storeName = demoResult.storeName,
                            date = demoResult.date,
                            description = demoResult.description
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // OCR 처리 중 표시
            if (uiState.isOcrProcessing) {
                OcrProcessingIndicator()
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 지출 정보 입력 폼
            ExpenseInputForm(
                uiState = uiState,
                onAmountChange = { amount ->
                    uiState = uiState.copy(amount = amount)
                    Log.d(TAG, "금액 입력: ${amount}원")
                },
                onStoreNameChange = { storeName ->
                    uiState = uiState.copy(storeName = storeName)
                    Log.d(TAG, "상호명 입력: $storeName")
                },
                onDateChange = { date ->
                    uiState = uiState.copy(date = date)
                    Log.d(TAG, "날짜 입력: $date")
                },
                onDescriptionChange = { description ->
                    uiState = uiState.copy(description = description)
                    Log.d(TAG, "내용 입력: $description")
                },

                )

            Spacer(modifier = Modifier.height(32.dp))

            // 지출 등록 버튼
            Button(
                onClick = {
                    Log.d(TAG, "지출 등록 시작")
                    Log.d(TAG, "등록 정보 - 금액: ${uiState.amount}원, 상호: ${uiState.storeName}")

                    uiState = uiState.copy(isRegistering = true)

                    // 지출 등록 API 호출
                    registerExpense(
                        amount = uiState.amount.toLongOrNull() ?: 0L,
                        storeName = uiState.storeName,
                        date = uiState.date,
                        description = uiState.description,
                        receiptImagePath = uiState.receiptImagePath
                    ) { success ->
                        uiState = uiState.copy(isRegistering = false)

                        if (success) {
                            Log.i(TAG, "지출 등록 완료")
                            onExpenseRegistered()
                        } else {
                            Log.e(TAG, "지출 등록 실패")
                            // TODO: 에러 처리
                        }
                    }
                },
                enabled = uiState.canRegister() && !uiState.isRegistering,
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        spotColor = Color(0x26000000),
                        ambientColor = Color(0x26000000)
                    )
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5FBF),
                    disabledContainerColor = Color(0x4D8B5FBF)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isRegistering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "지출 등록하기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ReceiptCameraButton(
    hasImage: Boolean,
    isProcessing: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .size(160.dp)
            .clickable(enabled = !isProcessing) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (hasImage) Color(0xFFF0F9FF) else Color(0xFFF8F9FA)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color(0xFF8B5FBF)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "처리 중...",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "영수증 촬영",
                    tint = if (hasImage) Color(0xFF10B981) else Color(0xFF666666),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (hasImage) "다시 촬영" else "촬영하기",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasImage) Color(0xFF10B981) else Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
private fun OcrProcessingIndicator() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F9FF)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color(0xFF8B5FBF)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "영수증을 분석하고 있습니다...",
                fontSize = 14.sp,
                color = Color(0xFF1C1C1E)
            )
        }
    }
}

@Composable
private fun ExpenseInputForm(
    uiState: ExpenseRegisterUiState,
    onAmountChange: (String) -> Unit,
    onStoreNameChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    Column {
        // 금액 입력
        ExpenseInputField(
            label = "금액",
            value = uiState.amount,
            onValueChange = onAmountChange,
            placeholder = "0",
            suffix = "원",
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 상호명 입력
        ExpenseInputField(
            label = "상호명",
            value = uiState.storeName,
            onValueChange = onStoreNameChange,
            placeholder = "상호명을 입력하세요"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 날짜 입력
        ExpenseInputField(
            label = "날짜",
            value = uiState.date,
            onValueChange = onDateChange,
            placeholder = "YYYY.MM.DD"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 내용 입력
        ExpenseInputField(
            label = "내용",
            value = uiState.description,
            onValueChange = onDescriptionChange,
            placeholder = "지출 내용을 입력하세요"
        )
    }
}

@Composable
private fun ExpenseInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    suffix: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1C1C1E)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = Color(0xFFCCCCCC)
                    )
                },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5FBF),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true
            )

            if (suffix.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = suffix,
                    fontSize = 16.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

// 임시 카메라 촬영 함수 (실제로는 CameraX 구현)
private fun startReceiptCapture(onImageCaptured: (String) -> Unit) {
    Log.d(TAG, "영수증 촬영 시작 (임시)")
    // TODO: 실제 카메라 촬영 구현
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
        kotlinx.coroutines.delay(1000)
        onImageCaptured("/temp/receipt_image.jpg")
    }
}

// 임시 OCR 처리 함수 (실제로는 ML Kit 구현)
private fun processOcr(imagePath: String, onOcrComplete: (OcrResult) -> Unit) {
    Log.d(TAG, "OCR 처리 시작: $imagePath")
    // TODO: 실제 ML Kit OCR 구현
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
        kotlinx.coroutines.delay(2000) // OCR 처리 시뮬레이션

        val ocrResult = OcrResult(
            amount = 18000L,
            storeName = "캠퍼스 카페",
            date = "2025.03.15",
            description = "커피 6잔"
        )

        Log.d(TAG, "OCR 처리 완료: $ocrResult")
        onOcrComplete(ocrResult)
    }
}

// 임시 지출 등록 함수 (실제로는 Repository 구현)
private fun registerExpense(
    amount: Long,
    storeName: String,
    date: String,
    description: String,
    receiptImagePath: String?,
    onComplete: (Boolean) -> Unit
) {
    Log.d(TAG, "지출 등록 API 호출 - 금액: ${amount}원")
    // TODO: 실제 API 호출 구현
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
        kotlinx.coroutines.delay(1500)
        onComplete(true)
    }
}

/**
 * 지출 등록 UI 상태
 */
data class ExpenseRegisterUiState(
    val receiptImagePath: String? = null,
    val isOcrProcessing: Boolean = false,
    val amount: String = "",
    val storeName: String = "",
    val date: String = "",
    val description: String = "",
    val isRegistering: Boolean = false
) {
    fun canRegister(): Boolean {
        return amount.isNotBlank() &&
                storeName.isNotBlank() &&
                date.isNotBlank() &&
                description.isNotBlank() &&
                amount.toLongOrNull() != null &&
                amount.toLongOrNull()!! > 0
    }
}

/**
 * OCR 결과 데이터
 */
data class OcrResult(
    val amount: Long,
    val storeName: String,
    val date: String,
    val description: String
)