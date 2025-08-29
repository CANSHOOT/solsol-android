package com.heyyoung.solsol.feature.remittance.presentation

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemittanceScreen(
    groupId: Long,
    receiverName: String = "김신한",
    receiverInfo: String = "관리자명의 | 2020.7.8",
    amount: String = "29,002",
    cardNumber: String = "****1234",
    onNavigateBack: () -> Unit = {},
    onRemittanceComplete: () -> Unit = {}
) {
    var showSuccessScreen by remember { mutableStateOf(false) }
    val viewModel: RemittanceViewModel = hiltViewModel()

    if (showSuccessScreen) {
        RemittanceSuccessScreen(
            receiverName = receiverName,
            amount = amount,
            onComplete = {
                showSuccessScreen = false
                onRemittanceComplete()
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
            title = { Text("송금하기") },
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
            Spacer(Modifier.height(20.dp))

            // 수신자 정보 박스
            ReceiverInfoBox(
                receiverName = receiverName,
                receiverInfo = receiverInfo
            )

            Spacer(Modifier.height(20.dp))

            // 송금 금액 박스
            AmountBox(amount = amount)

            Spacer(Modifier.height(20.dp))

            // 카드 정보 박스
            CardInfoBox(cardNumber = cardNumber)

            Spacer(Modifier.height(32.dp))

            // 수수료 안내
            Text(
                text = "헤이영 학생 간 송금 수수료 무료",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // 송금하기 버튼
            Button(
                onClick = {
                    groupId?.let {
                        viewModel.sendPayment(it, "정산 송금")
                    }

                    showSuccessScreen = true
                },
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        spotColor = Color(0x40000000),
                        ambientColor = Color(0x40000000)
                    )
                    .width(342.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5FBF),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = "송금하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ReceiverInfoBox(
    receiverName: String,
    receiverInfo: String
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
                color = Color(0xB2E2E8F0),
                shape = RoundedCornerShape(size = 12.dp)
            )
            .width(342.dp)
            .height(100.dp)
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 아이콘
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color(0xFFE5E5E5),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF666666)
            )
        }

        Spacer(Modifier.width(12.dp))

        // 수신자 정보
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = receiverName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = receiverInfo,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun AmountBox(amount: String) {
    Column(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = 1.dp,
                color = Color(0xCC8B5FBF),
                shape = RoundedCornerShape(size = 16.dp)
            )
            .padding(1.dp)
            .width(342.dp)
            .height(120.dp)
            .background(
                color = Color(0xFFF8F7FF),
                shape = RoundedCornerShape(size = 16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "송금 금액",
            fontSize = 14.sp,
            color = Color(0xFF7D6BB0),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${amount}원",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1C1C1E)
        )
    }
}

@Composable
private fun CardInfoBox(cardNumber: String) {
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
            .height(200.dp)
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 카드 이미지
            Image(
                painter = painterResource(id = com.heyyoung.solsol.R.drawable.shinhan_card),
                contentDescription = "신한 체크카드",
                modifier = Modifier
                    .width(280.dp)
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(8.dp))

            // 카드 정보
            Text(
                text = "신한 체크카드 ($cardNumber)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E)
            )
        }
    }
}