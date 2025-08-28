package com.heyyoung.solsol.feature.payment.presentation

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.heyyoung.solsol.feature.payment.domain.PaymentViewModel
import com.heyyoung.solsol.feature.payment.domain.DiscountCoupon
import com.heyyoung.solsol.feature.payment.domain.CouponType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    qrData: String = "",
    onNavigateBack: () -> Unit = {},
    onPaymentComplete: () -> Unit = {},
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val TAG = "PaymentScreen"
    val uiState = viewModel.uiState

    // QR 데이터가 있을 때 결제 정보 로드
    LaunchedEffect(qrData) {
        if (qrData.isNotEmpty()) {
            Log.d(TAG, "QR 데이터로 결제 정보 로드: $qrData")
            // 새로운 결제 시작시 이전 상태 초기화
            viewModel.resetPaymentState()
            viewModel.loadPaymentInfo(qrData)
        }
    }

    // 결제 완료 시 성공 화면으로 이동
    if (uiState.isPaymentComplete) {
        PaymentSuccessScreen(
            paymentResult = uiState.paymentResult,
            onComplete = {
                // 결제 완료 후 상태 초기화
                viewModel.resetPaymentState()
                onPaymentComplete()
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

        // 로딩 상태 표시
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color(0xFF7D6BB0)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "결제 정보를 불러오는 중...",
                        fontSize = 16.sp,
                        color = Color(0xFF7D6BB0)
                    )
                }
            }
            return@Column
        }

        // 에러 상태 표시
        uiState.errorMessage?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚠️",
                        fontSize = 48.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = error,
                        fontSize = 16.sp,
                        color = Color(0xFFFF6B6B),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.clearError()
                            if (qrData.isNotEmpty()) {
                                viewModel.loadPaymentInfo(qrData)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7D6BB0)
                        )
                    ) {
                        Text("다시 시도")
                    }
                }
            }
            return@Column
        }

        // 결제 정보가 로드된 경우에만 표시
        uiState.paymentInfo?.let { paymentInfo ->
            // 본문 (스크롤 가능)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(12.dp))

                // 주문 내역 제목
                Box(Modifier.width(342.dp)) {
                    Column {
                        Text(
                            text = "주문 내역",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                        Spacer(Modifier.height(8.dp))

                        // 주문한 메뉴들 리스트
                        paymentInfo.orderItems.forEach { orderItem ->
                            OrderItemRow(
                                name = orderItem.name,
                                price = orderItem.price.toInt()
                            )
                            Spacer(Modifier.height(4.dp))
                        }

                        Spacer(Modifier.height(8.dp))

                        // 구분선
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFE5E5E5))
                        )

                        Spacer(Modifier.height(8.dp))

                        // 총 금액
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "총 금액",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )
                            Text(
                                text = String.format("%,d원", paymentInfo.total.toInt()),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1C1C1E)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ▽ 제휴 할인 박스
                DiscountBox(
                    title = "${paymentInfo.department} 제휴 할인",
                    sub = "${paymentInfo.discountRate}% 할인 · 캠퍼스 카페",
                    amount = paymentInfo.discount.toInt()
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

                // 쿠폰 선택 영역
                Log.d("PaymentScreen", "쿠폰 데이터 확인: ${paymentInfo.coupons}")
                Log.d("PaymentScreen", "쿠폰 개수: ${paymentInfo.coupons?.size ?: 0}")
                
                // 실제 API 쿠폰 데이터 + 테스트 쿠폰 데이터
                val apiCoupons = paymentInfo.coupons ?: emptyList()
                val testCoupons = listOf(
                    DiscountCoupon(
                        discountCouponId = 14L,
                        amount = 500,
                        createdDate = "2025-07-30",
                        endDate = "2025-08-29",
                        couponType = "RANDOM"
                    ),
                    DiscountCoupon(
                        discountCouponId = 13L,
                        amount = 500,
                        createdDate = "2025-08-24",
                        endDate = "2025-09-23",
                        couponType = "ATTENDANCE"
                    )
                )
                
                val availableCoupons = if (apiCoupons.isNotEmpty()) {
                    Log.d("PaymentScreen", "API 쿠폰 사용: ${apiCoupons.size}개")
                    apiCoupons
                } else {
                    Log.d("PaymentScreen", "테스트 쿠폰 사용: ${testCoupons.size}개")
                    testCoupons
                }
                
                if (availableCoupons.isNotEmpty()) {
                    Log.d("PaymentScreen", "쿠폰 선택 UI 표시")
                    CouponSelector(
                        availableCoupons = availableCoupons,
                        selectedCoupon = uiState.selectedCoupon,
                        onCouponSelected = { coupon ->
                            viewModel.selectCoupon(coupon)
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                } else {
                    Log.d("PaymentScreen", "사용 가능한 쿠폰이 없습니다")
                }

                Spacer(Modifier.height(16.dp))

                // ▽ 결제 카드 표시 영역 (임시 카드 들어갈 영역)
                CardPreviewBox(

                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "총 ${String.format("%,d", paymentInfo.discount.toInt() + (uiState.selectedCoupon?.amount ?: 0))}원 할인을 받았어요",
                    fontSize = 12.sp,
                    color = Color(0xFF7D6BB0)
                )

                Spacer(Modifier.height(16.dp))

                // ▽ 결제 버튼 (그림자 + 보라)
                val finalPrice = viewModel.calculateFinalAmount()
                PaymentCta(
                    finalPrice = finalPrice,
                    enabled = !uiState.isProcessingPayment,
                    onClick = {
                        viewModel.processPayment()
                    }
                )

                if (uiState.isProcessingPayment) {
                    Spacer(Modifier.height(10.dp))
                    Text("결제 중…", fontSize = 12.sp, color = Color(0xFF7D6BB0))
                }

                // 결제 버튼이 화면에서 보이도록 충분한 공간 확보
                Spacer(Modifier.height(40.dp))
            }
        } ?: run {
            // 결제 정보가 없고 로딩도 아닌 경우
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "결제 정보를 불러올 수 없습니다",
                    fontSize = 16.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}


@Composable
private fun OrderItemRow(
    name: String,
    price: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 14.sp,
            color = Color(0xFF1C1C1E),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = String.format("%,d원", price),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1C1C1E)
        )
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
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF7D6BB0)
            )
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
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = com.heyyoung.solsol.R.drawable.shinhan_card),
            contentDescription = "신한 체크카드",
            modifier = Modifier
                .fillMaxWidth(0.95f)  // 95%로 여백 최소화
                .fillMaxHeight(0.85f) // 85%로 여백 최소화// ← 박스를 완전히 채움
                .clip(RoundedCornerShape(12.dp)),  // 박스와 같은 모서리 둥글기
            contentScale = ContentScale.Crop  // ← 박스를 가득 채우도록 잘림
        )

        // 텍스트 오버레이 (이미지 위에 표시)
        Text(
            text = "신한 체크카드 (4426-60**)",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = Color.White  // 검정 배경에 흰 글씨로 가독성 확보
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CouponSelector(
    availableCoupons: List<DiscountCoupon>,
    selectedCoupon: DiscountCoupon?,
    onCouponSelected: (DiscountCoupon?) -> Unit
) {
    Log.d("CouponSelector", "CouponSelector 시작 - 쿠폰 ${availableCoupons.size}개")
    availableCoupons.forEach { coupon ->
        Log.d("CouponSelector", "쿠폰: ${coupon.amount}원 (ID: ${coupon.discountCouponId})")
    }
    
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.width(342.dp)
    ) {
        // 쿠폰 선택 라벨
        Text(
            text = "💳 할인 쿠폰 선택",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1C1C1E),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 드롭다운 박스
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    spotColor = Color(0x1A000000),
                    ambientColor = Color(0x1A000000)
                )
                .border(
                    width = 1.dp,
                    color = Color(0x338B5FBF),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { expanded = true }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF7D6BB0)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = selectedCoupon?.let { 
                            "${String.format("%,d", it.amount)}원 할인 쿠폰"
                        } ?: "쿠폰을 선택해주세요",
                        fontSize = 14.sp,
                        color = if (selectedCoupon != null) Color(0xFF1C1C1E) else Color(0xFF999999)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color(0xFF7D6BB0)
                )
            }
        }
        
        // 드롭다운 메뉴 (최대 높이 제한)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)  // 최대 높이를 200dp로 제한
        ) {
            // "쿠폰 사용 안함" 옵션
            DropdownMenuItem(
                text = { 
                    Text(
                        "쿠폰 사용 안함",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                },
                onClick = {
                    onCouponSelected(null)
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF999999)
                    )
                }
            )
            
            // 사용 가능한 쿠폰들
            availableCoupons.forEach { coupon ->
                val couponType = CouponType.fromString(coupon.couponType)
                
                DropdownMenuItem(
                    text = { 
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = couponType.emoji,
                                    fontSize = 12.sp
                                )
                                Text(
                                    "${String.format("%,d", coupon.amount)}원 할인",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1C1C1E)
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                couponType.displayName,
                                fontSize = 11.sp,
                                color = Color(0xFF7D6BB0),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "만료일: ${formatCouponDate(coupon.endDate)}",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    },
                    onClick = {
                        onCouponSelected(coupon)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF7D6BB0)
                        )
                    }
                )
            }
        }
        
        // 선택된 쿠폰 정보 표시
        selectedCoupon?.let { coupon ->
            val couponType = CouponType.fromString(coupon.couponType)
            Spacer(Modifier.height(8.dp))
            
            Column(
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "${couponType.emoji} ${String.format("%,d", coupon.amount)}원 추가 할인이 적용됩니다",
                    fontSize = 12.sp,
                    color = Color(0xFF7D6BB0),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = couponType.description,
                    fontSize = 10.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun formatCouponDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: java.util.Date())
    } catch (e: Exception) {
        dateString
    }
}
