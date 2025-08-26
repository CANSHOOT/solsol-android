package com.heyyoung.solsol.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.R

@Composable
fun SolsolTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    width: Dp = 342.dp,
    height: Dp = 52.dp,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    isFocused: Boolean = false
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = colorResource(id = R.color.solsol_purple_30),
                ambientColor = colorResource(id = R.color.solsol_purple_30)
            )
            .border(
                width = 1.dp,
                color = if (isFocused) {
                    colorResource(id = R.color.solsol_purple)
                } else {
                    colorResource(id = R.color.solsol_light_gray)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = colorResource(id = R.color.solsol_white),
                shape = RoundedCornerShape(12.dp)
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
                color = colorResource(id = R.color.solsol_dark_text),
                fontWeight = FontWeight.Normal
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            singleLine = singleLine,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = colorResource(id = R.color.solsol_gray_text),
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}


// 이메일 입력 필드

@Composable
fun SolsolEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "이메일을 입력하세요"
) {
    var isFocused by remember { mutableStateOf(false) }

    SolsolTextField(
        value = value,
        onValueChange = { newValue ->
            // 이메일 형식 기본 검증 (공백 제거)
            val cleanValue = newValue.trim()
            onValueChange(cleanValue)
        },
        modifier = modifier,
        placeholder = placeholder,
        keyboardType = KeyboardType.Email,
        isFocused = isFocused || value.isNotEmpty()
    )
}

// 학번 비밀번호 입력 필드

@Composable
fun SolsolStudentNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "학번을 입력하세요"
) {
    var isFocused by remember { mutableStateOf(false) }

    SolsolTextField(
        value = value,
        onValueChange = { newValue ->
            // 숫자만 입력 가능하고 최대 8자리
            if (newValue.all { it.isDigit() } && newValue.length <= 8) {
                onValueChange(newValue)
            }
        },
        modifier = modifier,
        placeholder = placeholder,
        keyboardType = KeyboardType.Number,
        isPassword = false, // 마스킹 없음
        isFocused = isFocused || value.isNotEmpty()
    )
}





