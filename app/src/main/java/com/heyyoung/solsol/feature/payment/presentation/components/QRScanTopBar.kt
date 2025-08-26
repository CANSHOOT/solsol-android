// QRScanTopBar.kt
package com.heyyoung.solsol.feature.payment.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScanTopBar(
    onBackClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text("QR 결제") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,            // ← 흰 배경
            titleContentColor = Color(0xFF1C1C1E),
            navigationIconContentColor = Color(0xFF1C1C1E)
        )
    )
}
