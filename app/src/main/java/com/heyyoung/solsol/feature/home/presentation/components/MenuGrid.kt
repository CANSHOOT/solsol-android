package com.heyyoung.solsol.feature.home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
 * 메뉴 그리드 컴포넌트 (현대적 인터랙션 효과 적용)
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
        ModernMenuTitle()

        Spacer(modifier = Modifier.height(24.dp))

        // 메뉴 그리드
        InteractiveMenuItemGrid(
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
 * 현대적인 메뉴 제목
 */
@Composable
private fun ModernMenuTitle() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "바로가기메뉴",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 데코레이션 라인
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}

/**
 * 인터랙티브 메뉴 아이템 그리드
 */
@Composable
private fun InteractiveMenuItemGrid(
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
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(menuItems) { item ->
            InteractiveMenuItemCard(
                title = item.title,
                iconRes = item.iconRes,
                onClick = item.onClick
            )
        }
    }
}

/**
 * 인터랙티브 메뉴 아이템 카드
 */
@Composable
private fun InteractiveMenuItemCard(
    title: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(if (isPressed) 0.95f else 1f)
            .alpha(if (isPressed) 0.8f else 1f)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        // 아이콘 컨테이너
        ModernIconContainer(
            iconRes = iconRes,
            title = title,
            isPressed = isPressed
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 텍스트 라벨
        ModernMenuLabel(
            text = title,
            isPressed = isPressed
        )
    }
}


@Composable
private fun ModernIconContainer(
    iconRes: Int,
    title: String,
    isPressed: Boolean
) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .shadow(
                elevation = if (isPressed) 4.dp else 12.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color.White.copy(alpha = 0.4f),
                ambientColor = Color.White.copy(alpha = 0.2f)
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isPressed) 0.25f else 0.35f),
                        Color.White.copy(alpha = if (isPressed) 0.15f else 0.25f),
                        Color.White.copy(alpha = if (isPressed) 0.1f else 0.2f)
                    ),
                    radius = 120f
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(22.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(if (isPressed) 68.dp else 72.dp)
        )
    }
}

@Composable
private fun ModernMenuLabel(
    text: String,
    isPressed: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = if (isPressed) 0.8f else 1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    spotColor = Color.Black.copy(alpha = 0.3f),
                    ambientColor = Color.Black.copy(alpha = 0.2f)
                )
        )
    }
}

/**
 * 메뉴 아이템 데이터 클래스
 */
private data class MenuItemData(
    val title: String,
    val iconRes: Int,
    val onClick: () -> Unit
)