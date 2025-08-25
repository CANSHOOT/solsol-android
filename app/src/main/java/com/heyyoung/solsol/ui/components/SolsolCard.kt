package com.heyyoung.solsol.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.heyyoung.solsol.ui.components.modifiers.solsolMainCard
import com.heyyoung.solsol.ui.components.modifiers.solsolSmallCard
import com.heyyoung.solsol.ui.components.modifiers.solsolTransparentCard

// 메인 카드 컴포넌트

@Composable
fun SolsolMainCard(
    modifier: Modifier = Modifier,
    width: Dp = 342.dp,
    height: Dp = 250.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.solsolMainCard(width = width, height = height)
    ) {
        content()
    }
}

// 작은 카드 컴포넌트

@Composable
fun SolsolSmallCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cardModifier = modifier.solsolSmallCard()

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    } else {
        Box(modifier = cardModifier) {
            content()
        }
    }
}

// 투명 카드 컴포넌트

@Composable
fun SolsolTransparentCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.solsolTransparentCard()
    ) {
        content()
    }
}