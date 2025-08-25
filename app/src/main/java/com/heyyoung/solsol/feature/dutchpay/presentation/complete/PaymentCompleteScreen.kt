package com.heyyoung.solsol.feature.dutchpay.presentation.complete

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.heyyoung.solsol.ui.theme.SolsolPrimary
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.*

/**
 * 정산 요청 완료 화면
 * - 성공 메시지
 * - 정산 금액 표시
 * - 홈으로 돌아가기 버튼
 */
@Composable
fun PaymentCompleteScreen(
    totalAmount: Double,
    participantCount: Int,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(300)
        showAnimation = true
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        // 성공 아이콘
        if (showAnimation) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = Color(0xFF4CAF50)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 성공 메시지
        Text(
            text = "정산 요청을 보냈어요!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "참여자들에게 정산 요청이 전송되었습니다.\n알림을 통해 결과를 확인할 수 있어요.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 정산 요약 카드
        PaymentSummaryCard(
            totalAmount = totalAmount,
            participantCount = participantCount
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 홈으로 돌아가기 버튼
        Button(
            onClick = onNavigateToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SolsolPrimary
            )
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "홈으로 돌아가기",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 추가 액션 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    // TODO: 실제로는 현재 생성된 더치페이의 groupId를 전달해야 함
                    // 현재는 임시로 안내 메시지만 표시
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("내역 보기")
            }
            
            OutlinedButton(
                onClick = {
                    // 정산 요청 정보를 공유하기 위한 텍스트 생성
                    val shareText = """
                        💰 솔솔 정산 요청
                        
                        총 금액: ${NumberFormat.getNumberInstance(Locale.KOREA).format(totalAmount.toInt())}원
                        참여자: ${participantCount}명
                        1인당 금액: ${NumberFormat.getNumberInstance(Locale.KOREA).format((totalAmount / participantCount).toInt())}원
                        
                        솔솔 캠퍼스페이로 간편하게 정산해보세요!
                    """.trimIndent()
                    
                    // Android 기본 공유 인텐트 실행
                    val intent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "솔솔 정산 요청")
                    }
                    
                    try {
                        context.startActivity(
                            android.content.Intent.createChooser(intent, "정산 요청 공유하기")
                        )
                    } catch (e: Exception) {
                        // 공유 앱이 없는 경우 처리
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("공유하기")
            }
        }
    }
}

@Composable
private fun PaymentSummaryCard(
    totalAmount: Double,
    participantCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SolsolPrimary.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "정산 요청 내역",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "총 정산 금액",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalAmount.toInt())}원",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = SolsolPrimary
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "참여자 수",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${participantCount}명",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "1인당 금액",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format((totalAmount / participantCount).toInt())}원",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}