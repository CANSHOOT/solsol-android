package com.heyyoung.solsol.feature.payment.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.ui.theme.OneShinhan

data class MenuItem(
    val id: Int,
    val name: String,
    val price: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CafeMenuScreen(
    onNavigateBack: () -> Unit = {},
    onMenuSelected: (MenuItem) -> Unit = {}
) {
    var selectedMenuId by remember { mutableStateOf<Int?>(null) }

    val menuItems = listOf(
        MenuItem(1, "아메리카노", 4500),
        MenuItem(2, "카페라떼", 5100),
        MenuItem(3, "카푸치노", 5100),
        MenuItem(4, "바닐라라떼", 5300),
        MenuItem(5, "카라멜 마키아또", 5500),
        MenuItem(6, "에스프레소", 2200),
        MenuItem(7, "콜드브루", 4700),
        MenuItem(8, "아이스크림 라떼", 5500),
        MenuItem(9, "녹차라떼", 5700),
        MenuItem(10, "초콜릿라떼", 5500)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "신한카페",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            },
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

        // 스크롤 가능한 컨텐츠와 고정 버튼을 분리
        Box(modifier = Modifier.fillMaxSize()) {
            // 스크롤 가능한 본문 영역
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 120.dp), // 하단 버튼 공간 확보
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                // 카페 소개
                Box(
                    modifier = Modifier
                        .width(342.dp)
                        .background(
                            color = Color(0xFFF8F7FF),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0x338B5FBF),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "신한카페에 오신 것을 환영합니다",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7D6BB0)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "신선한 원두로 만든 프리미엄 커피를 만나보세요",
                            fontSize = 14.sp,
                            color = Color(0xFF2D3748)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 메뉴 선택 제목
                Box(Modifier.width(342.dp)) {
                    Text(
                        text = "메뉴를 선택해주세요",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 메뉴 리스트
                menuItems.forEach { menuItem ->
                    MenuItemCard(
                        menuItem = menuItem,
                        isSelected = selectedMenuId == menuItem.id,
                        onSelected = { selectedMenuId = menuItem.id }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // 추가 여백으로 스크롤 공간 확보
                Spacer(Modifier.height(60.dp))
            }

            // 하단 고정 결제 버튼 영역
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(alpha = 0.98f)
                    )
                    .padding(horizontal = 16.dp)
                    .padding(
                        top = 20.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 20.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val selectedMenu = menuItems.find { it.id == selectedMenuId }

                PaymentButton(
                    selectedMenu = selectedMenu,
                    onClick = {
                        selectedMenu?.let { menu ->
                            onMenuSelected(menu)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MenuItemCard(
    menuItem: MenuItem,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(342.dp)
            .shadow(
                elevation = if (isSelected) 8.dp else 2.dp,
                spotColor = Color(0x1A8B5FBF),
                ambientColor = Color(0x1A8B5FBF),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = if (isSelected) Color(0xFFF8F7FF) else Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF8B5FBF) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelected() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 라디오 버튼
        RadioButton(
            selected = isSelected,
            onClick = onSelected,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF8B5FBF),
                unselectedColor = Color(0xFF718096)
            )
        )

        Spacer(Modifier.width(12.dp))

        // 메뉴 정보
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = menuItem.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color(0xFF2D3748) else Color(0xFF2D3748)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = String.format("%,d원", menuItem.price),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color(0xFF8B5FBF) else Color(0xFF718096)
            )
        }

        // 선택 표시 원형 인디케이터
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = Color(0xFF8B5FBF),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        )
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = Color.Transparent,
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = Color(0xFFE2E8F0),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun PaymentButton(
    selectedMenu: MenuItem?,
    onClick: () -> Unit
) {
    val isEnabled = selectedMenu != null

    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .shadow(
                elevation = if (isEnabled) 8.dp else 2.dp,
                spotColor = Color(0x408B5FBF),
                ambientColor = Color(0x408B5FBF),
                shape = RoundedCornerShape(16.dp)
            )
            .width(342.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF8B5FBF),
            disabledContainerColor = Color(0xFF8B5FBF).copy(alpha = 0.3f),
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = selectedMenu?.let {
                "${String.format("%,d", it.price)}원 결제하기"
            } ?: "메뉴를 선택해주세요",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = OneShinhan
        )
    }
}