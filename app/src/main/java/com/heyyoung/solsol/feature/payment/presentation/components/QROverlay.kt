package com.heyyoung.solsol.feature.payment.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * QR 스캔 오버레이
 *
 * 기능:
 * - 중앙에 스캔 가이드라인 (보라색 테두리)
 * - 배경 어둡게 처리 (가이드라인 영역 제외)
 * - 스캔 애니메이션 효과
 * - 안내 텍스트
 */
@Composable
fun QROverlay(
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // 스캔 가이드라인 크기
    val scanAreaSize = with(density) { 280.dp.toPx() }
    val cornerLength = with(density) { 24.dp.toPx() }
    val cornerStrokeWidth = with(density) { 4.dp.toPx() }

    // 스캔 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "scanAnimation")
    val scanLinePosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLine"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 배경 어둡게 처리 + 가이드라인 그리기
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawQROverlay(
                scanAreaSize = scanAreaSize,
                cornerLength = cornerLength,
                cornerStrokeWidth = cornerStrokeWidth,
                scanLinePosition = scanLinePosition
            )
        }

        // 안내 텍스트
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (scanAreaSize / density.density / 2 + 60).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "QR코드를 스캔해주세요",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "가맹점 QR코드에 카메라를 맞춰주세요",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * QR 오버레이 그리기
 */
private fun DrawScope.drawQROverlay(
    scanAreaSize: Float,
    cornerLength: Float,
    cornerStrokeWidth: Float,
    scanLinePosition: Float
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val scanAreaLeft = centerX - scanAreaSize / 2
    val scanAreaTop = centerY - scanAreaSize / 2
    val scanAreaRight = centerX + scanAreaSize / 2
    val scanAreaBottom = centerY + scanAreaSize / 2

    // 1. 배경 어둡게 처리 (스캔 영역 제외)
    drawRect(
        color = Color.Black.copy(alpha = 0.5f),
        size = Size(size.width, scanAreaTop)
    )
    drawRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset(0f, scanAreaTop),
        size = Size(scanAreaLeft, scanAreaSize)
    )
    drawRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset(scanAreaRight, scanAreaTop),
        size = Size(size.width - scanAreaRight, scanAreaSize)
    )
    drawRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset(0f, scanAreaBottom),
        size = Size(size.width, size.height - scanAreaBottom)
    )

    // 2. 스캔 영역 테두리 (둥근 모서리)
    drawRoundRect(
        color = Color(0xFF8B5FBF).copy(alpha = 0.8f),
        topLeft = Offset(scanAreaLeft, scanAreaTop),
        size = Size(scanAreaSize, scanAreaSize),
        cornerRadius = CornerRadius(16.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )

    // 3. 모서리 가이드라인 (4개 모서리)
    val guideColor = Color(0xFF8B5FBF)

    // 왼쪽 위
    drawLine(
        color = guideColor,
        start = Offset(scanAreaLeft, scanAreaTop + cornerLength),
        end = Offset(scanAreaLeft, scanAreaTop),
        strokeWidth = cornerStrokeWidth
    )
    drawLine(
        color = guideColor,
        start = Offset(scanAreaLeft, scanAreaTop),
        end = Offset(scanAreaLeft + cornerLength, scanAreaTop),
        strokeWidth = cornerStrokeWidth
    )

    // 오른쪽 위
    drawLine(
        color = guideColor,
        start = Offset(scanAreaRight - cornerLength, scanAreaTop),
        end = Offset(scanAreaRight, scanAreaTop),
        strokeWidth = cornerStrokeWidth
    )
    drawLine(
        color = guideColor,
        start = Offset(scanAreaRight, scanAreaTop),
        end = Offset(scanAreaRight, scanAreaTop + cornerLength),
        strokeWidth = cornerStrokeWidth
    )

    // 왼쪽 아래
    drawLine(
        color = guideColor,
        start = Offset(scanAreaLeft, scanAreaBottom - cornerLength),
        end = Offset(scanAreaLeft, scanAreaBottom),
        strokeWidth = cornerStrokeWidth
    )
    drawLine(
        color = guideColor,
        start = Offset(scanAreaLeft, scanAreaBottom),
        end = Offset(scanAreaLeft + cornerLength, scanAreaBottom),
        strokeWidth = cornerStrokeWidth
    )

    // 오른쪽 아래
    drawLine(
        color = guideColor,
        start = Offset(scanAreaRight - cornerLength, scanAreaBottom),
        end = Offset(scanAreaRight, scanAreaBottom),
        strokeWidth = cornerStrokeWidth
    )
    drawLine(
        color = guideColor,
        start = Offset(scanAreaRight, scanAreaBottom - cornerLength),
        end = Offset(scanAreaRight, scanAreaBottom),
        strokeWidth = cornerStrokeWidth
    )

    // 4. 스캔 애니메이션 라인
    val scanLineY = scanAreaTop + (scanAreaSize * scanLinePosition)
    drawLine(
        color = Color(0xFFF093FB).copy(alpha = 0.8f),
        start = Offset(scanAreaLeft + 20, scanLineY),
        end = Offset(scanAreaRight - 20, scanLineY),
        strokeWidth = 3.dp.toPx()
    )
}