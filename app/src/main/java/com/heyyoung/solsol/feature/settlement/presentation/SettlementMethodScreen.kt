package com.heyyoung.solsol.feature.settlement.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.heyyoung.solsol.feature.settlement.presentation.viewmodel.NearbyViewModel
import com.heyyoung.solsol.feature.settlement.presentation.components.NearbyPermissionDialog

private const val TAG = "SettlementMethodScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementMethodScreen(
    onNavigateBack: () -> Unit = {},
    onMethodSelected: (String) -> Unit = {},
    onNavigateToGame: () -> Unit = {},
    nearbyViewModel: NearbyViewModel = hiltViewModel()
) {
    // 선택된 방식 상태 관리 (초기값을 명시적으로 null로 설정)
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    
    // 권한 상태
    val showPermissionDialog by nearbyViewModel.showPermissionDialog.collectAsState()
    val permissionMessage by nearbyViewModel.permissionMessage.collectAsState()

    Log.d(TAG, "정산 방식 선택 화면 진입")
    Log.d(TAG, "현재 선택된 방식: $selectedMethod")

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
                    Log.d(TAG, "🟦 똑같이 나누기 클릭됨")
                    selectedMethod = "equal"
                    Log.d(TAG, "상태 변경됨: $selectedMethod")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 직접 입력하기 - 문제가 있던 부분
            SettlementOptionCard(
                title = "직접 입력하기",
                description = "사람별로 다른 금액 입력",
                isSelected = selectedMethod == "manual",
                onClick = {
                    Log.d(TAG, "🟨 직접 입력하기 클릭됨")
                    selectedMethod = "manual"
                    Log.d(TAG, "상태 변경됨: $selectedMethod")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 랜덤 게임으로 정하기
            SettlementOptionCard(
                title = "랜덤 게임으로 정하기",
                description = "누가 쏠지 제비뽑기로 뽑아보세요!",
                isSelected = selectedMethod == "random",
                onClick = {
                    Log.d(TAG, "🟩 랜덤 게임으로 정하기 클릭됨")
                    selectedMethod = "random"
                    Log.d(TAG, "상태 변경됨: $selectedMethod")
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 현재 선택 상태 디버그 표시 (개발용)
            if (selectedMethod != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0F9FF)
                    )
                ) {
                    Text(
                        text = "✅ 선택됨: ${when(selectedMethod) {
                            "equal" -> "똑같이 나누기"
                            "manual" -> "직접 입력하기"
                            "random" -> "랜덤 게임"
                            else -> "알 수 없음"
                        }}",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF1E40AF),
                        fontSize = 14.sp
                    )
                }
            }

            // 다음 버튼
            Button(
                onClick = {
                    Log.d(TAG, "다음 버튼 클릭, 선택된 방식: $selectedMethod")
                    selectedMethod?.let { method ->
                        Log.d(TAG, "선택된 방식으로 진행: $method")
                        if (method == "random") {
                            Log.d(TAG, "랜덤 게임으로 네비게이션 - 권한 확인 중...")
                            // 권한 체크 먼저
                            nearbyViewModel.checkPermissionsAndNavigateToGame {
                                // 권한이 있으면 게임으로 이동
                                onNavigateToGame()
                            }
                        } else {
                            onMethodSelected(method)
                        }
                    } ?: Log.w(TAG, "선택된 방식이 없음")
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
                    containerColor = Color(0xFF8B5FBF),
                    disabledContainerColor = Color(0x4D8B5FBF)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = if (selectedMethod != null) "다음" else "방식을 선택하세요",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    
    // 권한 다이얼로그
    NearbyPermissionDialog(
        isVisible = showPermissionDialog,
        onDismiss = { nearbyViewModel.hidePermissionDialog() },
        onRequestPermissions = { 
            // 권한 요청 처리
            nearbyViewModel.onPermissionsGranted()
        },
        onOpenSettings = { 
            // 설정 앱 열기
            nearbyViewModel.hidePermissionDialog()
        },
        permissionMessage = permissionMessage
    )
}

@Composable
private fun SettlementOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Log.d(TAG, "카드 렌더링: $title, 선택됨: $isSelected")

    Card(
        modifier = Modifier
            .shadow(
                elevation = if (isSelected) 6.dp else 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF8B5FBF) else Color(0xCCE2E8F0),
                shape = RoundedCornerShape(16.dp)
            )
            .width(330.dp)
            .height(110.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // 리플 효과 제거
            ) {
                Log.d(TAG, "카드 클릭됨: $title")
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF8F4FD) else Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 메인 텍스트
            Column(
                modifier = Modifier.weight(1f),
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

            // 선택 표시 아이콘
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "선택됨",
                    tint = Color(0xFF8B5FBF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}