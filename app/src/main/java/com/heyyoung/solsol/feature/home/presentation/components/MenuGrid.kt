package com.heyyoung.solsol.feature.home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.R
import com.heyyoung.solsol.ui.components.SolsolSmallCard

/**
 * 메뉴 그리드 컴포넌트 (커스텀 이미지 사용)
 * 3x2 격자로 메뉴 아이템들 표시
 */
@Composable
fun MenuGrid(
    onPaymentClick: () -> Unit = {},
    onSettlementClick: () -> Unit = {},
    onSettlementManagementClick: () -> Unit = {},
    onMoneyTransferClick: () -> Unit = {},
    onStudentCouncilClick: () -> Unit = {},
    onCouponsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // 메뉴 제목
        MenuTitle()

        Spacer(modifier = Modifier.height(20.dp))

        // 메뉴 그리드
        MenuItemGrid(
            onPaymentClick = onPaymentClick,
            onSettlementClick = onSettlementClick,
            onSettlementManagementClick = onSettlementManagementClick,
            onMoneyTransferClick = onMoneyTransferClick,
            onStudentCouncilClick = onStudentCouncilClick,
            onCouponsClick = onCouponsClick
        )
    }
}

/**
 * 메뉴 제목
 */
@Composable
private fun MenuTitle() {
    Text(
        text = "바로가기메뉴",
        modifier = Modifier.padding(horizontal = 30.dp),
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
}

/**
 * 메뉴 아이템 그리드 (이미지 사용)
 */
@Composable
private fun MenuItemGrid(
    onPaymentClick: () -> Unit,
    onSettlementClick: () -> Unit,
    onSettlementManagementClick: () -> Unit,
    onMoneyTransferClick: () -> Unit,
    onStudentCouncilClick: () -> Unit,
    onCouponsClick: () -> Unit
) {
    val menuItems = listOf(
        MenuItemData("결제", R.drawable.ic_payment, onPaymentClick),
        MenuItemData("내역조회", R.drawable.ic_history, onSettlementClick),
        MenuItemData("정산요청", R.drawable.ic_settlement, onSettlementManagementClick),
        MenuItemData("송금하기", R.drawable.ic_transfer, onMoneyTransferClick),
        MenuItemData("학생회", R.drawable.ic_council, onStudentCouncilClick),
        MenuItemData("쿠폰", R.drawable.ic_coupon, onCouponsClick)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(menuItems) { item ->
            NewStyleMenuItemCard(
                title = item.title,
                iconRes = item.iconRes,
                onClick = item.onClick
            )
        }
    }
}

/**
 * 개별 메뉴 아이템 카드 (이미지 사용)
 */
@Composable
private fun NewStyleMenuItemCard(
    title: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        // 아이콘 컨테이너
        Box(
            modifier = Modifier
                .size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(70.dp) // 아이콘 크기 증가
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 텍스트 라벨
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp) // 라벨 영역 고정 높이 (원하면 24~26dp로 조정)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,              // 통일 권장 (원하면 16.sp)
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = Color.White,
                maxLines = 1,                  // ✅ 한 줄로 고정
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * 메뉴 아이템 데이터 클래스 (이미지용)
 */
private data class MenuItemData(
    val title: String,
    val iconRes: Int,
    val onClick: () -> Unit
)