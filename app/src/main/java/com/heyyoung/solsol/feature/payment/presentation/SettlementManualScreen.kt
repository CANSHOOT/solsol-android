package com.heyyoung.solsol.feature.settlement.presentation

import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TAG = "SettlementManualScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementManualScreen(
    participants: List<Person>,
    onNavigateBack: () -> Unit = {},
    onRequestSettlement: (Int, Map<Person, Int>) -> Unit = { _, _ -> }
) {
    // 각 참여자별 입력 금액 상태 관리
    var participantAmounts by remember {
        mutableStateOf(
            participants.associateWith { "" }.toMutableMap()
        )
    }

    // 총합 계산
    val totalAmount = participantAmounts.values.sumOf { amountText ->
        amountText.toIntOrNull() ?: 0
    }

    Log.d(TAG, "직접 입력하기 화면 진입 - 참여자: ${participants.size}명")

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
                    items(participants) { person ->
                        PersonAmountInputCard(
                            person = person,
                            amount = participantAmounts[person] ?: "",
                            onAmountChange = { newAmount ->
                                // 숫자만 입력 허용
                                val filteredAmount = newAmount.filter { it.isDigit() }
                                if (filteredAmount.length <= 8) { // 최대 8자리까지만
                                    participantAmounts[person] = filteredAmount
                                    Log.d(TAG, "${person.name} 금액 입력: ${filteredAmount}원")
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

            // 정산 요청하기 버튼
            Button(
                onClick = {
                    val settlementMap = participantAmounts.mapValues { (_, amountText) ->
                        amountText.toIntOrNull() ?: 0
                    }.filterValues { it > 0 }

                    Log.d(TAG, "정산 요청 - 총액: ${totalAmount}원")
                    settlementMap.forEach { (person, amount) ->
                        Log.d(TAG, "  ${person.name}: ${amount}원")
                    }
                    onRequestSettlement(totalAmount, settlementMap)
                },
                enabled = totalAmount > 0 && participantAmounts.values.any { it.isNotBlank() },
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
                Text(
                    text = "정산 요청하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이름 표시
            Text(
                text = if (person.isMe) "${person.name} (나)" else person.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E),
                modifier = Modifier.width(100.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // ₩ 아이콘
            Text(
                text = "₩",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF999999)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 금액 입력 필드
            OutlinedTextField(
                value = if (amount.isEmpty()) "" else String.format("%,d", amount.toIntOrNull() ?: 0),
                onValueChange = { newValue ->
                    // 콤마 제거하고 숫자만 추출
                    val numberOnly = newValue.replace(",", "").filter { it.isDigit() }
                    onAmountChange(numberOnly)
                },
                placeholder = {
                    Text(
                        "0",
                        color = Color(0xFFCCCCCC),
                        textAlign = TextAlign.End
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color(0xFF1C1C1E),
                    unfocusedTextColor = Color(0xFF1C1C1E)
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

            // "원" 텍스트
            Text(
                text = "원",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF999999)
            )
        }
    }
}

@Composable
private fun TotalAmountDisplay(totalAmount: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 구분선
        Divider(
            color = Color(0xFFE0E0E0),
            thickness = 1.dp,
            modifier = Modifier.width(342.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "총 입력 금액: ${String.format("%,d", totalAmount)}원",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E)
        )
    }
}