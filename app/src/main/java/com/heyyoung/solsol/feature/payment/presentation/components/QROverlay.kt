package com.heyyoung.solsol.feature.payment.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.R

/**
 * QR 스캔 오버레이
 *
 * 기능:
 * - 중앙에 스캔 가이드라인 (보라색 테두리)
 * - 배경 어둡게 처리 (가이드라인 영역 제외)
 * - 안내 텍스트
 */
@Composable
fun QROverlay(
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // 스캔 가이드라인 크기
    val scanAreaSize = with(density) { 280.dp.toPx() }
    val cornerLength = with(density) { 32.dp.toPx() }
    val cornerStrokeWidth = with(density) { 8.dp.toPx() }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 배경 어둡게 처리 + 가이드라인 그리기
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawCleanQROverlay(
                scanAreaSize = scanAreaSize,
                cornerLength = cornerLength,
                cornerStrokeWidth = cornerStrokeWidth
            )
        }

        // 깔끔한 안내 텍스트
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (scanAreaSize / density.density / 2 + 80).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 메인 안내 카드
            Card(
                modifier = Modifier
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = colorResource(id = R.color.solsol_purple_30),
                        ambientColor = colorResource(id = R.color.solsol_purple_30)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(id = R.color.solsol_white_50)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QR코드를 스캔해주세요",
                        color = colorResource(id = R.color.solsol_white),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "가맹점 QR코드에 카메라를 맞춰주세요",
                        color = colorResource(id = R.color.solsol_white),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 깔끔한 QR 오버레이 그리기
 */
private fun DrawScope.drawCleanQROverlay(
    scanAreaSize: Float,
    cornerLength: Float,
    cornerStrokeWidth: Float
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val scanAreaLeft = centerX - scanAreaSize / 2
    val scanAreaTop = centerY - scanAreaSize / 2
    val scanAreaRight = centerX + scanAreaSize / 2
    val scanAreaBottom = centerY + scanAreaSize / 2

    // 1. 배경 어둡게 처리 (스캔 영역 제외)
    drawRect(
        color = Color.Black.copy(alpha = 0.65f),
        size = Size(size.width, scanAreaTop)
    )
    drawRect(
        color = Color.Black.copy(alpha = 0.65f),
        topLeft = Offset(0f, scanAreaTop),
        size = Size(scanAreaLeft, scanAreaSize)
    )
    drawRect(
        color = Color.Black.copy(alpha = 0.65f),
        topLeft = Offset(scanAreaRight, scanAreaTop),
        size = Size(size.width - scanAreaRight, scanAreaSize)
    )
    drawRect(
        color = Color.Black.copy(alpha = 0.65f),
        topLeft = Offset(0f, scanAreaBottom),
        size = Size(size.width, size.height - scanAreaBottom)
    )

    // 2. 스캔 영역 테두리 (깔끔한 둥근 모서리)
    drawRoundRect(
        color = Color(0xFF8B5FBF).copy(alpha = 0.4f),
        topLeft = Offset(scanAreaLeft, scanAreaTop),
        size = Size(scanAreaSize, scanAreaSize),
        cornerRadius = CornerRadius(24.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )

    // 3. 모서리 가이드라인 (깔끔하고 현대적으로)
    val guideColor = Color(0xFF8B5FBF)
    val cornerRadius = 24.dp.toPx()

    // 왼쪽 위
    drawLine(
        color = guideColor,
        start = Offset(scanAreaLeft, scanAreaTop + cornerLength),
        end = Offset(scanAreaLeft, scanAreaTop + cornerRadius),
        strokeWidth = cornerStrokeWidth
    )
    drawLine(
        color = guideColor,
        start = Offset(scanAreaLeft + cornerRadius, scanAreaTop),
        end = Offset(scanAreaLeft + cornerLength, scanAreaTop),
        strokeWidth = cornerStrokeWidth
    )

    // 오른쪽 위
    drawLine(
        color = guideColor,
        start = Offset(scanAreaRight - cornerLength, scanAreaTop),
        end = Offset(scanAreaRight - cornerRadius, scanAreaTop),
        strokeWidth = cornerStrokeWidth
    )
    drawLine(
        color = guideColor,
        start = Offset(scanAreaRight, scanAreaTop + cornerRadius),
        end = Offset(scanAreaRight, scanAreaTop + cornerLength),
        strokeWidth = cornerStrokeWidth
    )

    // 왼쪽 아래
    drawLine(
        color = guideColor,
        start = Offset(scanAreaLeft, scanAreaBottom - cornerLength),
        end = Offset(scanAreaLeft, scanAreaBottom - cornerRadius),
        strokeWidth = cornerStrokeWidth
    )
    drawLine(
        color = guideColor,
        start = Offset(scanAreaLeft + cornerRadius, scanAreaBottom),
        end = Offset(scanAreaLeft + cornerLength, scanAreaBottom),
        strokeWidth = cornerStrokeWidth
    )

    // 오른쪽 아래
    drawLine(
        color = guideColor,
        start = Offset(scanAreaRight - cornerLength, scanAreaBottom),
        end = Offset(scanAreaRight - cornerRadius, scanAreaBottom),
        strokeWidth = cornerStrokeWidth
    )
    drawLine(
        color = guideColor,
        start = Offset(scanAreaRight, scanAreaBottom - cornerLength),
        end = Offset(scanAreaRight, scanAreaBottom - cornerRadius),
        strokeWidth = cornerStrokeWidth
    )
}