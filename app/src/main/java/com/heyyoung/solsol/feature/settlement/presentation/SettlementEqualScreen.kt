package com.heyyoung.solsol.feature.settlement.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TAG = "SettlementEqualScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementEqualScreen(
    participants: List<Person>,
    onNavigateBack: () -> Unit = {},
    onRequestSettlement: (Int, Map<Person, Int>) -> Unit = { _, _ -> }
) {
    // 상태 관리
    var totalAmountText by remember { mutableStateOf("") }
    val totalAmount = totalAmountText.toIntOrNull() ?: 0

    // 계산 로직
    val perPersonAmount = if (totalAmount > 0 && participants.isNotEmpty()) {
        totalAmount / participants.size
    } else 0

    val remainder = if (totalAmount > 0 && participants.isNotEmpty()) {
        totalAmount % participants.size
    } else 0

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

            // 정산 요청하기 버튼
            Button(
                onClick = {
                    val settlementMap = participants.associateWith { perPersonAmount }
                    Log.d(TAG, "정산 요청 - 총액: ${totalAmount}원, 1인당: ${perPersonAmount}원, 나머지: ${remainder}원")
                    onRequestSettlement(totalAmount, settlementMap)
                },
                enabled = totalAmount > 0 && participants.isNotEmpty(),
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