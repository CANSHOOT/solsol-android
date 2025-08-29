package com.heyyoung.solsol.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.R

val OneShinhan = FontFamily(
    Font(R.font.one_shinhan_light, FontWeight.Light),
    Font(R.font.one_shinhan_medium, FontWeight.Medium),
    Font(R.font.one_shinhan_bold, FontWeight.Bold)
)

val Typography = androidx.compose.material3.Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, // 기본 시스템 폰트
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = OneShinhan, // 신한은행 폰트
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    )
)


