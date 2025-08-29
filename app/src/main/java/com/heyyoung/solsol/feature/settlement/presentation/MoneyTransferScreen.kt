package com.heyyoung.solsol.feature.settlement.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.AccountBox
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
import androidx.activity.compose.BackHandler

private const val TAG = "MoneyTransferScreen"
enum class TransferSide { SENT, RECEIVED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyTransferScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToRemittance: (groupId: Long, receiverName: String, amount: Long) -> Unit
) {
    val viewModel: MoneyTransferViewModel = androidx.hilt.navigation.compose.hiltViewModel()

    var selectedSide by remember { mutableStateOf(TransferSide.SENT) }

    val loading by viewModel.loading.collectAsState(initial = false)
    val error by viewModel.error.collectAsState(initial = null)
    val sent by viewModel.sent.collectAsState(initial = emptyList())
    val received by viewModel.received.collectAsState(initial = emptyList())
    val currentList = if (selectedSide == TransferSide.SENT) sent else received

    // 화면 진입 시마다 refresh 실행
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    // 하드웨어/제스처 뒤로가기 버튼 처리
    BackHandler {
        onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFAFA),
                        Color.White
                    )
                )
            )
    ) {
        // 상단 앱바 - 그라데이션 배경
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "송금하기",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2D3748) // solsol_dark_text
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFFF7FAFC),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "뒤로",
                        tint = Color(0xFF2D3748), // solsol_dark_text
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )

        // 탭 디자인 - 더 모던하게
        val tabs = listOf("보낸요청", "받은요청")
        val selectedIndex = if (selectedSide == TransferSide.SENT) 0 else 1

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .background(
                    Color(0xFFF7FAFC),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedIndex == index
                Button(
                    onClick = { selectedSide = if (index == 0) TransferSide.SENT else TransferSide.RECEIVED },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) {
                            Color(0xFF8B5FBF) // solsol_purple
                        } else Color.Transparent,
                        contentColor = if (isSelected) {
                            Color(0xFFFFFFFF) // solsol_white
                        } else Color(0xFF718096) // solsol_gray_text
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = if (isSelected) {
                        ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    } else {
                        ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    }
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        when {
            loading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF8B5FBF).copy(alpha = 0.1f),
                                            Color(0xFFF093FB).copy(alpha = 0.05f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF8B5FBF), // solsol_purple
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "송금 내역을 불러오는 중...",
                            fontSize = 16.sp,
                            color = Color(0xFF718096), // solsol_gray_text
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            error != null -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 에러 상태 아이콘
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF6B6B).copy(alpha = 0.1f),
                                        Color(0xFFFF6B6B).copy(alpha = 0.05f)
                                    )
                                ),
                                shape = RoundedCornerShape(25.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = Color(0xFFFF6B6B),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        )
                    }

                    Spacer(Modifier.height(28.dp))
                    Text(
                        "목록을 불러오지 못했습니다.",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748) // solsol_dark_text
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        error!!,
                        fontSize = 14.sp,
                        color = Color(0xFF718096), // solsol_gray_text
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.refresh() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5FBF) // solsol_purple
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .shadow(
                                elevation = 12.dp,
                                spotColor = Color(0x338B5FBF),
                                ambientColor = Color(0x338B5FBF),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .height(48.dp)
                            .widthIn(min = 120.dp)
                    ) {
                        Text(
                            "다시 시도",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
            else -> {
                if (currentList.isEmpty()) {
                    // 빈 상태 - 더 트렌디하게
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 빈 상태 아이콘
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF8B5FBF).copy(alpha = 0.12f),
                                                Color(0xFFF093FB).copy(alpha = 0.06f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(30.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (selectedSide == TransferSide.SENT) Icons.Default.Send else Icons.Default.AccountBox,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFF8B5FBF) // solsol_purple
                                )
                            }

                            Spacer(Modifier.height(32.dp))
                            Text(
                                text = if (selectedSide == TransferSide.SENT)
                                    "보낸 요청이 없습니다"
                                else
                                    "받은 요청이 없습니다",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748) // solsol_dark_text
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "정산 완료 후 송금 요청이 여기에 표시됩니다",
                                fontSize = 15.sp,
                                color = Color(0xFF718096), // solsol_gray_text
                                lineHeight = 22.sp
                            )
                        }
                    }
                } else {
                    // 실제 목록 렌더링
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(Modifier.height(4.dp)) }

                        item {
                            val title = if (selectedSide == TransferSide.SENT)
                                "보낸 요청 (내가 받아야 할 돈)"
                            else
                                "받은 요청 (내가 보내야 할 돈)"

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF8B5FBF).copy(alpha = 0.15f),
                                                    Color(0xFF8B5FBF).copy(alpha = 0.08f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(
                                                color = Color(0xFF8B5FBF), // solsol_purple
                                                shape = RoundedCornerShape(5.dp)
                                            )
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B5FBF), // solsol_purple
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        items(currentList) { request ->
                            MoneyTransferCard(
                                name = request.name,
                                amount = request.amount,
                                status = request.status,
                                onClick = {
                                    if (request.status == MoneyTransferStatus.PENDING && request.side == TransferSide.RECEIVED) {
                                        // 받은 요청(내가 보내야 할 돈) + 진행중일 때만 이동
                                        onNavigateToRemittance(request.groupId, request.name, request.amount)
                                    } else {
                                        Log.d(TAG, "클릭 무시: ${request.name}, side=${request.side}, status=${request.status}")
                                    }
                                }
                            )
                        }

                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoneyTransferCard(
    name: String,
    amount: Long,
    status: MoneyTransferStatus,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x1A8B5FBF),
                ambientColor = Color(0x1A8B5FBF),
                shape = RoundedCornerShape(20.dp)
            )
            .fillMaxWidth()
            .height(88.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF) // solsol_card_white
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 이미지 - 그라데이션 적용
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF8B5FBF).copy(alpha = 0.15f),
                                    Color(0xFFF093FB).copy(alpha = 0.08f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.first().toString(),
                        color = Color(0xFF8B5FBF), // solsol_purple
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // 이름
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748) // solsol_dark_text
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 금액
                Text(
                    text = "${String.format("%,d", amount)}원",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2D3748) // solsol_dark_text
                )

                // 상태 버튼
                StatusButton(status = status)
            }
        }
    }
}

@Composable
private fun StatusButton(status: MoneyTransferStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        MoneyTransferStatus.PENDING -> Triple(
            Color(0xFFFFB366).copy(alpha = 0.12f), // 연한 오렌지/복숭아색
            Color(0xFF2D3748), // 어두운 텍스트로 가독성 확보
            "진행중"
        )
        MoneyTransferStatus.COMPLETED -> Triple(
            Color(0xFF68D391).copy(alpha = 0.12f), // 연한 초록색
            Color(0xFF2D3748), // 어두운 텍스트로 가독성 확보
            "완료"
        )
    }

    Surface(
        onClick = { Log.d(TAG, "상태 버튼 클릭: $status") },
        modifier = Modifier
            .height(36.dp)
            .width(72.dp)
            .shadow(
                elevation = 6.dp,
                spotColor = backgroundColor.copy(alpha = 0.4f),
                ambientColor = backgroundColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(18.dp)
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(18.dp),
        contentColor = textColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

/** 화면 표시용 도메인 아이템 (ViewModel에서 채움) */
data class MoneyTransferItem(
    val name: String,
    val amount: Long,
    val status: MoneyTransferStatus,
    val side: TransferSide,
    val groupId: Long
)

enum class MoneyTransferStatus {
    PENDING,    // 진행중
    COMPLETED   // 완료
}