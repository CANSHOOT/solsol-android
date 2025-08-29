package com.heyyoung.solsol.feature.auth.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.heyyoung.solsol.feature.auth.presentation.components.LoginCard
import com.heyyoung.solsol.ui.components.DeveloperQuickTest

// 로그인 화면
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val TAG = "LoginScreen"
    val uiState = viewModel.uiState

    // 입력 상태 관리
    var email by remember { mutableStateOf("") }
    var studentNumber by remember { mutableStateOf("") }

    // 화면 진입 시 로그인 상태 초기화
    LaunchedEffect(Unit) {
        Log.d(TAG, "LoginScreen 진입 - 로그인 상태 초기화")
        viewModel.resetLoginState()
    }

    // 에러 메시지 로깅
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Log.e(TAG, "로그인 에러: $error")
        }
    }

    // 개선된 레이아웃 (그라데이션 배경)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFBFC),
                        Color(0xFFF0F0F0).copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 여백 대폭 증가
            Spacer(modifier = Modifier.height(80.dp))

            // 로그인 카드
            LoginCard(
                email = email,
                onEmailChange = {
                    email = it
                    viewModel.clearError()
                },
                studentNumber = studentNumber,
                onStudentNumberChange = {
                    studentNumber = it
                    viewModel.clearError()
                },
                onLoginClick = {
                    Log.d(TAG, "로그인 시도: $email")
                    viewModel.login(email, studentNumber) { success ->
                        if (success) {
                            Log.i(TAG, "로그인 성공! 홈으로 이동")
                            onLoginSuccess()
                        }
                    }
                },
                onRegisterClick = {
                    Log.d(TAG, "회원가입 시도: $email")
                    viewModel.register(email, studentNumber) { success ->
                        if (success) {
                            Log.i(TAG, "회원가입 성공! 홈으로 이동")
                            onLoginSuccess()
                        }
                    }
                },
                isLoading = uiState.isLoading
            )

            // 에러 메시지 표시 (개선된 디자인)
            uiState.errorMessage?.let { errorMessage ->
                Spacer(modifier = Modifier.height(20.dp))
                ModernErrorCard(errorMessage)
            }

            Spacer(modifier = Modifier.weight(1f))

            // 개발용 빠른 테스트 도구 (그대로 유지)
            DeveloperQuickTest(
                onFillTestData = { testEmail, testStudentNumber ->
                    email = testEmail
                    studentNumber = testStudentNumber
                    Log.d(TAG, "테스트 데이터 자동 입력: $testEmail")
                    viewModel.clearError()
                }
            )

            // 로딩 중일 때 추가 정보 표시 (개선된 디자인)
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                ModernLoadingCard()
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 현대적인 에러 메시지 카드
 */
@Composable
private fun ModernErrorCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Red.copy(alpha = 0.15f),
                ambientColor = Color.Red.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 에러 아이콘을 좀 더 모던하게
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = Color(0xFFFFE5E5),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚠",
                    fontSize = 16.sp,
                    color = Color(0xFFE53E3E)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3748),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 현대적인 로딩 정보 카드
 */
@Composable
private fun ModernLoadingCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0xFF8B5FBF).copy(alpha = 0.15f),
                ambientColor = Color(0xFF8B5FBF).copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color(0xFF8B5FBF),
                strokeWidth = 2.5.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "서버 통신 중...",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8B5FBF)
            )
        }
    }
}