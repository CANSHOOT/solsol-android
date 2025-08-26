package com.heyyoung.solsol.ui.components.modifiers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

fun Modifier.solsolMainCard(
    width: Dp = 342.dp,
    height: Dp = 250.dp
): Modifier = this
    .shadow(
        elevation = 16.dp,
        spotColor = Color(0x26000000),
        ambientColor = Color(0x26000000)
    )
    .width(width)
    .height(height)
    .background(
        color = Color(0xFFFFFFFF),
        shape = RoundedCornerShape(size = 15.dp)
    )

fun Modifier.solsolInputField(
    width: Dp = 342.dp,
    height: Dp = 52.dp
): Modifier = this
    .shadow(
        elevation = 8.dp,
        spotColor = Color(0x0D000000),
        ambientColor = Color(0x0D000000)
    )
    .border(
        width = 1.dp,
        color = Color(0xFFE2E8F0),
        shape = RoundedCornerShape(size = 12.dp)
    )
    .padding(0.5.dp)
    .width(width)
    .height(height)
    .background(
        color = Color(0xFFFFFFFF),
        shape = RoundedCornerShape(size = 12.dp)
    )

fun Modifier.solsolButton(
    width: Dp = 342.dp,
    height: Dp = 52.dp
): Modifier = this
    .shadow(
        elevation = 4.dp,
        spotColor = Color(0x1A9C27B0),
        ambientColor = Color(0x1A9C27B0)
    )
    .width(width)
    .height(height)
    .background(
        color = Color(0xFF9C27B0),
        shape = RoundedCornerShape(size = 12.dp)
    )

fun Modifier.solsolSmallCard(): Modifier = this
    .shadow(
        elevation = 4.dp,
        spotColor = Color(0x1A000000),
        ambientColor = Color(0x1A000000)
    )
    .background(
        color = Color(0xFFFFFFFF),
        shape = RoundedCornerShape(size = 12.dp)
    )

fun Modifier.solsolTransparentCard(): Modifier = this
    .background(
        color = Color.White.copy(alpha = 0.9f),
        shape = RoundedCornerShape(size = 16.dp)
    )

fun Modifier.solsolProfileImage(size: Dp = 50.dp): Modifier = this
    .size(size)
    .background(
        color = Color(0xFFCCCCCC),
        shape = androidx.compose.foundation.shape.CircleShape
    )
fun Modifier.solsolGradientBackground(
    startColor: Color = Color(0xFF8B5FBF),
    endColor: Color = Color(0xFFF093FB),
    angleInDegrees: Float = 70f,
    alpha: Float = 1f
): Modifier = this.then(
    Modifier.drawBehind {
        val angleRad = Math.toRadians(angleInDegrees.toDouble())
        val direction = Offset(cos(angleRad).toFloat(), sin(angleRad).toFloat())
        val center = Offset(size.width / 2f, size.height / 2f)
        val halfDiagonal = (hypot(size.width.toDouble(), size.height.toDouble()) / 2f).toFloat()

        val start = center - direction * halfDiagonal
        val end = center + direction * halfDiagonal

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    startColor.copy(alpha = startColor.alpha * alpha),
                    endColor.copy(alpha = endColor.alpha * alpha)
                ),
                start = start,
                end = end
            )
        )
    }
)