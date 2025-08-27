package com.heyyoung.solsol.feature.settlement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.heyyoung.solsol.feature.settlement.domain.model.Person
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementGroup
import kotlinx.coroutines.delay

@Composable
fun SettlementCompleteScreen(
    settlementGroup: SettlementGroup?,
    participants: List<Person>,
    totalAmount: Int,
    amountPerPerson: Int,
    onNavigateToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 애니메이션을 위한 상태
    var showCheckmark by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(200)
        showCheckmark = true
        delay(300)
        showContent = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF8B5FBF),
                        Color(0xFF6A4C93)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        // 체크마크 아이콘
        if (showCheckmark) {
            Card(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Color(0x40000000)
                    ),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "완료",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (showContent) {
            // 제목
            Text(
                text = "정산 요청 완료!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 부제목
            Text(
                text = "참여자들에게 정산 알림이 발송되었어요",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // 정산 정보 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0x40000000)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 그룹명
                    settlementGroup?.let { group ->
                        Text(
                            text = group.groupName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // 총 금액
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "총 금액",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = "${String.format("%,d", totalAmount)}원",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 참여 인원
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "참여 인원",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = "${participants.size}명",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 1인당 금액
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1인당 금액",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = "${String.format("%,d", amountPerPerson)}원",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5FBF)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 구분선
                    Divider(
                        color = Color(0xFFE0E0E0),
                        thickness = 1.dp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 참여자 목록
                    Text(
                        text = "참여자 목록",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    participants.forEach { participant ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (participant.isMe) "${participant.name} (나)" else participant.name,
                                fontSize = 14.sp,
                                color = if (participant.isMe) Color(0xFF8B5FBF) else Color(0xFF666666),
                                fontWeight = if (participant.isMe) FontWeight.Medium else FontWeight.Normal
                            )
                            Text(
                                text = if (participant.isMe) "총무" else "대기중",
                                fontSize = 12.sp,
                                color = if (participant.isMe) Color(0xFF4CAF50) else Color(0xFF999999),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 홈으로 가기 버튼
            Button(
                onClick = onNavigateToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = Color(0x40000000)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF8B5FBF)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "홈으로 가기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}