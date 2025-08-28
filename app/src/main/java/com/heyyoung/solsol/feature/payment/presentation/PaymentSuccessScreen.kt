package com.heyyoung.solsol.feature.payment.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.feature.payment.domain.CouponResult
import com.heyyoung.solsol.feature.payment.domain.PaymentResult

@Composable
fun PaymentSuccessScreen(
    paymentResult: PaymentResult? = null,
    onComplete: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(80.dp))

        // 체크 아이콘
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF8B5FBF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
        }

        Spacer(Modifier.height(28.dp))

        Text("결제 완료되었습니다.", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1E), textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        
        // 결제 금액 정보
        val finalAmount = paymentResult?.finalAmount ?: 0
        Text("${String.format("%,d", finalAmount)}원", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5FBF))
        
        // 할인 내역 표시
        paymentResult?.let { result ->
            if (result.discountAmount > 0 || result.couponDiscount > 0) {
                Spacer(Modifier.height(16.dp))
                
                // 할인 내역 박스
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .background(
                            Color(0xFFF8F7FF),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "💰 할인 내역",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7D6BB0)
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("원래 금액", fontSize = 12.sp, color = Color(0xFF666666))
                        Text("${String.format("%,d", result.originalAmount)}원", fontSize = 12.sp, color = Color(0xFF666666))
                    }
                    
                    if (result.discountAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("제휴 할인", fontSize = 12.sp, color = Color(0xFF666666))
                            Text("-${String.format("%,d", result.discountAmount)}원", fontSize = 12.sp, color = Color(0xFF7D6BB0))
                        }
                    }
                    
                    if (result.couponDiscount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("쿠폰 할인", fontSize = 12.sp, color = Color(0xFF666666))
                            Text("-${String.format("%,d", result.couponDiscount)}원", fontSize = 12.sp, color = Color(0xFF7D6BB0))
                        }
                        
                        result.usedCoupon?.let { coupon ->
                            Text(
                                "✨ ${coupon.amount}원 쿠폰을 사용했어요",
                                fontSize = 11.sp,
                                color = Color(0xFF7D6BB0),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("총 할인", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1E))
                        Text(
                            "-${String.format("%,d", result.discountAmount + result.couponDiscount)}원",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7D6BB0)
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // 쿠폰 당첨 결과에 따른 조건부 UI
        val couponResult = paymentResult?.couponResult
        if (couponResult?.winning == true) {
            // 당첨된 경우
            Text("🎉 럭키 ! ${String.format("%,d", couponResult.amount)}원 할인 쿠폰 당첨", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1C1C1E))
            Text("쿠폰함을 확인해보세요.", fontSize = 14.sp, color = Color(0xFF666666))
        } else {
            // 미당첨된 경우
            Text("😅 아쉽게도 할인 쿠폰을 못 받았습니다", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1C1C1E))
            Text("다음에 다시 도전해보세요!", fontSize = 14.sp, color = Color(0xFF666666))
        }


        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .shadow(4.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
                .width(180.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5FBF)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("완료", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }

        Spacer(Modifier.height(32.dp))
    }
}
