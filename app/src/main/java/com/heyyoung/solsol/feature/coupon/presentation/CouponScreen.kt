package com.heyyoung.solsol.feature.coupon.presentation

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import com.heyyoung.solsol.R
import com.heyyoung.solsol.feature.payment.domain.CouponItem
import com.heyyoung.solsol.feature.payment.domain.CouponType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: CouponViewModel = hiltViewModel()
) {
    val TAG = "CouponScreen"
    val uiState = viewModel.uiState
    val purple = Color(0xFF8B5FBF)
    val textMain = Color(0xFF2D3748)
    val textSub = Color(0xFF718096)

    // 화면 진입시 쿠폰 목록 로드
    LaunchedEffect(Unit) {
        Log.d(TAG, "쿠폰 화면 진입 - 쿠폰 목록 로드")
        viewModel.loadCoupons()
    }

    // 하드웨어/제스처 뒤로가기 버튼 처리
    BackHandler {
        onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFAFA),
                        Color.White
                    )
                )
            )
    ) {
        // 상단 앱바 - 모던 스타일
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "내 쿠폰함",
                    color = textMain,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFFF7FAFC),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "뒤로",
                        tint = textMain,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )

        // 로딩 상태 표시
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        purple.copy(alpha = 0.1f),
                                        purple.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = purple,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "쿠폰을 불러오는 중...",
                        fontSize = 16.sp,
                        color = textSub,
                        fontWeight = FontWeight.Medium
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF6B6B).copy(alpha = 0.1f),
                                        Color(0xFFFF6B6B).copy(alpha = 0.05f)
                                    )
                                ),
                                shape = RoundedCornerShape(25.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 48.sp
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "쿠폰을 불러오지 못했습니다",
                        fontSize = 18.sp,
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error,
                        fontSize = 14.sp,
                        color = textSub,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.clearError()
                            viewModel.loadCoupons()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = purple
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.shadow(
                            elevation = 8.dp,
                            spotColor = Color(0x338B5FBF),
                            shape = RoundedCornerShape(16.dp)
                        )
                    ) {
                        Text(
                            "다시 시도",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            return@Column
        }

        // 쿠폰 목록 표시
        if (uiState.coupons.isEmpty()) {
            // 쿠폰이 없는 경우
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 쿠폰 이미지 아이콘
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        purple.copy(alpha = 0.12f),
                                        purple.copy(alpha = 0.06f)
                                    )
                                ),
                                shape = RoundedCornerShape(30.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.coupon),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = "보유 중인 쿠폰이 없습니다",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMain
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "결제 시 럭키 쿠폰을 받아보세요!",
                        fontSize = 16.sp,
                        color = textSub,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    )
                }
            }
        } else {
            // 쿠폰 목록
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // 쿠폰 개수 표시 - 헤더 스타일
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        purple.copy(alpha = 0.15f),
                                        purple.copy(alpha = 0.08f)
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val purple = Color(0xFF8B5FBF)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(purple.copy(alpha = 0.12f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(purple, shape = CircleShape)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "보유 쿠폰 ${uiState.coupons.size}장",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = purple
                    )
                }

                Spacer(Modifier.height(8.dp))

                // 쿠폰 리스트
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(uiState.coupons) { coupon ->
                        CouponCard(coupon = coupon)
                    }
                }
            }
        }
    }
}

@Composable
private fun CouponCard(coupon: CouponItem) {
    val purple = Color(0xFF8B5FBF)
    val textMain = Color(0xFF2D3748)
    val textSub = Color(0xFF718096)

    // 만료일까지 남은 일수 계산
    val daysUntilExpiry = calculateDaysUntilExpiry(coupon.endDate)
    val isExpiringSoon = daysUntilExpiry <= 7

    // 쿠폰 타입 정보
    val couponType = CouponType.fromString(coupon.couponType)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x1A8B5FBF),
                ambientColor = Color(0x1A8B5FBF),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽 쿠폰 아이콘 영역
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                purple.copy(alpha = 0.15f),
                                purple.copy(alpha = 0.08f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.coupon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.width(20.dp))

            // 중간 쿠폰 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${String.format("%,d", coupon.amount)}원",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = purple
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = couponType.displayName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = couponType.description,
                    fontSize = 13.sp,
                    color = textSub,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "${formatDate(coupon.createdDate)} ~ ${formatDate(coupon.endDate)}",
                    fontSize = 12.sp,
                    color = textSub,
                    fontWeight = FontWeight.Medium
                )
            }

            // 오른쪽 상태 표시
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // 만료 임박 경고
                if (isExpiringSoon) {
                    Box(
                        modifier = Modifier
                            .background(
                                Color(0xFFFFB366).copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "곧 만료",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB366)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // 남은 일수
                Text(
                    text = "${daysUntilExpiry}일 남음",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when {
                        daysUntilExpiry <= 3 -> Color(0xFFFF4D4D) // 3일 이하 → 빨간색
                        else -> Color(0xFF8B5FBF)                 // 그 외 → 보라색
                    }
                )
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

private fun calculateDaysUntilExpiry(endDateString: String): Long {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val endDate = format.parse(endDateString)
        val currentDate = Date()
        val diffInMillis = (endDate?.time ?: 0) - currentDate.time
        val daysUntilExpiry = diffInMillis / (1000 * 60 * 60 * 24)
        maxOf(0, daysUntilExpiry) // 음수 방지
    } catch (e: Exception) {
        0
    }
}