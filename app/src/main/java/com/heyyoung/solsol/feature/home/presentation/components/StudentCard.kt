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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.R

// 모바일 학생증 형태
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
    // 가운데 정렬 + 정확한 사이즈/그림자/배경 적용
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    spotColor = Color(0x26000000),
                    ambientColor = Color(0x26000000),
                    shape = RoundedCornerShape(15.dp)
                )
                .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(15.dp))
                .width(342.dp)
                .height(250.dp) // ← 세로 길이 확대
                .padding(20.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                // 헤더 텍스트
                StudentCardHeader()

                Spacer(modifier = Modifier.height(16.dp))

                // 학생 정보 섹션
                StudentInfo(
                    studentName = studentName,
                    studentNumber = studentNumber,
                    department = department,
                    grade = grade
                )

                Spacer(modifier = Modifier.weight(1f))

                // QR/BT 버튼 바 (피그마 스타일)
                StudentCardButtons(
                    onQrClick = onQrClick,
                    onBtClick = onBtClick
                )
            }
        }
    }
}

@Composable
private fun StudentCardHeader() {
    Text(
        text = "모바일 학생증",
        fontSize = 12.sp,
        color = Color(0xFF666666)
    )
}

@Composable
private fun StudentInfo(
    studentName: String,
    studentNumber: String,
    department: String,
    grade: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // 프로필 이미지 (플레이스홀더)
        StudentProfileImage()

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "$department / $grade",
                fontSize = 12.sp,
                color = colorResource(id = R.color.solsol_text_gray)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$studentName ($studentNumber)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun StudentProfileImage() {
    Box(
        modifier = Modifier
            .padding(1.dp)
            .size(75.dp)
            .background(color = Color(0xFFF0F0F0), shape = CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = "프로필",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun StudentCardButtons(
    onQrClick: () -> Unit,
    onBtClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .width(280.dp)
                .height(55.dp)
                .background(color = Color(0x4D8B5FBF), shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // QR
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onQrClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.qr1),
                        contentDescription = "QR 결제",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("QR", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            // 구분선
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.35f))
            )

            // BT
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onBtClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.bt1),
                        contentDescription = "BT 결제",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("BT", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PagerDots(
    total: Int = 3,
    selectedIndex: Int = 0,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.35f),
    size: Dp = 8.dp,
    spacing: Dp = 6.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(if (i == selectedIndex) activeColor else inactiveColor)
            )
            if (i < total - 1) Spacer(Modifier.width(spacing))
        }
    }
}
