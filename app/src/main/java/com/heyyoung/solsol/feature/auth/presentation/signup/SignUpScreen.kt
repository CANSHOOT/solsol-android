package com.heyyoung.solsol.feature.auth.presentation.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heyyoung.solsol.ui.theme.SolsolPrimary

/**
 * 회원가입 화면
 * - 이메일, 이름, 학번, 학과 입력
 * - 학생회 임원 여부 선택
 * - 입력값 검증 및 중복 확인
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onSignUpSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(uiState.isSignUpSuccess) {
        if (uiState.isSignUpSuccess) {
            onSignUpSuccess()
        }
    }
    
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("회원가입") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SolsolPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "계정 정보를 입력해주세요",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 이메일 입력
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("이메일") },
                placeholder = { Text("solsol@ssafy.co.kr") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 이름 입력
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChanged,
                label = { Text("이름") },
                placeholder = { Text("홍길동") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 학번 입력
            OutlinedTextField(
                value = uiState.studentNumber,
                onValueChange = viewModel::onStudentNumberChanged,
                label = { Text("학번") },
                placeholder = { Text("2024001234") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 학과 입력
            OutlinedTextField(
                value = uiState.departmentName,
                onValueChange = viewModel::onDepartmentNameChanged,
                label = { Text("학과") },
                placeholder = { Text("컴퓨터공학과") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 학생회 ID 선택 (간단히 텍스트 필드로 구현)
            OutlinedTextField(
                value = uiState.councilId.toString(),
                onValueChange = { value ->
                    value.toLongOrNull()?.let { viewModel.onCouncilIdChanged(it) }
                },
                label = { Text("학생회 ID") },
                placeholder = { Text("1") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 학생회 임원 여부
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.isCouncilOfficer,
                    onCheckedChange = viewModel::onCouncilOfficerChanged
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "학생회 임원입니다",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 회원가입 버튼
            Button(
                onClick = viewModel::onSignUpClicked,
                enabled = uiState.canSignUp && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SolsolPrimary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "회원가입",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}