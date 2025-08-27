package com.heyyoung.solsol.feature.coupon.presentation

import android.util.Log
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.heyyoung.solsol.R
import com.heyyoung.solsol.feature.payment.domain.CouponItem
import com.heyyoung.solsol.ui.components.modifiers.solsolGradientBackground
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

    // 화면 진입시 쿠폰 목록 로드
    LaunchedEffect(Unit) {
        Log.d(TAG, "쿠폰 화면 진입 - 쿠폰 목록 로드")
        viewModel.loadCoupons()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .solsolGradientBackground() // 그라데이션 배경 적용
    ) {
        // 상단 앱바 (투명 배경)
        CenterAlignedTopAppBar(
            title = { 
                Text(
                    "내 쿠폰함",
                    color = colorResource(id = R.color.solsol_white),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack, 
                        contentDescription = "뒤로",
                        tint = colorResource(id = R.color.solsol_white)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
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
                        color = colorResource(id = R.color.solsol_white)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "쿠폰을 불러오는 중...",
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.solsol_white),
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚠️",
                        fontSize = 48.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = error,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.solsol_white),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.clearError()
                            viewModel.loadCoupons()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.solsol_white).copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            "다시 시도",
                            color = colorResource(id = R.color.solsol_white),
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
                    // 스타 아이콘을 동그란 배경과 함께
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                colorResource(id = R.color.solsol_white).copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = colorResource(id = R.color.solsol_white)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "보유 중인 쿠폰이 없습니다",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.solsol_white)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "결제 시 럭키 쿠폰을 받아보세요! ✨",
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.solsol_white).copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // 쿠폰 목록
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // 쿠폰 개수 표시
                Text(
                    text = "💳 보유 쿠폰 ${uiState.coupons.size}장",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.solsol_white),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // 쿠폰 리스트
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
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
    // 만료일까지 남은 일수 계산
    val daysUntilExpiry = calculateDaysUntilExpiry(coupon.endDate)
    val isExpiringSoon = daysUntilExpiry <= 7
    
    // 쿠폰 상태에 따른 그라데이션 색상
    val gradientColors = if (isExpiringSoon) {
        listOf(Color(0xFFFF8A80), Color(0xFFFF5722)) // 주황-빨강 그라데이션 
    } else {
        listOf(Color(0xFF8B5FBF), Color(0xFF9C27B0)) // 보라 그라데이션
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x40000000),
                ambientColor = Color(0x40000000),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽 쿠폰 아이콘 영역 (더 크고 화려하게)
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(16.dp))

                // 중간 쿠폰 정보
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${String.format("%,d", coupon.amount)}원",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    
                    Text(
                        text = "할인 쿠폰",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "${formatDate(coupon.createdDate)} ~ ${formatDate(coupon.endDate)}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }

                // 오른쪽 상태 표시 (더 예쁘게)
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (isExpiringSoon) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color.White.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🚨 곧 만료",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }

                    Text(
                        text = "${daysUntilExpiry}일 남음",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // 쿠폰 느낌의 점선 장식 (오른쪽 끝에)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp)
                    .size(20.dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        CircleShape
                    )
            )
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
