package com.heyyoung.solsol.feature.payment.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.R
import com.heyyoung.solsol.feature.payment.domain.PaymentResult

@Composable
fun PaymentSuccessScreen(
    paymentResult: PaymentResult? = null,
    onComplete: () -> Unit = {}
) {
    val purple = Color(0xFF8B5FBF)
    val textMain = Color(0xFF2D3748)
    val textSub = Color(0xFF718096)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp), // 좌우 여백 고정
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(72.dp))

        // 체크 아이콘
        Box(
            modifier = Modifier
                .size(96.dp)
                .shadow(
                    elevation = 14.dp,
                    spotColor = purple.copy(alpha = 0.18f),
                    ambientColor = purple.copy(alpha = 0.12f),
                    shape = CircleShape
                )
                .background(purple, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // 헤드라인
        Text(
            text = "결제 완료되었습니다",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = textMain,
            textAlign = TextAlign.Center
        )

        // 결제 금액
        val finalAmount = paymentResult?.finalAmount ?: 0
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${String.format("%,d", finalAmount)}원",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = purple,
            textAlign = TextAlign.Center
        )

        // ───────── 할인 내역 ─────────
        paymentResult?.let { result ->
            if (result.discountAmount > 0 || result.couponDiscount > 0) {
                Spacer(Modifier.height(24.dp))

                SectionCard {
                    // 제목(아이콘은 coupon.png 사용)
                    SectionHeaderWithImage(
                        title = "할인 내역",
                        imageRes = R.drawable.coupon, // ← 동그라미 대신 이미지
                        titleColor = purple
                    )

                    Spacer(Modifier.height(12.dp))

                    LabeledAmountRow(
                        label = "원래 금액",
                        value = "${String.format("%,d", result.originalAmount)}원",
                        labelColor = textSub,
                        valueColor = textMain
                    )

                    if (result.discountAmount > 0) {
                        Spacer(Modifier.height(8.dp))
                        LabeledAmountRow(
                            label = "제휴 할인",
                            value = "-${String.format("%,d", result.discountAmount)}원",
                            labelColor = textSub,
                            valueColor = purple
                        )
                    }

                    if (result.couponDiscount > 0) {
                        Spacer(Modifier.height(8.dp))
                        LabeledAmountRow(
                            label = "쿠폰 할인",
                            value = "-${String.format("%,d", result.couponDiscount)}원",
                            labelColor = textSub,
                            valueColor = purple
                        )

                        result.usedCoupon?.let { coupon ->
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${String.format("%,d", coupon.amount)}원 쿠폰을 사용했어요",
                                fontSize = 11.sp,
                                color = textSub,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    Spacer(Modifier.height(12.dp))

                    LabeledAmountRow(
                        label = "총 할인",
                        value = "-${String.format("%,d", result.discountAmount + result.couponDiscount)}원",
                        labelColor = textMain,
                        valueColor = purple,
                        labelWeight = FontWeight.Bold,
                        valueWeight = FontWeight.ExtraBold
                    )
                }
            }

            // ───────── 쿠폰 결과 ─────────
            Spacer(Modifier.height(24.dp))

            val couponResult = paymentResult.couponResult
            if (couponResult?.winning == true) {
                SectionBanner(
                    bg = purple.copy(alpha = 0.10f),
                    title = "🤩 럭키! ${String.format("%,d", couponResult.amount)}원 할인 쿠폰 당첨",
                    subtitle = "쿠폰함을 확인해보세요",
                    titleColor = purple,
                    subtitleColor = textSub
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(320.dp)
                ) {
                    Text(
                        text = "😅 아쉽게도 할인 쿠폰을 못 받았습니다",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = textMain,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "다음에 다시 도전해보세요!",
                        fontSize = 13.sp,
                        color = textSub,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(40.dp))

        // 완료 버튼
        Button(
            onClick = onComplete,
            modifier = Modifier
                .width(220.dp)
                .height(54.dp)
                .shadow(
                    elevation = 14.dp,
                    spotColor = purple.copy(alpha = 0.25f),
                    ambientColor = purple.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(28.dp)
                ),
            colors = ButtonDefaults.buttonColors(containerColor = purple),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "완료",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

/* -------------------------- 작은 UI 헬퍼들 -------------------------- */

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .width(342.dp) // 본문 폭 고정으로 시선 흐름 안정화
            .shadow(
                elevation = 10.dp,
                spotColor = Color(0x1A8B5FBF),
                ambientColor = Color(0x1A8B5FBF),
                shape = RoundedCornerShape(16.dp)
            )
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun SectionHeaderWithImage(
    title: String,
    imageRes: Int,
    titleColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // coupon.png 아이콘
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .background(Color.Transparent, RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor
        )
    }
}

@Composable
private fun LabeledAmountRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    labelWeight: FontWeight = FontWeight.Medium,
    valueWeight: FontWeight = FontWeight.Medium
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = labelColor,
            fontWeight = labelWeight
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = valueColor,
            fontWeight = valueWeight,
            textAlign = TextAlign.Right
        )
    }
}

@Composable
private fun SectionBanner(
    bg: Color,
    title: String,
    subtitle: String,
    titleColor: Color,
    subtitleColor: Color
) {
    Column(
        modifier = Modifier
            .width(342.dp)
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = subtitleColor,
            textAlign = TextAlign.Center
        )
    }
}
