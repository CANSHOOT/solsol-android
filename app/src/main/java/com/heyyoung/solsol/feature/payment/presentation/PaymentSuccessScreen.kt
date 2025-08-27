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

@Composable
fun PaymentSuccessScreen(
    finalPrice: Int = 3825,
    couponResult: CouponResult? = null,
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
        Text("${String.format("%,d", finalPrice)}원", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5FBF))
        Spacer(Modifier.height(12.dp))
        
        // 쿠폰 당첨 결과에 따른 조건부 UI
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
