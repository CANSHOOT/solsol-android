package com.heyyoung.solsol.feature.payment.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import com.heyyoung.solsol.feature.payment.domain.PaymentHistoryItem
import com.heyyoung.solsol.feature.payment.domain.PaymentHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: PaymentHistoryViewModel = hiltViewModel()
) {
    val TAG = "PaymentHistoryScreen"
    val uiState = viewModel.uiState

    // 하드웨어/제스처 뒤로가기 버튼 처리
    BackHandler {
        onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바 - 더 깔끔하게
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "결제 내역",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748) // solsol_dark_text
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "뒤로",
                        tint = Color(0xFF2D3748) // solsol_dark_text
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "새로고침",
                        tint = Color(0xFF8B5FBF) // solsol_purple
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF2D3748), // solsol_dark_text
                navigationIconContentColor = Color(0xFF2D3748) // solsol_dark_text
            )
        )

        // 로딩 상태 표시 - 더 트렌디하게
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color(0xFF8B5FBF), // solsol_purple
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "결제 내역을 불러오는 중...",
                        fontSize = 16.sp,
                        color = Color(0xFF718096), // solsol_gray_text
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            return@Column
        }

        // 에러 상태 표시 - 더 깔끔하게
        uiState.errorMessage?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 에러 아이콘을 색상 박스로 대체
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = Color(0xFFFF6B6B).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = Color(0xFFFF6B6B),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = error,
                        fontSize = 16.sp,
                        color = Color(0xFF2D3748), // solsol_dark_text
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.clearError()
                            viewModel.refresh()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5FBF) // solsol_purple
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .shadow(
                                elevation = 8.dp,
                                spotColor = Color(0x1A8B5FBF),
                                ambientColor = Color(0x1A8B5FBF)
                            )
                    ) {
                        Text(
                            "다시 시도",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            return@Column
        }

        // 결제 내역 목록
        if (uiState.paymentHistory.isEmpty()) {
            // 결제 내역이 없을 때 - 더 친근하게
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 빈 상태 아이콘을 색상 박스로 대체
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                color = Color(0xFF8B5FBF).copy(alpha = 0.1f), // solsol_purple with transparency
                                shape = RoundedCornerShape(25.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF8B5FBF) // solsol_purple
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "아직 결제 내역이 없습니다",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748) // solsol_dark_text
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "QR 코드를 스캔해서 첫 결제를 해보세요!",
                        fontSize = 15.sp,
                        color = Color(0xFF718096), // solsol_gray_text
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // 결제 내역 리스트 - 더 트렌디한 간격
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp), // 16dp에서 20dp로 증가
                verticalArrangement = Arrangement.spacedBy(16.dp), // 12dp에서 16dp로 증가
                contentPadding = PaddingValues(vertical = 20.dp) // 16dp에서 20dp로 증가
            ) {
                itemsIndexed(uiState.paymentHistory) { index, paymentItem ->
                    PaymentHistoryCard(
                        paymentItem = paymentItem,
                        displayNumber = index + 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentHistoryCard(
    paymentItem: PaymentHistoryItem,
    displayNumber: Int
) {
    val formattedDate = formatDate(paymentItem.date)

    Column(
        modifier = Modifier
            .shadow(
                elevation = 12.dp, // 4dp에서 12dp로 증가
                spotColor = Color(0x1A8B5FBF),
                ambientColor = Color(0x1A8B5FBF)
            )
            .background(
                color = Color(0xFFFFFFFF), // solsol_card_white
                shape = RoundedCornerShape(16.dp) // 12dp에서 16dp로 증가
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE2E8F0), // solsol_light_gray
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp) // 16dp에서 20dp로 증가
            .fillMaxWidth()
    ) {
        // 결제 번호와 날짜/시간 - 더 깔끔하게
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 결제 번호 색상 표시기
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = Color(0xFF8B5FBF).copy(alpha = 0.1f), // solsol_purple with transparency
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$displayNumber",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5FBF) // solsol_purple
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "결제",
                    fontSize = 18.sp, // 16sp에서 18sp로 증가
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748) // solsol_dark_text
                )
            }

            // 날짜/시간을 컬럼으로 배치해서 더 읽기 쉽게
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val dateTimeParts = formattedDate.split(" ")
                if (dateTimeParts.size >= 2) {
                    // 날짜 부분
                    Text(
                        text = "${dateTimeParts[0]} ${dateTimeParts[1]}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF718096) // solsol_gray_text
                    )
                    // 시간 부분
                    Text(
                        text = dateTimeParts[2],
                        fontSize = 12.sp,
                        color = Color(0xFF718096).copy(alpha = 0.8f) // solsol_gray_text with transparency
                    )
                } else {
                    Text(
                        text = formattedDate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF718096) // solsol_gray_text
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp)) // 12dp에서 16dp로 증가

        // 금액 정보 - 더 깔끔한 카드 형태
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFF0F0F0).copy(alpha = 0.3f), // solsol_background_gray with transparency
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // 6dp에서 8dp로 증가
        ) {
            // 원래 금액
            PaymentAmountRow(
                label = "원래 금액",
                amount = paymentItem.originalAmount,
                color = Color(0xFF718096) // solsol_gray_text
            )

            // 할인 금액
            if (paymentItem.discountAmount > 0) {
                PaymentAmountRow(
                    label = "할인 금액",
                    amount = -paymentItem.discountAmount,
                    color = Color(0xFF8B5FBF), // solsol_purple
                    isDiscount = true
                )
            }

            // 구분선 - 더 부드럽게
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE2E8F0)) // solsol_light_gray
            )

            // 최종 결제 금액 - 더 강조
            PaymentAmountRow(
                label = "결제 금액",
                amount = paymentItem.finalAmount,
                color = Color(0xFF2D3748), // solsol_dark_text
                isFinal = true
            )
        }

        // 할인 효과 표시 - 더 트렌디하게
        if (paymentItem.discountAmount > 0) {
            Spacer(Modifier.height(12.dp)) // 8dp에서 12dp로 증가

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = Color(0xFF8B5FBF).copy(alpha = 0.1f), // solsol_purple with transparency
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp) // 패딩 증가
            ) {
                // 할인 표시기
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = Color(0xFF8B5FBF).copy(alpha = 0.2f), // solsol_purple with transparency
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = Color(0xFF8B5FBF), // solsol_purple
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "총 ${String.format("%,d", paymentItem.discountAmount)}원 할인받았어요!", // 이모지 제거
                    fontSize = 13.sp, // 12sp에서 13sp로 증가
                    color = Color(0xFF8B5FBF), // solsol_purple
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PaymentAmountRow(
    label: String,
    amount: Int,
    color: Color,
    isDiscount: Boolean = false,
    isFinal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isFinal) 16.sp else 14.sp,
            fontWeight = if (isFinal) FontWeight.Bold else FontWeight.Medium,
            color = color
        )
        Text(
            text = if (isDiscount)
                "-${String.format("%,d", -amount)}원"
            else
                "${String.format("%,d", amount)}원",
            fontSize = if (isFinal) 18.sp else 14.sp,
            fontWeight = if (isFinal) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = color
        )
    }
}

private fun formatDate(dateString: String): String {
    // 여러 가능한 LocalDateTime 형식들을 시도
    val possibleFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS",    // 밀리초 포함
        "yyyy-MM-dd'T'HH:mm:ss",        // 기본 ISO 형식
        "yyyy-MM-dd'T'HH:mm:ss'Z'",     // UTC 표시
        "yyyy-MM-dd HH:mm:ss",          // 공백 구분
        "yyyy-MM-dd"                    // 날짜만 (백업용)
    )

    for (formatPattern in possibleFormats) {
        try {
            val inputFormat = SimpleDateFormat(formatPattern, Locale.getDefault())
            val date = inputFormat.parse(dateString)

            if (date != null) {
                // 시간이 포함된 형식인지 확인
                val hasTime = formatPattern.contains("HH:mm")
                val outputFormat = if (hasTime) {
                    SimpleDateFormat("MM월 dd일 HH:mm", Locale.getDefault())
                } else {
                    SimpleDateFormat("MM월 dd일", Locale.getDefault())
                }

                return outputFormat.format(date)
            }
        } catch (e: Exception) {
            // 다음 형식 시도
            continue
        }
    }

    Log.e("PaymentHistoryScreen", "모든 날짜 형식 변환 실패: $dateString")
    return dateString
}