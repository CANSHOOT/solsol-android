package com.heyyoung.solsol.feature.settlement.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TAG = "SettlementMethodScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementMethodScreen(
    onNavigateBack: () -> Unit = {},
    onMethodSelected: (String) -> Unit = {}
) {
    // 선택된 방식 상태 관리
    var selectedMethod by remember { mutableStateOf<String?>(null) }

    Log.d(TAG, "정산 방식 선택 화면 진입")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("정산하기") },
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

        // 메인 컨텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // 안내 텍스트
            Text(
                text = "정산 방식을 선택해주세요",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 똑같이 나누기
            SettlementOptionCard(
                title = "똑같이 나누기",
                description = "총 금액을 인원수로 나누어",
                isSelected = selectedMethod == "equal",
                onClick = {
                    Log.d(TAG, "똑같이 나누기 선택")
                    selectedMethod = "equal"
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 직접 입력하기
            SettlementOptionCard(
                title = "직접 입력하기",
                description = "사람별로 다른 금액 입력",
                isSelected = selectedMethod == "manual",
                onClick = {
                    Log.d(TAG, "직접 입력하기 선택")
                    selectedMethod = "manual"
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 랜덤 게임으로 정하기
            SettlementOptionCard(
                title = "랜덤 게임으로 정하기",
                description = "누가 쏠지 제비뽑기로 뽑아보세요!",
                isSelected = selectedMethod == "random",
                onClick = {
                    Log.d(TAG, "랜덤 게임으로 정하기 선택")
                    selectedMethod = "random"
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 다음 버튼
            Button(
                onClick = {
                    selectedMethod?.let { method ->
                        Log.d(TAG, "선택된 방식으로 진행: $method")
                        onMethodSelected(method)
                    }
                },
                enabled = selectedMethod != null,
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        spotColor = Color(0x26000000),
                        ambientColor = Color(0x26000000)
                    )
                    .width(342.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xE58B5FBF),
                    disabledContainerColor = Color(0x4D8B5FBF)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "다음",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SettlementOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
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
                color = if (isSelected) Color(0xFF8B5FBF) else Color(0xCCE2E8F0),
                shape = RoundedCornerShape(16.dp)
            )
            .width(330.dp)
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF8B5FBF) else Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = if (isSelected) Color(0xFF8B5FBF) else Color(0xFF666666)
            )
        }
    }
}