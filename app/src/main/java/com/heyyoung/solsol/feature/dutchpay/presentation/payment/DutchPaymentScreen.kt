package com.heyyoung.solsol.feature.dutchpay.presentation.payment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayParticipant
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayStatus
import com.heyyoung.solsol.feature.dutchpay.domain.model.ParticipantPaymentStatus
import com.heyyoung.solsol.ui.theme.SolsolPrimary
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 더치페이 정산/송금 화면
 * - 더치페이 정보 및 참여자 현황 표시
 * - 참여 및 송금 기능
 * - 실시간 상태 업데이트
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutchPaymentScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DutchPaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("더치페이 정산") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SolsolPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SolsolPrimary)
            }
        } else {
            uiState.dutchPay?.let { dutchPay ->
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 더치페이 정보
                    DutchPayInfoCard(dutchPay = dutchPay)
                    
                    // 참여자 현황
                    ParticipantsStatusCard(
                        participants = dutchPay.participants,
                        totalParticipants = dutchPay.participantCount
                    )
                    
                    // 액션 버튼들
                    ActionButtons(
                        uiState = uiState,
                        onJoinDutchPay = viewModel::onJoinDutchPay,
                        onAccountNumberChanged = viewModel::onAccountNumberChanged,
                        onTransactionSummaryChanged = viewModel::onTransactionSummaryChanged,
                        onSendPayment = viewModel::onSendPayment
                    )
                    
                    // 결제 결과 표시
                    uiState.paymentResult?.let { result ->
                        PaymentResultCard(result = result)
                    }
                }
            }
        }
    }
}

@Composable
private fun DutchPayInfoCard(dutchPay: com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayGroup) {
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
                text = dutchPay.groupName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SolsolPrimary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("총 금액")
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(dutchPay.totalAmount.toInt())}원",
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("참여자 수")
                Text(
                    text = "${dutchPay.participantCount}명",
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1인당 금액")
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(dutchPay.amountPerPerson.toInt())}원",
                    fontWeight = FontWeight.Bold,
                    color = SolsolPrimary
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("상태")
                StatusChip(status = dutchPay.status)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ParticipantsStatusCard(
    participants: List<DutchPayParticipant>,
    totalParticipants: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "참여자 현황",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            val completedCount = participants.count { it.paymentStatus == ParticipantPaymentStatus.COMPLETED }
            val pendingCount = participants.count { it.paymentStatus == ParticipantPaymentStatus.PENDING }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusSummaryItem("완료", completedCount, Color.Green)
                StatusSummaryItem("대기", pendingCount, Color.Yellow)
                StatusSummaryItem("미참여", totalParticipants - participants.size, Color.Gray)
            }
            
            if (participants.isNotEmpty()) {
                participants.forEach { participant ->
                    ParticipantItem(participant = participant)
                }
            }
        }
    }
}

@Composable
private fun StatusSummaryItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ParticipantItem(participant: DutchPayParticipant) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = participant.user?.name ?: "참여자",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = participant.joinedAt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            PaymentStatusChip(status = participant.paymentStatus)
        }
    }
}

@Composable
private fun ActionButtons(
    uiState: DutchPaymentUiState,
    onJoinDutchPay: () -> Unit,
    onAccountNumberChanged: (String) -> Unit,
    onTransactionSummaryChanged: (String) -> Unit,
    onSendPayment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                uiState.canJoin -> {
                    Text(
                        text = "더치페이 참여",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Button(
                        onClick = onJoinDutchPay,
                        enabled = !uiState.isJoinLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SolsolPrimary)
                    ) {
                        if (uiState.isJoinLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("참여하기")
                        }
                    }
                }
                
                uiState.canPay -> {
                    Text(
                        text = "송금 정보 입력",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = uiState.accountNumber,
                        onValueChange = onAccountNumberChanged,
                        label = { Text("계좌번호") },
                        placeholder = { Text("0016174648358792") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = uiState.transactionSummary,
                        onValueChange = onTransactionSummaryChanged,
                        label = { Text("거래내역") },
                        placeholder = { Text("점심 더치페이 정산") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Button(
                        onClick = onSendPayment,
                        enabled = !uiState.isPaymentLoading && 
                                 uiState.accountNumber.isNotBlank() && 
                                 uiState.transactionSummary.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SolsolPrimary)
                    ) {
                        if (uiState.isPaymentLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            uiState.dutchPay?.let { dutchPay ->
                                Text("${NumberFormat.getNumberInstance(Locale.KOREA).format(dutchPay.amountPerPerson.toInt())}원 송금")
                            }
                        }
                    }
                }
                
                uiState.isOrganizer -> {
                    Text(
                        text = "주최자입니다",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SolsolPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                else -> {
                    if (uiState.currentParticipant?.paymentStatus == ParticipantPaymentStatus.COMPLETED) {
                        Text(
                            text = "✅ 송금이 완료되었습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Green,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentResultCard(result: com.heyyoung.solsol.feature.dutchpay.domain.model.PaymentResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isSuccess) Color.Green.copy(alpha = 0.1f) 
                           else Color.Red.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (result.isSuccess) "✅ 송금 완료" else "❌ 송금 실패",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (result.isSuccess) Color.Green else Color.Red
            )
            
            Text(
                text = result.message,
                style = MaterialTheme.typography.bodyMedium
            )
            
            result.transactionId?.let { transactionId ->
                Text(
                    text = "거래ID: $transactionId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: DutchPayStatus) {
    val (text, color) = when (status) {
        DutchPayStatus.ACTIVE -> "진행중" to Color.Blue
        DutchPayStatus.COMPLETED -> "완료" to Color.Green
        DutchPayStatus.CANCELLED -> "취소" to Color.Red
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PaymentStatusChip(status: ParticipantPaymentStatus) {
    val (text, color) = when (status) {
        ParticipantPaymentStatus.PENDING -> "대기중" to Color.Yellow
        ParticipantPaymentStatus.COMPLETED -> "완료" to Color.Green
        ParticipantPaymentStatus.FAILED -> "실패" to Color.Red
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}