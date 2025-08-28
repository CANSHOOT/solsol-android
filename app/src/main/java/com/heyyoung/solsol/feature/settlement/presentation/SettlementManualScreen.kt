package com.heyyoung.solsol.feature.settlement.presentation

import android.util.Log
import com.heyyoung.solsol.feature.settlement.domain.model.Person
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementGroup
import com.heyyoung.solsol.feature.settlement.presentation.SettlementEqualViewModel
import java.math.BigDecimal

private const val TAG = "SettlementManualScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementManualScreen(
    participants: List<Person>,
    onNavigateBack: () -> Unit = {},
    onNavigateToComplete: (
        settlementGroup: SettlementGroup,
        participants: List<Person>,
        totalAmount: Int
    ) -> Unit = { _, _, _ -> },
    viewModel: SettlementEqualViewModel = hiltViewModel() // ✅ Equal과 같은 VM 재사용
) {
    // ViewModel 상태
    val uiState by viewModel.uiState.collectAsState()

    // 각 참여자별 입력 금액 상태 관리
    var participantAmounts by remember {
        mutableStateOf(
            participants.associateWith { "" }.toMutableMap()
        )
    }

    // 총액 계산
    val totalAmount = participantAmounts.values.sumOf { amountText ->
        amountText.toIntOrNull() ?: 0
    }

    Log.d(TAG, "직접 입력하기 화면 진입 - 참여자: ${participants.size}명")
    Log.d(TAG, "현재 입력 상태: $participantAmounts")

    // ✅ 정산 완료되면 완료화면으로 이동
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            uiState.createdSettlement?.let { settlement ->
                viewModel.onSettlementCompleteNavigated()
                onNavigateToComplete(
                    settlement,
                    participants,
                    totalAmount
                )
            }
        }
    }

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
                text = "개인별 금액을 입력해주세요",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 참여자별 금액 입력 리스트
            Box(
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(participants.filter { !it.isMe }) { person ->   // 👈 '나' 제외
                        PersonAmountInputCard(
                            person = person,
                            amount = participantAmounts[person] ?: "",
                            onAmountChange = { newAmount ->
                                val filteredAmount = newAmount.filter { it.isDigit() }
                                if (filteredAmount.length <= 8) {
                                    participantAmounts = participantAmounts.toMutableMap().apply {
                                        this[person] = filteredAmount
                                    }
                                    Log.d(TAG, "✅ ${person.name} 금액 입력: ${filteredAmount}원")
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 총합 표시
            TotalAmountDisplay(totalAmount = totalAmount)

            Spacer(modifier = Modifier.height(24.dp))

            // 정산 요청 버튼
            Button(
                onClick = {
                    val organizerId = participants.find { it.isMe }?.id ?: "me"

                    // Person 리스트에 금액 반영
                    val updatedParticipants = participants.map { person ->
                        val inputAmount = participantAmounts[person]?.toIntOrNull() ?: 0
                        person.copy(amount = BigDecimal.valueOf(inputAmount.toLong()))
                    }

                    Log.d(TAG, "🚀 수동 정산 API 요청 시작: 총액=${totalAmount}원")
                    viewModel.createSettlement(
                        organizerId = organizerId,
                        groupName = "직접 입력 정산",
                        totalAmount = totalAmount.toDouble(),
                        participants = updatedParticipants
                    )
                },
                enabled = totalAmount > 0 && !uiState.isCreating,
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
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
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
private fun PersonAmountInputCard(
    person: Person,
    amount: String,
    onAmountChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .shadow(4.dp)
            .border(
                width = 2.dp,
                color = if (amount.isNotEmpty()) Color(0xFF8B5FBF) else Color(0xCCE2E8F0),
                shape = RoundedCornerShape(12.dp)
            )
            .width(342.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (amount.isNotEmpty()) Color(0xFFF8F4FD) else Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (person.isMe) "${person.name} (나)" else person.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E),
                modifier = Modifier.width(100.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text("₩", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF999999))

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = formatDisplayAmount(amount),
                onValueChange = { newValue ->
                    val numberOnly = newValue.replace(",", "").filter { it.isDigit() }
                    onAmountChange(numberOnly)
                },
                placeholder = { Text("0", color = Color(0xFFCCCCCC), textAlign = TextAlign.End) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("원", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF999999))
        }
    }
}

// 금액 포맷팅
private fun formatDisplayAmount(amount: String): String {
    return if (amount.isEmpty()) "" else {
        val number = amount.toLongOrNull() ?: 0L
        String.format("%,d", number)
    }
}

@Composable
private fun TotalAmountDisplay(totalAmount: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp, modifier = Modifier.width(342.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "총 입력 금액: ${String.format("%,d", totalAmount)}원",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E)
        )
    }
}
