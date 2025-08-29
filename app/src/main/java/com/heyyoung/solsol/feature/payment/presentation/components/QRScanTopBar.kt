// QRScanTopBar.kt
package com.heyyoung.solsol.feature.payment.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScanTopBar(
    onBackClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "QR 결제",
                color = colorResource(id = R.color.solsol_dark_text),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(8.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = colorResource(id = R.color.solsol_purple_30),
                        ambientColor = colorResource(id = R.color.solsol_purple_30)
                    )
                    .background(
                        color = colorResource(id = R.color.solsol_white),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .size(40.dp)
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "뒤로",
                    tint = colorResource(id = R.color.solsol_purple),
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colorResource(id = R.color.solsol_card_white),
            titleContentColor = colorResource(id = R.color.solsol_dark_text),
            navigationIconContentColor = colorResource(id = R.color.solsol_purple)
        ),
        modifier = Modifier.shadow(
            elevation = 4.dp,
            spotColor = colorResource(id = R.color.solsol_light_gray),
            ambientColor = colorResource(id = R.color.solsol_light_gray)
        )
    )
}