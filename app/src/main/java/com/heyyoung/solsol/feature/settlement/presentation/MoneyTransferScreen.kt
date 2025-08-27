package com.heyyoung.solsol.feature.settlement.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TAG = "MoneyTransferScreen"
enum class TransferSide { SENT, RECEIVED }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyTransferScreen(
    onNavigateBack: () -> Unit = {}
) {
    // 데모 데이터
    val receivedRequests = remember { getReceivedRequests() }
    val sentRequests = remember { getSentRequests() }

    Log.d(TAG, "송금하기 화면 진입")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("송금하기") },
            navigationIcon = {
                IconButton(onClick = {
                    Log.d(TAG, "뒤로가기 클릭")
                    onNavigateBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1C1C1E),
                navigationIconContentColor = Color(0xFF1C1C1E)
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 페이지 제목
            item {
                Text(
                    text = "정산현황",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }

            // 받은 요청 섹션
            if (receivedRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "받은 요청 (내가 보내야 할 돈)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(receivedRequests) { request ->
                    MoneyTransferCard(
                        name = request.name,
                        amount = request.amount,
                        status = request.status
                    )
                }
            }

            // 보낸 요청 섹션
            if (sentRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "보낸 요청 (내가 받아야 할 돈)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(sentRequests) { request ->
                    MoneyTransferCard(
                        name = request.name,
                        amount = request.amount,
                        status = request.status
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun MoneyTransferCard(
    name: String,
    amount: Long,
    status: MoneyTransferStatus
) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = 2.dp,
                color = Color(0xB28B5FBF),
                shape = RoundedCornerShape(12.dp)
            )
            .fillMaxWidth()
            .height(60.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 이미지
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF999999),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 이름만 표시
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 금액
                Text(
                    text = "${String.format("%,d", amount)}원",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )

                // 상태 버튼
                StatusButton(status = status)
            }
        }
    }
}

@Composable
private fun StatusButton(status: MoneyTransferStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        MoneyTransferStatus.PENDING -> Triple(Color(0xFFFFA500), Color.White, "진행중")
        MoneyTransferStatus.COMPLETED -> Triple(Color(0xFF10B981), Color.White, "완료")
    }

    Button(
        onClick = {
            Log.d(TAG, "상태 버튼 클릭: $status")
        },
        modifier = Modifier
            .height(28.dp)
            .width(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

// 데모 데이터
private fun getReceivedRequests(): List<MoneyTransferItem> = listOf(
    MoneyTransferItem(
        name = "김신한",
        amount = 29002L,
        status = MoneyTransferStatus.PENDING,
        TransferSide.SENT
    ),
    MoneyTransferItem(
        name = "이지헌",
        amount = 8500L,
        status = MoneyTransferStatus.COMPLETED,
        TransferSide.RECEIVED
    )
)

private fun getSentRequests(): List<MoneyTransferItem> = listOf(
    MoneyTransferItem(
        name = "박민수",
        amount = 15000L,
        status = MoneyTransferStatus.PENDING,
        TransferSide.SENT
    ),
    MoneyTransferItem(
        name = "최영희",
        amount = 12500L,
        status = MoneyTransferStatus.COMPLETED,
        TransferSide.RECEIVED
    )
)

data class MoneyTransferItem(
    val name: String,
    val amount: Long,
    val status: MoneyTransferStatus,
    val side: TransferSide
)

enum class MoneyTransferStatus {
    PENDING,    // 진행중
    COMPLETED   // 완료
}