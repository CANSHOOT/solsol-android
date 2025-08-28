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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("결제 내역") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "새로고침",
                        tint = Color(0xFF7D6BB0)
                    )
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
                        text = "결제 내역을 불러오는 중...",
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
                            viewModel.refresh()
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

        // 결제 내역 목록
        if (uiState.paymentHistory.isEmpty()) {
            // 결제 내역이 없을 때
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF999999)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "아직 결제 내역이 없습니다",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF999999)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "QR 코드를 스캔해서 첫 결제를 해보세요!",
                        fontSize = 14.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
        } else {
            // 결제 내역 리스트
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                itemsIndexed(uiState.paymentHistory) { index, paymentItem ->
                    PaymentHistoryCard(
                        paymentItem = paymentItem,
                        displayNumber = index + 1  // 1부터 시작하는 순서 번호
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
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = 1.dp,
                color = Color(0x338B5FBF),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // 결제 번호와 날짜/시간
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "결제 #${displayNumber}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            
            // 날짜/시간을 컬럼으로 배치해서 더 읽기 쉽게
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val dateTimeParts = formattedDate.split(" ")
                if (dateTimeParts.size >= 2) {
                    // 날짜 부분 (예: "12월 25일")
                    Text(
                        text = "${dateTimeParts[0]} ${dateTimeParts[1]}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                    // 시간 부분 (예: "14:30")
                    Text(
                        text = dateTimeParts[2],
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                } else {
                    // 시간이 없는 경우 (날짜만)
                    Text(
                        text = formattedDate,
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // 금액 정보
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 원래 금액
            PaymentAmountRow(
                label = "원래 금액",
                amount = paymentItem.originalAmount,
                color = Color(0xFF666666)
            )
            
            // 할인 금액
            if (paymentItem.discountAmount > 0) {
                PaymentAmountRow(
                    label = "할인 금액",
                    amount = -paymentItem.discountAmount,
                    color = Color(0xFF7D6BB0),
                    isDiscount = true
                )
            }

            // 구분선
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE5E5E5))
            )

            // 최종 결제 금액
            PaymentAmountRow(
                label = "결제 금액",
                amount = paymentItem.finalAmount,
                color = Color(0xFF1C1C1E),
                isFinal = true
            )
        }

        // 할인 효과 표시
        if (paymentItem.discountAmount > 0) {
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = "💰 총 ${String.format("%,d", paymentItem.discountAmount)}원 할인받았어요!",
                fontSize = 12.sp,
                color = Color(0xFF7D6BB0),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(
                        Color(0xFFF8F7FF),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
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
            fontWeight = if (isFinal) FontWeight.ExtraBold else FontWeight.Medium,
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
