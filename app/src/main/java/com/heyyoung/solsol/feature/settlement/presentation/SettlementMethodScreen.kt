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
        // 상단 앱바 - 더 깔끔하게
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "정산하기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748) // solsol_dark_text
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    Log.d(TAG, "뒤로가기 클릭")
                    onNavigateBack()
                }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "뒤로",
                        tint = Color(0xFF2D3748) // solsol_dark_text
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF2D3748), // solsol_dark_text
                navigationIconContentColor = Color(0xFF2D3748) // solsol_dark_text
            )
        )

        // 메인 컨텐츠 - 더 여유로운 레이아웃
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp)) // 60dp에서 40dp로 조정

            // 안내 텍스트 - 더 트렌디하게
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "정산 방식을 선택해주세요",
                    fontSize = 22.sp, // 20sp에서 22sp로 증가
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748) // solsol_dark_text
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "가장 편한 방법으로 정산해보세요",
                    fontSize = 15.sp,
                    color = Color(0xFF718096), // solsol_gray_text
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(48.dp)) // 40dp에서 48dp로 증가

            // 똑같이 나누기 - 개선된 카드 디자인
            SettlementOptionCard(
                title = "똑같이 나누기",
                description = "총 금액을 인원수로 나누어",
                isSelected = selectedMethod == "equal",
                onClick = {
                    Log.d(TAG, "똑같이 나누기 클릭됨")
                    selectedMethod = "equal"
                    Log.d(TAG, "상태 변경됨: $selectedMethod")
                }
            )

            Spacer(modifier = Modifier.height(20.dp)) // 16dp에서 20dp로 증가

            // 직접 입력하기 - 문제가 있던 부분
            SettlementOptionCard(
                title = "직접 입력하기",
                description = "사람별로 다른 금액 입력",
                isSelected = selectedMethod == "manual",
                onClick = {
                    Log.d(TAG, "직접 입력하기 클릭됨")
                    selectedMethod = "manual"
                    Log.d(TAG, "상태 변경됨: $selectedMethod")
                }
            )

            Spacer(modifier = Modifier.height(20.dp)) // 16dp에서 20dp로 증가

            // 랜덤 게임으로 정하기 - 더 재미있게
            SettlementOptionCard(
                title = "랜덤 게임으로 정하기",
                description = "누가 적지 제비뽑기로 뽑아보세요!",
                isSelected = selectedMethod == "random",
                onClick = {
                    Log.d(TAG, "랜덤 게임으로 정하기 클릭됨")
                    selectedMethod = "random"
                    Log.d(TAG, "상태 변경됨: $selectedMethod")
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 현재 선택 상태 디버그 표시 (개발용) - 더 깔끔하게
            if (selectedMethod != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF8B5FBF).copy(alpha = 0.08f), // solsol_purple with very low transparency
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF8B5FBF).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    color = Color(0xFF8B5FBF).copy(alpha = 0.2f),
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
                            text = "선택됨: ${when(selectedMethod) {
                                "equal" -> "똑같이 나누기"
                                "manual" -> "직접 입력하기"
                                "random" -> "랜덤 게임"
                                else -> "알 수 없음"
                            }}",
                            color = Color(0xFF8B5FBF), // solsol_purple
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 다음 버튼 - 더 트렌디한 디자인
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
                        elevation = 16.dp, // 8dp에서 16dp로 증가
                        spotColor = Color(0x308B5FBF),
                        ambientColor = Color(0x308B5FBF)
                    )
                    .fillMaxWidth(0.9f) // 342.dp 대신 비율로
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5FBF), // solsol_purple
                    disabledContainerColor = Color(0xFF8B5FBF).copy(alpha = 0.4f) // solsol_purple with transparency
                ),
                shape = RoundedCornerShape(16.dp) // 28dp에서 16dp로 조정 (더 모던한 느낌)
            ) {
                Text(
                    text = if (selectedMethod != null) "다음" else "방식을 선택하세요",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF) // solsol_white
                )
            }

            Spacer(modifier = Modifier.height(32.dp)) // 40dp에서 32dp로 조정
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

    Box(
        modifier = Modifier
            .shadow(
                elevation = if (isSelected) 16.dp else 8.dp, // 그림자 강화
                spotColor = if (isSelected) Color(0x2A8B5FBF) else Color(0x1A000000),
                ambientColor = if (isSelected) Color(0x2A8B5FBF) else Color(0x1A000000)
            )
            .fillMaxWidth()
            .height(120.dp) // 110dp에서 120dp로 증가
            .background(
                color = if (isSelected)
                    Color(0xFF8B5FBF).copy(alpha = 0.05f) // solsol_purple with very low transparency
                else
                    Color(0xFFFFFFFF), // solsol_card_white
                shape = RoundedCornerShape(20.dp) // 16dp에서 20dp로 증가
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected)
                    Color(0xFF8B5FBF) // solsol_purple
                else
                    Color(0xFFE2E8F0), // solsol_light_gray
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // 리플 효과 제거
            ) {
                Log.d(TAG, "카드 클릭됨: $title")
                onClick()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // 20dp에서 24dp로 증가
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽 색상 표시기
            Box(
                modifier = Modifier
                    .size(48.dp) // 색상 표시기 추가
                    .background(
                        color = if (isSelected)
                            Color(0xFF8B5FBF).copy(alpha = 0.15f)
                        else
                            Color(0xFF8B5FBF).copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 16.dp else 12.dp)
                        .background(
                            color = if (isSelected) Color(0xFF8B5FBF) else Color(0xFF718096),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }

            Spacer(Modifier.width(16.dp))

            // 메인 텍스트
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF8B5FBF) else Color(0xFF2D3748) // solsol_purple or solsol_dark_text
                )

                Spacer(modifier = Modifier.height(6.dp)) // 8dp에서 6dp로 조정

                Text(
                    text = description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color(0xFF8B5FBF).copy(alpha = 0.8f) else Color(0xFF718096) // solsol_purple with transparency or solsol_gray_text
                )
            }

            // 선택 표시 아이콘 - 더 트렌디하게
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = Color(0xFF8B5FBF), // solsol_purple
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "선택됨",
                        tint = Color(0xFFFFFFFF), // solsol_white
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}