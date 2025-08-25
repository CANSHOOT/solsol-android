package com.heyyoung.solsol.feature.dutchpay.presentation.amount

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.heyyoung.solsol.feature.dutchpay.domain.model.User
import com.heyyoung.solsol.ui.theme.SolsolPrimary
import java.text.NumberFormat
import java.util.*

/**
 * 금액 입력 및 확인 화면
 * - 총 금액 입력
 * - 참여자별 분담 금액 확인
 * - 정산 요청 버튼
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountInputScreen(
    participants: List<User>,
    onNavigateBack: () -> Unit,
    onRequestPayment: (Double, List<User>) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var totalAmount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val totalAmountValue = totalAmount.toDoubleOrNull() ?: 0.0
    val participantCount = participants.size + 1 // +1 for current user
    val amountPerPerson = if (participantCount > 0) totalAmountValue / participantCount else 0.0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("정산 금액 입력") },
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
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TotalAmountSection(
                    totalAmount = totalAmount,
                    onTotalAmountChanged = { totalAmount = it }
                )
            }
            
            item {
                ParticipantsSummarySection(
                    participantCount = participantCount,
                    participants = participants
                )
            }
            
            item {
                AmountBreakdownSection(
                    totalAmount = totalAmountValue,
                    amountPerPerson = amountPerPerson,
                    participantCount = participantCount
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                RequestPaymentButton(
                    isEnabled = totalAmountValue >= 100 && 
                               totalAmountValue <= 10_000_000 && 
                               participants.isNotEmpty(),
                    isLoading = isLoading,
                    onClick = {
                        isLoading = true
                        onRequestPayment(totalAmountValue, participants)
                    },
                    onLoadingComplete = { isLoading = false }
                )
            }
        }
    }
}

@Composable
private fun TotalAmountSection(
    totalAmount: String,
    onTotalAmountChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "총 정산 금액을 입력해주세요",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = totalAmount,
                onValueChange = { value ->
                    // 숫자만 입력 가능하도록 필터링 (최대 10자리)
                    if (value.isEmpty() || (value.all { it.isDigit() } && value.length <= 10)) {
                        onTotalAmountChanged(value)
                    }
                },
                label = { Text("금액") },
                placeholder = { Text("0") },
                suffix = { Text("원") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold
                ),
                supportingText = {
                    if (totalAmount.isNotEmpty()) {
                        val amount = totalAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 10_000_000) { // 1천만원 초과
                            Text(
                                "최대 1천만원까지 입력 가능합니다",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (amount < 100) { // 100원 미만
                            Text(
                                "최소 100원 이상 입력해주세요",
                                color = MaterialTheme.colorScheme.error  
                            )
                        }
                    }
                },
                isError = totalAmount.isNotEmpty() && run {
                    val amount = totalAmount.toDoubleOrNull() ?: 0.0
                    amount > 10_000_000 || amount < 100
                }
            )
        }
    }
}

@Composable
private fun ParticipantsSummarySection(
    participantCount: Int,
    participants: List<User>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "참여자",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "총 ${participantCount}명",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SolsolPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // 본인 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = SolsolPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "나",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(본인)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 다른 참여자들
            participants.take(3).forEach { participant ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = participant.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // 더 많은 참여자가 있을 경우 표시
            if (participants.size > 3) {
                Text(
                    text = "외 ${participants.size - 3}명",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 28.dp)
                )
            }
        }
    }
}

@Composable
private fun AmountBreakdownSection(
    totalAmount: Double,
    amountPerPerson: Double,
    participantCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SolsolPrimary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "정산 내역",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("총 금액")
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalAmount.toInt())}원",
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("참여자")
                Text(
                    text = "${participantCount}명",
                    fontWeight = FontWeight.Medium
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "1인당 분담 금액",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(amountPerPerson.toInt())}원",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SolsolPrimary
                )
            }
        }
    }
}

@Composable
private fun RequestPaymentButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    onLoadingComplete: () -> Unit = {}
) {
    LaunchedEffect(isLoading) {
        if (isLoading) {
            kotlinx.coroutines.delay(100) // 약간의 딜레이로 UI 업데이트 확인
        }
    }
    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SolsolPrimary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = "정산 요청하기",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}