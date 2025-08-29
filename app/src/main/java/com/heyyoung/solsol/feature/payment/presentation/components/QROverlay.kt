package com.heyyoung.solsol.feature.qr // ← 패키지는 프로젝트에 맞게 변경

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 화면 전체에 어둡게 딤 처리 + 중앙 스캔 영역의 4개 모서리를
 * "부드러운 L자"로 보여주는 오버레이
 */
@Composable
fun QROverlay(
    modifier: Modifier = Modifier,
    scanBoxSize: Dp = 260.dp,      // 중앙 정사각형 스캔 영역 크기
    cornerLength: Dp = 64.dp,      // L자 길이
    strokeWidth: Dp = 6.dp,        // 선 두께
    dimAlpha: Float = 0.65f,       // 배경 딤 투명도
    guideColor: Color = Color.White
) {
    val density = LocalDensity.current
    val scanPx = with(density) { scanBoxSize.toPx() }
    val cornerPx = with(density) { cornerLength.toPx() }
    val strokePx = with(density) { strokeWidth.toPx() }

    Canvas(modifier = modifier) {
        drawCleanQROverlayRounded(
            scanAreaSize = scanPx,
            cornerSize = cornerPx,
            stroke = strokePx,
            dimAlpha = dimAlpha,
            guideColor = guideColor
        )
    }
}

/** 부드러운 L-코너 4개로 보이는 QR 오버레이 (DrawScope 확장) */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCleanQROverlayRounded(
    scanAreaSize: Float,
    cornerSize: Float,
    stroke: Float,
    dimAlpha: Float,
    guideColor: Color
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val left = centerX - scanAreaSize / 2f
    val top = centerY - scanAreaSize / 2f
    val right = centerX + scanAreaSize / 2f
    val bottom = centerY + scanAreaSize / 2f

    // 1) 바깥 배경 어둡게
    val dim = Color.Black.copy(alpha = dimAlpha)
    // 상
    drawRect(color = dim, size = Size(width = size.width, height = top))
    // 좌
    drawRect(color = dim, topLeft = Offset(0f, top), size = Size(left, scanAreaSize))
    // 우
    drawRect(color = dim, topLeft = Offset(right, top), size = Size(size.width - right, scanAreaSize))
    // 하
    drawRect(color = dim, topLeft = Offset(0f, bottom), size = Size(size.width, size.height - bottom))

    // 2) L자 코너(부드러운 곡선) — 90° 원호 + 라운드 캡
    val strokeStyle = Stroke(width = stroke, cap = StrokeCap.Round)
    val sweep = 90f

    // Top-Left (start 180° → 90° sweep)
    drawArc(
        color = guideColor,
        startAngle = 180f,
        sweepAngle = sweep,
        useCenter = false,
        style = strokeStyle,
        size = Size(cornerSize, cornerSize),
        topLeft = Offset(left, top)
    )
    // Top-Right (start 270°)
    drawArc(
        color = guideColor,
        startAngle = 270f,
        sweepAngle = sweep,
        useCenter = false,
        style = strokeStyle,
        size = Size(cornerSize, cornerSize),
        topLeft = Offset(right - cornerSize, top)
    )
    // Bottom-Right (start 0°)
    drawArc(
        color = guideColor,
        startAngle = 0f,
        sweepAngle = sweep,
        useCenter = false,
        style = strokeStyle,
        size = Size(cornerSize, cornerSize),
        topLeft = Offset(right - cornerSize, bottom - cornerSize)
    )
    // Bottom-Left (start 90°)
    drawArc(
        color = guideColor,
        startAngle = 90f,
        sweepAngle = sweep,
        useCenter = false,
        style = strokeStyle,
        size = Size(cornerSize, cornerSize),
        topLeft = Offset(left, bottom - cornerSize)
    )
}
