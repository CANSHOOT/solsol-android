package com.heyyoung.solsol.feature.home.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.heyyoung.solsol.R
import com.heyyoung.solsol.feature.home.HomeViewModel
import com.heyyoung.solsol.feature.home.presentation.components.MenuGrid
import com.heyyoung.solsol.feature.home.presentation.components.PagerDots
import com.heyyoung.solsol.feature.home.presentation.components.StudentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToQrScan: () -> Unit = {},
    onNavigateToPaymentHistory: () -> Unit = {},
    onNavigateToSettlement: () -> Unit = {},
    onNavigateToCouncil: () -> Unit = {},
    onNavigateToMoneyTransfer: () -> Unit = {},
    onNavigateToCoupon: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val TAG = "HomeScreen"

    val studentName by viewModel.studentName.collectAsState()
    val studentNumber by viewModel.studentNumber.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    // 홈 화면 진입 로그
    LaunchedEffect(Unit) {
        Log.d(TAG, "홈 화면 진입")
    }

    // 깔끔한 그라데이션 배경
    HomeScreenLayout(
        modifier = modifier,
        onLogout = { viewModel.logout(onLogout) }
    ) {
        // 학생 정보 카드
        StudentCard(
            studentName = studentName ?: if (isLoading) "불러오는 중..." else "이름 없음",
            studentNumber = studentNumber ?: if (isLoading) "불러오는 중..." else "학번 없음",
            department = "컴퓨터공학과",
            grade = "재학생1학년",
            onQrClick = {
                Log.d(TAG, "QR 스캔 버튼 클릭")
                onNavigateToQrScan()
            },
            onBtClick = {
                Log.d(TAG, "BT 버튼 클릭")
            }
        )

        Spacer(modifier = Modifier.height(30.dp))
        PagerDots(total = 3, selectedIndex = 0)
        Spacer(Modifier.height(20.dp))

        // 바로가기 메뉴 그리드
        MenuGrid(
            onPaymentClick = {
                Log.d(TAG, "결제 메뉴 클릭")
                onNavigateToQrScan()
            },
            onSettlementClick = {
                Log.d(TAG, "내역조회 메뉴 클릭")
                onNavigateToPaymentHistory()
            },
            onSettlementManagementClick = {
                Log.d(TAG, "정산요청 메뉴 클릭")
                onNavigateToSettlement()
            },
            onMoneyTransferClick = {
                Log.d(TAG, "송금하기 메뉴 클릭")
                onNavigateToMoneyTransfer()
            },
            onStudentCouncilClick = {
                Log.d(TAG, "학생회 메뉴 클릭")
                onNavigateToCouncil()
            },
            onCouponsClick = {
                Log.d(TAG, "쿠폰 메뉴 클릭")
                onNavigateToCoupon()
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // 하단 네비게이션
        HomeBottomNavigation()
    }
}

/**
 * 홈 화면 레이아웃 (살짝 개선된 그라데이션)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenLayout(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorResource(id = R.color.solsol_gradient_start),
                        colorResource(id = R.color.solsol_gradient_end)
                    )
                )
            )
    ) {
        // 상단 앱바
        HomeTopAppBar(onLogout = onLogout)

        Spacer(modifier = Modifier.height(24.dp))

        // 메인 콘텐츠
        content()
    }
}

/**
 * 상단 앱바
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(
    onLogout: () -> Unit = {}
) {
    val TAG = "HomeTopAppBar"

    TopAppBar(
        title = {
            Text(
                text = "solsolheyoung",
                color = colorResource(id = R.color.solsol_white),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        actions = {
            IconButton(onClick = {
                Log.d(TAG, "알림 버튼 클릭")
            }) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "알림",
                    tint = colorResource(id = R.color.solsol_white),
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = {
                Log.i(TAG, "로그아웃 버튼 클릭")
                onLogout()
            }) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = "로그아웃",
                    tint = colorResource(id = R.color.solsol_white),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}

/**
 * 하단 네비게이션 (기존과 동일)
 */
@Composable
private fun HomeBottomNavigation() {
    val TAG = "HomeBottomNav"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.solsol_white)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "학사",
                selected = true
            ) {
                Log.d(TAG, "학사 탭 클릭")
            }

            BottomNavItem(
                icon = Icons.Default.Menu,
                label = "전체메뉴",
                selected = false
            ) {
                Log.d(TAG, "전체메뉴 탭 클릭")
            }
        }
    }
}

/**
 * 하단 네비게이션 아이템 (기존과 동일)
 */
@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) {
                colorResource(id = R.color.solsol_purple)
            } else {
                colorResource(id = R.color.solsol_gray_text)
            },
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected) {
                colorResource(id = R.color.solsol_purple)
            } else {
                colorResource(id = R.color.solsol_gray_text)
            }
        )
    }
}