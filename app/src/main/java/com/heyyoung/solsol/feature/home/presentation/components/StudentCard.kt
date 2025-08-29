package com.heyyoung.solsol.feature.home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.R

// 모바일 학생증 형태 (글래스모피즘 적용)
@Composable
fun StudentCard(
    studentName: String = "김신한",
    studentNumber: String = "20251234",
    department: String = "컴퓨터공학과",
    grade: String = "재학생1학년",
    onQrClick: () -> Unit = {},
    onBtClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // 글래스모피즘 카드
        GlassmorphismCard(
            modifier = Modifier
                .width(350.dp)
                .height(260.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 헤더 텍스트
                ModernCardHeader()

                Spacer(modifier = Modifier.height(20.dp))

                // 학생 정보 섹션
                ModernStudentInfo(
                    studentName = studentName,
                    studentNumber = studentNumber,
                    department = department,
                    grade = grade
                )

                Spacer(modifier = Modifier.weight(1f))

                // QR/BT 버튼 바
                ModernCardButtons(
                    onQrClick = onQrClick,
                    onBtClick = onBtClick
                )
            }
        }
    }
}

/**
 * 글래스모피즘 카드 컨테이너
 */
@Composable
private fun GlassmorphismCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.White.copy(alpha = 0.3f),
                ambientColor = Color.White.copy(alpha = 0.15f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        content()
    }
}

@Composable
private fun ModernCardHeader() {
    Text(
        text = "모바일 학생증",
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Color.White.copy(alpha = 0.9f)
    )
}

@Composable
private fun ModernStudentInfo(
    studentName: String,
    studentNumber: String,
    department: String,
    grade: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 현대적인 프로필 이미지
        ModernProfileImage()

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$department / $grade",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$studentName ($studentNumber)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ModernProfileImage() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                spotColor = Color.White.copy(alpha = 0.4f),
                ambientColor = Color.White.copy(alpha = 0.2f)
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                shape = CircleShape
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = "프로필",
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun ModernCardButtons(
    onQrClick: () -> Unit,
    onBtClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .width(300.dp)
                .height(60.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(30.dp),
                    spotColor = Color.White.copy(alpha = 0.3f),
                    ambientColor = Color.White.copy(alpha = 0.15f)
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // QR 버튼
            ModernActionButton(
                iconRes = R.drawable.qr1,
                label = "QR",
                onClick = onQrClick,
                modifier = Modifier.weight(1f)
            )

            // 구분선
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(Color.White.copy(alpha = 0.3f))
            )

            // BT 버튼
            ModernActionButton(
                iconRes = R.drawable.bt1,
                label = "BT",
                onClick = onBtClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModernActionButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "$label 결제",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PagerDots(
    total: Int = 3,
    selectedIndex: Int = 0,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.4f),
    size: Dp = 10.dp,
    spacing: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .size(if (i == selectedIndex) size + 2.dp else size)
                    .shadow(
                        elevation = if (i == selectedIndex) 6.dp else 2.dp,
                        shape = CircleShape,
                        spotColor = Color.White.copy(alpha = 0.5f)
                    )
                    .clip(CircleShape)
                    .background(
                        if (i == selectedIndex) {
                            Brush.radialGradient(
                                colors = listOf(
                                    activeColor,
                                    activeColor.copy(alpha = 0.8f)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    inactiveColor,
                                    inactiveColor.copy(alpha = 0.6f)
                                )
                            )
                        }
                    )
            )
            if (i < total - 1) Spacer(Modifier.width(spacing))
        }
    }
}