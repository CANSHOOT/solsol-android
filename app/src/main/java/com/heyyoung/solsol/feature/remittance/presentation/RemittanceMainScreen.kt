package com.heyyoung.solsol.feature.remittance.presentation

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemittanceMainScreen(
    groupId: Long?,
    receiverName: String = "김신한",
    receiverInfo: String = " ",
    amount: String = "29,002",
    cardNumber: String = "****1234",
    onNavigateBack: () -> Unit = {},
    onRemittanceComplete: () -> Unit = {}
) {
    val TAG = "RemittanceMainScreen"
    val context = LocalContext.current
    var showSuccessScreen by remember { mutableStateOf(false) }
    val viewModel: RemittanceViewModel = hiltViewModel()
    val paymentResponse by viewModel.paymentResponse.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    // 지문 인식 함수
    fun authenticateWithBiometric(onSuccess: () -> Unit, onError: (String) -> Unit) {
        Log.d(TAG, "지문 인식 시도 시작")
        val biometricManager = BiometricManager.from(context)
        
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "지문 센서 사용 가능")
                try {
                    val activity = context as FragmentActivity
                    val executor = ContextCompat.getMainExecutor(context)
                    val biometricPrompt = BiometricPrompt(activity, executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                super.onAuthenticationError(errorCode, errString)
                                Log.e(TAG, "지문 인증 에러: $errorCode, $errString")
                                onError(errString.toString())
                            }

                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                Log.d(TAG, "지문 인증 성공!")
                                onSuccess()
                            }

                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                                Log.d(TAG, "지문 인증 실패")
                                onError("지문 인식에 실패했습니다")
                            }
                        })

                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("지문으로 송금을 인증해주세요")
                        .setSubtitle("안전한 송금을 위해 지문 인증이 필요합니다")
                        .setNegativeButtonText("취소")
                        .build()

                    Log.d(TAG, "지문 인증 다이얼로그 실행")
                    biometricPrompt.authenticate(promptInfo)
                } catch (e: Exception) {
                    Log.e(TAG, "지문 인증 실행 중 오류", e)
                    onError("지문 인식을 초기화할 수 없습니다: ${e.message}")
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e(TAG, "지문 센서 하드웨어 없음")
                onError("지문 센서가 없습니다")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e(TAG, "지문 센서 사용 불가")
                onError("지문 센서를 사용할 수 없습니다")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.e(TAG, "등록된 지문 없음")
                onError("등록된 지문이 없습니다.\n설정에서 지문을 등록해주세요.")
            }
            else -> {
                Log.e(TAG, "기타 지문 인식 오류")
                onError("지문 인식을 사용할 수 없습니다")
            }
        }
    }

    // 하드웨어/제스처 뒤로가기 버튼 처리 (송금 처리 중일 때는 비활성화)
    BackHandler(enabled = !loading) {
        onNavigateBack()
    }

    if (showSuccessScreen) {
        RemittanceSuccessScreen(
            receiverName = receiverName,
            amount = amount,
            onComplete = {
                showSuccessScreen = false
                onRemittanceComplete()
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("송금하기") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1C1C1E),
                navigationIconContentColor = Color(0xFF1C1C1E)
            )
        )

        // 본문
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // 수신자 정보 박스
            ReceiverInfoBox(
                receiverName = receiverName,
                receiverInfo = receiverInfo
            )

            Spacer(Modifier.height(20.dp))

            // 송금 금액 박스
            AmountBox(amount = amount)

            Spacer(Modifier.height(20.dp))

            // 카드 정보 박스 (회전 가능)
            CardPreviewBox(cardNumber = cardNumber)

            Spacer(Modifier.height(32.dp))

            // 수수료 안내
            Text(
                text = "해외 학록 및 송금 수수료 무료",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // 송금하기 버튼
            Button(
                onClick = {
                    authenticateWithBiometric(
                        onSuccess = {
                            groupId?.let {
                                viewModel.sendPayment(it, "정산 송금")
                            }
                        },
                        onError = { errorMessage ->
                            Log.e(TAG, "지문 인증 실패: $errorMessage")
                            // TODO: 에러 메시지를 UI에 표시할 수 있습니다
                        }
                    )
                },
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        spotColor = Color(0x40000000),
                        ambientColor = Color(0x40000000)
                    )
                    .width(342.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5FBF),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = "송금하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            // ✅ 응답 처리
            when {
                loading -> Text("송금 처리중...", color = Color.Gray)
                error != null -> Text("에러: $error", color = Color.Red)
                paymentResponse != null -> {
                    // ✅ 성공 응답 소비
                    LaunchedEffect(paymentResponse) {
                        viewModel.clearPaymentResponse()
                        onRemittanceComplete()
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ReceiverInfoBox(
    receiverName: String,
    receiverInfo: String
) {
    Row(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = 2.dp,
                color = Color(0xB2E2E8F0),
                shape = RoundedCornerShape(size = 12.dp)
            )
            .width(342.dp)
            .height(100.dp)
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 아이콘
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color(0xFFE5E5E5),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF666666)
            )
        }

        Spacer(Modifier.width(12.dp))

        // 수신자 정보
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = receiverName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = receiverInfo,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun AmountBox(amount: String) {
    Column(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = 1.dp,
                color = Color(0xCC8B5FBF),
                shape = RoundedCornerShape(size = 16.dp)
            )
            .padding(1.dp)
            .width(342.dp)
            .height(120.dp)
            .background(
                color = Color(0xFFF8F7FF),
                shape = RoundedCornerShape(size = 16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "송금 금액",
            fontSize = 14.sp,
            color = Color(0xFF7D6BB0),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${amount}원",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1C1C1E)
        )
    }
}

@Composable
private fun CardPreviewBox(cardNumber: String) {
    var isFlipped by remember { mutableStateOf(false) }
    
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "card_flip"
    )
    
    Box(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x0D000000),
                ambientColor = Color(0x0D000000)
            )
            .border(
                width = 1.dp,
                color = Color(0x33718096),
                shape = RoundedCornerShape(12.dp)
            )
            .width(342.dp)
            .height(245.dp)
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { isFlipped = !isFlipped },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                },
            contentAlignment = Alignment.Center
        ) {
            // rotation이 90도를 넘으면 뒤집힌 상태로 판단
            val showBack = rotation > 90f
            
            if (showBack) {
                // 뒤면 이미지 (180도 더 회전시켜서 올바른 방향으로 표시)
                Image(
                    painter = painterResource(id = com.heyyoung.solsol.R.drawable.cd_credit_poddr1b),
                    contentDescription = "카드 뒤면",
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.85f)
                        .clip(RoundedCornerShape(12.dp))
                        .graphicsLayer { rotationY = 180f },
                    contentScale = ContentScale.Crop
                )
                
                // 뒤면 텍스트
                Text(
                    text = "카드 뒤면",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .graphicsLayer { rotationY = 180f },
                    fontSize = 12.sp,
                    color = Color.White
                )
            } else {
                // 앞면 이미지
                Image(
                    painter = painterResource(id = com.heyyoung.solsol.R.drawable.cd_credit_poddr1),
                    contentDescription = "신한 체크카드",
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.85f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // 앞면 텍스트
                Text(
                    text = "신한 체크카드 ($cardNumber)",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}