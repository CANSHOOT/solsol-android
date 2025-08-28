package com.heyyoung.solsol.feature.auth.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

    // 흰색 배경 레이아웃
    LoginScreenLayout(
        modifier = modifier
    ) {
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

        // 에러 메시지 표시
        uiState.errorMessage?.let { errorMessage ->
            Spacer(modifier = Modifier.height(16.dp))
            ErrorMessageCard(errorMessage)
        }

        // 개발용 빠른 테스트 도구 (그대로 유지)
        DeveloperQuickTest(
            onFillTestData = { testEmail, testStudentNumber ->
                email = testEmail
                studentNumber = testStudentNumber
                Log.d(TAG, "테스트 데이터 자동 입력: $testEmail")
                viewModel.clearError()
            }
        )

        // 로딩 중일 때 추가 정보 표시
        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            LoadingInfoCard()
        }
    }
}

/**
 * 로그인 화면 레이아웃 (흰색 배경)
 */
@Composable
private fun LoginScreenLayout(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x0D000000),
                ambientColor = Color(0x0D000000)
            )
            .background(color = Color(0xFFFAFBFC)), // 흰색 배경
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        content()

        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * 에러 메시지 카드
 */
@Composable
private fun ErrorMessageCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF).copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "❌",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = message,
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 로딩 정보 카드
 */
@Composable
private fun LoadingInfoCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF).copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Color(0xFF8B5FBF),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "서버 통신 중...",
                color = Color(0xFF8B5FBF)
            )
        }
    }
}