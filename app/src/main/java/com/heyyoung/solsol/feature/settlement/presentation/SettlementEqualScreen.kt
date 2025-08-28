package com.heyyoung.solsol.feature.settlement.presentation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.heyyoung.solsol.feature.settlement.domain.model.Person
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal

private const val TAG = "SettlementEqualScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementEqualScreen(
    participants: List<Person>,
    onNavigateBack: () -> Unit = {},
    onRequestSettlement: (Int, Map<Person, Int>) -> Unit = { _, _ -> },
    onNavigateToComplete: (settlementGroup: com.heyyoung.solsol.feature.settlement.domain.model.SettlementGroup, participants: List<Person>, totalAmount: Int, amountPerPerson: Int) -> Unit = { _, _, _, _ -> },
    viewModel: SettlementEqualViewModel = hiltViewModel()
) {
    // ViewModel 상태 관리
    val uiState by viewModel.uiState.collectAsState()
    
    // 로컬 상태 관리
    var totalAmountText by remember { mutableStateOf("") }
    var groupNameText by remember { mutableStateOf("") }
    val totalAmount = totalAmountText.toIntOrNull() ?: 0

    // 계산 로직
    val perPersonAmount = if (totalAmount > 0 && participants.isNotEmpty()) {
        totalAmount / participants.size
    } else 0

    val remainder = if (totalAmount > 0 && participants.isNotEmpty()) {
        totalAmount % participants.size
    } else 0

    // API 완료 시 화면 전환
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            uiState.createdSettlement?.let { settlement ->
                Log.d(TAG, "✅ 정산 생성 완료 - 완료 화면으로 이동")
                // 상태 초기화 후 완료 화면으로 이동
                viewModel.onSettlementCompleteNavigated()
                onNavigateToComplete(
                    settlement,
                    participants,
                    totalAmount,
                    perPersonAmount
                )
            }
        }
    }

    Log.d(TAG, "똑같이 나누기 화면 진입 - 참여자: ${participants.size}명, 총액: ${totalAmount}원")
    Log.d(TAG, "1인당: ${perPersonAmount}원, 나머지: ${remainder}원 (헤이영 제공)")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("정산하기") },
            navigationIcon = {
                IconButton(onClick = {
                    Log.d(TAG, "뒤로가기 클릭")
                    onNavigateBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1C1C1E),
                navigationIconContentColor = Color(0xFF1C1C1E)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 제목
            Text(
                text = "총 금액을 입력해주세요",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 그룹명 입력
            OutlinedTextField(
                value = groupNameText,
                onValueChange = { 
                    if (it.length <= 20) { // 최대 20자까지만
                        groupNameText = it 
                    }
                },
                label = { Text("정산 그룹명") },
                placeholder = { Text("예: 치킨값 정산, 카페 모임비") },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5FBF),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 총 금액 입력 카드
            TotalAmountInputCard(
                amount = totalAmountText,
                onAmountChange = { newAmount ->
                    // 숫자만 입력 허용
                    val filteredAmount = newAmount.filter { it.isDigit() }
                    if (filteredAmount.length <= 8) { // 최대 8자리까지만
                        totalAmountText = filteredAmount
                        Log.d(TAG, "총 금액 입력: ${filteredAmount}원")
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 나누기 결과 카드 (보라색)
            if (totalAmount > 0) {
                EqualSplitResultCard(
                    perPersonAmount = perPersonAmount,
                    participantCount = participants.size
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 개인별 결제 금액 카드
                IndividualAmountCard(
                    participants = participants,
                    perPersonAmount = perPersonAmount,
                    remainder = remainder
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 오류 메시지 표시
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3F3)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ ${uiState.error}",
                            color = Color(0xFFE53E3E),
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("확인", color = Color(0xFF8B5FBF))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 정산 요청하기 버튼
            Button(
                onClick = {
                    if (groupNameText.isBlank()) {
                        // 에러는 ViewModel에서 처리하지만, 여기서 간단히 로그만
                        Log.w(TAG, "그룹명이 입력되지 않음")
                        return@Button
                    }
                    
                    val organizerId = participants.find { it.isMe }?.id ?: "me"
                    Log.d(TAG, "🚀 정산 API 요청 시작: $groupNameText, ${totalAmount}원, ${participants.size}명")
                    val updatedParticipants = participants.map { it.copy(amount = BigDecimal.valueOf(perPersonAmount.toLong())) }

                    viewModel.createSettlement(
                        organizerId = organizerId,
                        groupName = groupNameText.trim(),
                        totalAmount = totalAmount.toDouble(),
                        participants = updatedParticipants
                    )
                },
                enabled = totalAmount > 0 && participants.isNotEmpty() && groupNameText.isNotBlank() && !uiState.isCreating,
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        spotColor = Color(0x26000000),
                        ambientColor = Color(0x26000000)
                    )
                    .width(342.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xE58B5FBF),
                    disabledContainerColor = Color(0x4D8B5FBF)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (uiState.isCreating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "정산 생성 중...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = "정산 요청하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TotalAmountInputCard(
    amount: String,
    onAmountChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = 2.dp,
                color = Color(0xCCE2E8F0),
                shape = RoundedCornerShape(12.dp)
            )
            .width(342.dp)
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // ₩ 아이콘
                Text(
                    text = "₩",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF999999)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 금액 입력
                OutlinedTextField(
                    value = if (amount.isEmpty()) "" else String.format("%,d", amount.toIntOrNull() ?: 0),
                    onValueChange = { newValue ->
                        // 콤마 제거하고 숫자만 추출
                        val numberOnly = newValue.replace(",", "").filter { it.isDigit() }
                        onAmountChange(numberOnly)
                    },
//                    placeholder = { Text("88,000", color = Color(0xFF999999)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1C1C1E)
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // "원" 텍스트
                Text(
                    text = "원",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}

@Composable
private fun EqualSplitResultCard(
    perPersonAmount: Int,
    participantCount: Int
) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF8B5FBF),
                shape = RoundedCornerShape(12.dp)
            )
            .width(342.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F7FF)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "똑같이 나누기 결과",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8B5FBF)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "1인당 ${String.format("%,d", perPersonAmount)}원",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5FBF)
            )
        }
    }
}

@Composable
private fun IndividualAmountCard(
    participants: List<Person>,
    perPersonAmount: Int,
    remainder: Int
) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = 2.dp,
                color = Color(0xB28B5FBF),
                shape = RoundedCornerShape(12.dp)
            )
            .width(342.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "개인별 결제 금액",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 각 참여자별 금액
            participants.forEach { person ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (person.isMe) "${person.name} (총무)" else person.name,
                        fontSize = 14.sp,
                        color = Color(0xFF1C1C1E),
                        maxLines = 1,                           // ✅ 한 줄
                        overflow = TextOverflow.Ellipsis        // ✅ 말줄임
                    )

                    Text(
                        text = "${String.format("%,d", perPersonAmount)}원",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E)
                    )
                }
            }

            // 헤이영 제공 (나머지 금액)
            if (remainder > 0) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "헤이영 제공",
                        fontSize = 14.sp,
                        color = Color(0xFF8B5FBF),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "+${remainder}원",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8B5FBF)
                    )
                }
            }
        }
    }
}