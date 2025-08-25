package com.heyyoung.solsol.feature.payment.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 하단 "사용방법" 시트
 * - 헤더는 항상 보이게 (peek)
 * - 위로 드래그 / 아래로 드래그 로 열고 닫힘
 * - 클릭으로 토글도 가능
 * - 외부와 offset(0f..1f) 공유: onOffsetChange, onExpandedChange 콜백 유지
 */
@Composable
fun UsageBottomSheet(
    offset: Float = 0f,                       // 0f(접힘) ~ 1f(펼침)
    onOffsetChange: (Float) -> Unit = {},
    onExpandedChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 화면/치수
    val density = LocalDensity.current
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val sheetMaxRatio = 0.62f                         // 펼친 높이 비율(화면의 ~62%)
    val peekHeight: Dp = 72.dp                        // 접힘 시 노출 높이

    val maxHeightPx = with(density) { (screenHeightDp * sheetMaxRatio).toPx() }
    val peekPx = with(density) { peekHeight.toPx() }

    // 내부 상태: 외부 offset과 동기화(초기값은 전달받은 offset으로)
    var target by remember { mutableStateOf(offset.coerceIn(0f, 1f)) }
    val animated by animateFloatAsState(targetValue = target, label = "usage_sheet_anim")

    // 외부에 진행도/펼침상태 알려주기
    LaunchedEffect(animated) {
        onOffsetChange(animated)
        onExpandedChange(animated > 0.1f)
    }

    // 총 높이: peek + (max - peek) * animated(0..1)
    val currentHeightPx = remember(animated, maxHeightPx, peekPx) {
        peekPx + (maxHeightPx - peekPx) * animated
    }
    val currentHeightDp = with(density) { currentHeightPx.toDp() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(currentHeightDp)                   // ← 위로 자라나는 형태
            .pointerInput(Unit) {                      // ← 드래그 제스처
                detectDragGestures(
                    onDrag = { _, drag ->
                        val dy = drag.y                // 위로 드래그: 음수, 아래로: 양수
                        val delta = -dy / (maxHeightPx - peekPx).coerceAtLeast(1f)
                        target = (target + delta).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        // 중간 값 기준 스냅
                        target = if (target > 0.3f) 1f else 0f
                    }
                )
            },
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Drag handle + 헤더
            HeaderBar(
                isExpanded = animated > 0.1f,
                onToggle = {
                    target = if (animated > 0.1f) 0f else 1f
                }
            )

            // 내용(스크롤 가능) — 진행도에 따라 페이드/노출
            if (animated > 0.01f) {
                Divider(color = Color(0xFFEAEAEA))
                UsageContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeaderBar(
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.Transparent)
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Material3 기본 드래그 핸들 느낌
        BottomSheetDefaults.DragHandle()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "사용방법",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1C1C1E),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isExpanded) "접기" else "펼치기",
                    tint = Color(0xFF8B5FBF)
                )
            }
        }
    }
}

@Composable
private fun UsageContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        StepCard(
            number = "1",
            title = "학과 제휴 자동 할인 적용",
            description = "내 학과 정보로 제휴 가맹점에서 자동으로 할인이 적용돼요"
        )
        Spacer(Modifier.height(12.dp))
        StepCard(
            number = "2",
            title = "가맹점 QR 코드 스캔",
            description = "가맹점 테이블이나 카운터의 QR 코드를 카메라에 맞춰주세요"
        )
        Spacer(Modifier.height(12.dp))
        StepCard(
            number = "3",
            title = "랜덤 쿠폰 이벤트",
            description = "결제할 때마다 500원 할인 쿠폰 당첨 기회!"
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StepCard(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // 보라색 라벨
        Surface(
            color = Color(0xFF8B5FBF),
            shape = RoundedCornerShape(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF5A5A5A),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Surface(
                color = Color(0xFFF4F1F8),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B6B6B),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
