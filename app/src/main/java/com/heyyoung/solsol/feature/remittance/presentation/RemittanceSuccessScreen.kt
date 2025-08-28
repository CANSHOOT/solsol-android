package com.heyyoung.solsol.feature.remittance.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemittanceSuccessScreen(
    receiverName: String = "김신한",
    amount: String = "29,002",
    onComplete: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("송금하기") },
            navigationIcon = {
                IconButton(onClick = onComplete) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            },
            actions = {
                IconButton(onClick = onComplete) {
                    Icon(Icons.Default.Close, contentDescription = "닫기")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1C1C1E),
                navigationIconContentColor = Color(0xFF1C1C1E),
                actionIconContentColor = Color(0xFF1C1C1E)
            )
        )

        Spacer(Modifier.height(80.dp))

        // 체크 아이콘
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF8B5FBF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        // 송금 완료 메시지
        Text(
            "${receiverName}님에게 ${amount}원 전송",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1C1C1E),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "송금 완료되었습니다!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        // 완료 버튼
        /*
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
        */

        Spacer(Modifier.height(32.dp))
    }
}