package com.heyyoung.solsol.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 개발용 빠른 테스트 도구 (흰색 배경에 맞게 수정)
 * 해커톤 개발 시 빠른 테스트를 위한 더미 데이터 입력 버튼들
 */
@Composable
fun DeveloperQuickTest(
    onFillTestData: (email: String, studentNumber: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5) // 연한 회색 배경
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "개발용 빠른 테스트",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 테스트 사용자 1
                DevTestButton(
                    text = "테스트1",
                    onClick = {
                        onFillTestData("test1@ssafy.com", "20251234")
                    },
                    modifier = Modifier.weight(1f)
                )

                // 테스트 사용자 2
                DevTestButton(
                    text = "테스트2",
                    onClick = {
                        onFillTestData("student@heyyoung.ac.kr", "20251111")
                    },
                    modifier = Modifier.weight(1f)
                )

                // 테스트 사용자 3
                DevTestButton(
                    text = "데모",
                    onClick = {
                        onFillTestData("demo@solsol.com", "20259999")
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "버튼을 누르면 자동으로 입력됩니다",
                fontSize = 10.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

/**
 * 개발용 테스트 버튼 (흰색 배경에 맞게 수정)
 */
@Composable
private fun DevTestButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE1BEE7) // 연한 보라색
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9C27B0) // 진한 보라색
        )
    }
}

/**
 * 개발용 로그 표시 컴포넌트 (흰색 배경에 맞게 수정)
 */
@Composable
fun DeveloperLog(
    logs: List<String>,
    modifier: Modifier = Modifier
) {
    if (logs.isNotEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF333333).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "🔍 개발 로그",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                logs.takeLast(5).forEach { log ->
                    Text(
                        text = log,
                        color = Color(0xFF4ADE80), // 초록색
                        fontSize = 8.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}

/**
 * 개발용 상태 표시 (흰색 배경에 맞게 수정)
 */
@Composable
fun DeveloperStatus(
    currentScreen: String,
    isLoggedIn: Boolean,
    userEmail: String = "",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📱 $currentScreen",
                fontSize = 10.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )

            Text(
                text = if (isLoggedIn) "✅ $userEmail" else "❌ 로그아웃",
                fontSize = 10.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
        }
    }
}