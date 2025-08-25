package com.heyyoung.solsol.feature.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.R

@Composable
fun LoginCard(
    email: String,
    onEmailChange: (String) -> Unit,
    studentNumber: String,
    onStudentNumberChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 로고 섹션
        SolsolLogoImage()

        Spacer(modifier = Modifier.height(60.dp))

        // 입력 필드들
        LoginInputFields(
            email = email,
            onEmailChange = onEmailChange,
            studentNumber = studentNumber,
            onStudentNumberChange = onStudentNumberChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 버튼들
        LoginButtons(
            onLoginClick = onLoginClick,
            onRegisterClick = onRegisterClick,
            isLoginEnabled = email.isNotBlank() && studentNumber.isNotBlank(),
            isLoading = isLoading
        )
    }
}

/**
 * 로고 이미지
 */
@Composable
private fun SolsolLogoImage(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.logo_image),
        contentDescription = "쏠쏠해영 로고",
        modifier = modifier
            .size(100.dp)
            .clip(RoundedCornerShape(20.dp))
    )
}

/**
 * 입력 필드들 (새로운 스타일 적용)
 */
@Composable
private fun LoginInputFields(
    email: String,
    onEmailChange: (String) -> Unit,
    studentNumber: String,
    onStudentNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 이메일 입력 필드
        UpdatedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "이메일을 입력하세요",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 학번 입력 필드
        UpdatedTextField(
            value = studentNumber,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() } && newValue.length <= 8) {
                    onStudentNumberChange(newValue)
                }
            },
            placeholder = "학번을 입력하세요",
            keyboardType = KeyboardType.Number,
            isPassword = true
        )
    }
}

/**
 * 새로운 스타일의 텍스트 필드
 */
@Composable
private fun UpdatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
            .width(342.dp)
            .height(52.dp)
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(size = 12.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Normal
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                androidx.compose.ui.text.input.VisualTransformation.None
            },
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF999999),
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

/**
 * 버튼들 (새로운 스타일 적용)
 */
@Composable
private fun LoginButtons(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    isLoginEnabled: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 로그인 버튼
        UpdatedLoginButton(
            text = "로그인하기",
            onClick = onLoginClick,
            enabled = isLoginEnabled,
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 회원가입 버튼 (새로운 스타일)
        UpdatedRegisterButton(
            onClick = onRegisterClick
        )
    }
}

/**
 * 새로운 스타일의 로그인 버튼
 */
@Composable
private fun UpdatedLoginButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .alpha(0.9f)
            .width(342.dp)
            .height(56.dp)
            .background(
                brush = if (enabled) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8B5FBF).copy(alpha = 0.8f),
                            Color(0xFFF093FB)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8B5FBF).copy(alpha = 0.4f),
                            Color(0xFFF093FB).copy(alpha = 0.4f)
                        )
                    )
                },
                shape = RoundedCornerShape(size = 12.dp)
            )
            .clickable(enabled = enabled && !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 새로운 스타일의 회원가입 버튼
 */
@Composable
private fun UpdatedRegisterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = "계정이 없으신가요? 회원가입",
        style = TextStyle(
            fontSize = 14.sp,
            // fontFamily = FontFamily(Font(R.font.nanum_gothic)), // 폰트 파일이 있을 때 사용
            fontWeight = FontWeight(400),
            color = Color(0x96718096),
            letterSpacing = 2.1.sp,
            textDecoration = TextDecoration.Underline,
        ),
        modifier = modifier.clickable { onClick() }
    )
}