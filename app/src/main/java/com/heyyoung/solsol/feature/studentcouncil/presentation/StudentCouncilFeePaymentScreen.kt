package com.heyyoung.solsol.feature.studentcouncil.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.heyyoung.solsol.feature.studentcouncil.StudentCouncilViewModel
import com.heyyoung.solsol.core.network.CouncilFeeTransferCommand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCouncilFeePaymentScreen(
    councilName: String = "컴퓨터공학과 학생회",
    semester: String = "2025-1학기",
    amount: String = "50,000",
    feeId: Long = 10001L,
    cardNumber: String = "****1234",
    onNavigateBack: () -> Unit = {},
    onPaymentComplete: () -> Unit = {},
    viewModel: StudentCouncilViewModel = hiltViewModel()
) {
    var showSuccessScreen by remember { mutableStateOf(false) }
    
    if (showSuccessScreen) {
        FeePaymentSuccessScreen(
            councilName = councilName,
            semester = semester,
            amount = amount,
            onComplete = {
                showSuccessScreen = false
                onPaymentComplete()
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("회비 납부") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1C1C1E),
                navigationIconContentColor = Color(0xFF1C1C1E)
            )
        )

        // 본문
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // 학생회 정보 박스
            CouncilInfoBox(
                councilName = councilName,
                semester = semester
            )

            Spacer(Modifier.height(20.dp))

            // 회비 금액 박스
            FeeAmountBox(amount = amount)

            Spacer(Modifier.height(20.dp))

            // 카드 정보 박스
            PaymentCardInfoBox(cardNumber = cardNumber)

            Spacer(Modifier.height(32.dp))

            // 수수료 안내
            Text(
                text = "학생회비 납부 수수료 무료",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // 납부하기 버튼
            Button(
                onClick = {
                    // 실제 납부 API 호출
                    val transferCommand = CouncilFeeTransferCommand(
                        feeId = feeId,
                        withdrawalAccountNo = "110-123-456789", // 실제로는 사용자 계좌번호
                        depositTransactionSummary = "$semester $councilName 회비",
                        withdrawalTransactionSummary = "$semester 회비 납부"
                    )
                    
                    viewModel.transferFee(transferCommand) {
                        showSuccessScreen = true
                    }
                },
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        spotColor = Color(0x40000000),
                        ambientColor = Color(0x40000000)
                    )
                    .width(342.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5FBF),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = "납부하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CouncilInfoBox(
    councilName: String,
    semester: String
) {
    Row(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = 2.dp,
                color = Color(0xB2E2E8F0),
                shape = RoundedCornerShape(size = 12.dp)
            )
            .width(342.dp)
            .height(100.dp)
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 학생회 아이콘
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color(0xFF8B5FBF).copy(alpha = 0.1f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "회",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5FBF)
            )
        }

        Spacer(Modifier.width(12.dp))

        // 학생회 정보
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = councilName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$semester 회비",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun FeeAmountBox(amount: String) {
    Column(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = 1.dp,
                color = Color(0xCC8B5FBF),
                shape = RoundedCornerShape(size = 16.dp)
            )
            .padding(1.dp)
            .width(342.dp)
            .height(120.dp)
            .background(
                color = Color(0xFFF8F7FF),
                shape = RoundedCornerShape(size = 16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "납부 금액",
            fontSize = 14.sp,
            color = Color(0xFF7D6BB0),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${amount}원",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1C1C1E)
        )
    }
}

@Composable
private fun PaymentCardInfoBox(cardNumber: String) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x0D000000),
                ambientColor = Color(0x0D000000)
            )
            .border(
                width = 1.dp,
                color = Color(0x33718096),
                shape = RoundedCornerShape(12.dp)
            )
            .width(342.dp)
            .height(120.dp)
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 카드 정보
            Text(
                text = "신한 체크카드",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = cardNumber,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = "안전한 결제 시스템",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8B5FBF)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeePaymentSuccessScreen(
    councilName: String = "컴퓨터공학과 학생회",
    semester: String = "2025-1학기",
    amount: String = "50,000",
    onComplete: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("회비 납부") },
            navigationIcon = {
                IconButton(onClick = onComplete) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1C1C1E),
                navigationIconContentColor = Color(0xFF1C1C1E)
            )
        )

        Spacer(Modifier.height(80.dp))

        // 체크 아이콘
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF8B5FBF), RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(32.dp))

        // 납부 완료 메시지
        Text(
            "${councilName}\n${semester} 회비 ${amount}원 납부",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1C1C1E),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "납부 완료되었습니다!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        // 완료 버튼
        Button(
            onClick = onComplete,
            modifier = Modifier
                .shadow(4.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
                .width(180.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5FBF)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("완료", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }

        Spacer(Modifier.height(32.dp))
    }
}