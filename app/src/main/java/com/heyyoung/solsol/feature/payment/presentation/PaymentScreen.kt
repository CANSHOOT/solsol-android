package com.heyyoung.solsol.feature.payment.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    qrData: String = "",
    onNavigateBack: () -> Unit = {},
    onPaymentComplete: () -> Unit = {}
) {
    var isPaymentCompleted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    val storeName = "아메리카노"
    val originalPrice = 4_500
    val discountAmount = 675 // 15%
    val finalPrice = originalPrice - discountAmount

    if (isPaymentCompleted) {
        PaymentSuccessScreen(
            finalPrice = finalPrice,
            onComplete = onPaymentComplete
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
            title = { Text("QR 결제") },
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
            Spacer(Modifier.height(12.dp))

            // 매장명 + 가격
            Box(Modifier.width(342.dp)) {
                Column {
                    Text(
                        text = storeName,
                        fontSize = 16.sp,
                        color = Color(0xFF5A5A5A)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = String.format("%,d", originalPrice),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1C1C1E)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ▽ 제휴 할인 박스
            DiscountBox(
                title = "컴퓨터공학과 제휴 할인",
                sub = "15% 할인 · 캠퍼스 카페",
                amount = discountAmount
            )

            Spacer(Modifier.height(8.dp))

            Box(Modifier.width(342.dp)) {
                Text(
                    text = "✓ 자동으로 적용되었습니다",
                    fontSize = 12.sp,
                    color = Color(0xFF7D6BB0) // 약한 보라
                )
            }

            Spacer(Modifier.height(16.dp))

            // ▽ 결제 카드 표시 영역 (임시 카드 들어갈 영역)
            CardPreviewBox()

            Spacer(Modifier.height(12.dp))

            Text(
                text = "총 ${String.format("%,d", discountAmount)}원 할인을 받았어요",
                fontSize = 12.sp,
                color = Color(0xFF7D6BB0)
            )

            Spacer(Modifier.height(16.dp))

            // ▽ 결제 버튼 (그림자 + 보라)
            PaymentCta(
                finalPrice = finalPrice,
                enabled = !isLoading,
                onClick = {
                    isLoading = true
                    scope.launch {
                        delay(1200)
                        isLoading = false
                        isPaymentCompleted = true
                    }
                }
            )

            if (isLoading) {
                Spacer(Modifier.height(10.dp))
                Text("결제 중…", fontSize = 12.sp, color = Color(0xFF7D6BB0))
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}


@Composable
private fun DiscountBox(
    title: String,
    sub: String,
    amount: Int
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
                color = Color(0x338B5FBF),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(2.dp)
            .width(342.dp)
            .height(120.dp)
            .background(
                color = Color(0xFFF8F7FF),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7D6BB0))
            Spacer(Modifier.height(6.dp))
            Text(sub, fontSize = 13.sp, color = Color(0xFF7D6BB0))
        }
        Text(
            text = "-${String.format("%,d", amount)}원",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF7D6BB0)
        )
    }
}


@Composable
private fun CardPreviewBox() {
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
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 아래 라벨(임시)
        Text(
            text = "신한 체크카드 (4426-60**)",
            modifier = Modifier.padding(bottom = 12.dp),
            fontSize = 12.sp,
            color = Color(0xFF7F8A96)
        )
    }
}


@Composable
private fun PaymentCta(
    finalPrice: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x40000000),
                ambientColor = Color(0x40000000)
            )
            .width(342.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xE58B5FBF),
            disabledContainerColor = Color(0xE58B5FBF).copy(alpha = 0.6f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = "${String.format("%,d", finalPrice)}원 결제하기",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
