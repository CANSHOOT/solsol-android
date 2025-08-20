package com.heyyoung.solsol.feature.dutchpay.presentation.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayParticipant
import com.heyyoung.solsol.feature.dutchpay.domain.model.ParticipantPaymentStatus
import com.heyyoung.solsol.ui.theme.SolsolPrimary
import com.heyyoung.solsol.ui.theme.SolsolSecondary
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 더치페이 정산/송금 화면
 * - 더치페이 정보 및 참여자 현황 표시
 * - 개인별 송금 상태 확인 (대기중/완료/실패)
 * - 원클릭 송금 버튼 (백엔드에서 금융 API 호출)
 * - 실시간 상태 업데이트 및 새로고침
 */
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
            // 에러 처리 (스낵바 등)
            viewModel.clearError()
        }
    }

    if (uiState.isPaymentSuccess) {
        LaunchedEffect(uiState.isPaymentSuccess) {
            // 송금 성공 처리
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
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DutchPayInfoCard(dutchPay = dutchPay)
                    }

                    item {
                        PaymentStatusCard(
                            dutchPay = dutchPay,
                            currentParticipant = uiState.currentParticipant,
                            canPay = uiState.canPay,
                            isPaymentLoading = uiState.isPaymentLoading,
                            onSendPayment = viewModel::onSendPayment
                        )
                    }

                    item {
                        Text(
                            text = "참여자 현황",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(dutchPay.participants) { participant ->
                        ParticipantStatusItem(
                            participant = participant,
                            amountPerPerson = dutchPay.amountPerPerson
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DutchPayInfoCard(
    dutchPay: com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayGroup
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
                text = dutchPay.groupName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Divider(color = SolsolPrimary.copy(alpha = 0.3f))
            
            InfoRow("총 금액", "${NumberFormat.getNumberInstance(Locale.KOREA).format(dutchPay.totalAmount.toInt())}원")
            InfoRow("참여자", "${dutchPay.participantCount}명")
            InfoRow("1인당 금액", "${NumberFormat.getNumberInstance(Locale.KOREA).format(dutchPay.amountPerPerson.toInt())}원")
            InfoRow("생성일", dutchPay.createdAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")))
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PaymentStatusCard(
    dutchPay: com.heyyoung.solsol.feature.dutchpay.domain.model.DutchPayGroup,
    currentParticipant: DutchPayParticipant?,
    canPay: Boolean,
    isPaymentLoading: Boolean,
    onSendPayment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (canPay) SolsolSecondary.copy(alpha = 0.1f) else Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentParticipant != null) {
                when (currentParticipant.paymentStatus) {
                    ParticipantPaymentStatus.PENDING -> {
                        Text(
                            text = "송금 대기 중",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SolsolSecondary
                        )
                        
                        Text(
                            text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(dutchPay.amountPerPerson.toInt())}원을 송금해주세요",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        
                        Button(
                            onClick = onSendPayment,
                            enabled = !isPaymentLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SolsolPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isPaymentLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text = "송금하기",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    ParticipantPaymentStatus.COMPLETED -> {
                        Text(
                            text = "송금 완료",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        
                        currentParticipant.paidAt?.let { paidAt ->
                            Text(
                                text = "송금 완료: ${paidAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    ParticipantPaymentStatus.FAILED -> {
                        Text(
                            text = "송금 실패",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Button(
                            onClick = onSendPayment,
                            enabled = !isPaymentLoading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SolsolPrimary
                            )
                        ) {
                            Text("다시 시도")
                        }
                    }
                }
            } else {
                Text(
                    text = "참여자 정보를 찾을 수 없습니다",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ParticipantStatusItem(
    participant: DutchPayParticipant,
    amountPerPerson: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = participant.user?.name ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = participant.user?.studentNumber ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                StatusChip(status = participant.paymentStatus)
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(amountPerPerson.toInt())}원",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    status: ParticipantPaymentStatus
) {
    val (color, text) = when (status) {
        ParticipantPaymentStatus.PENDING -> SolsolSecondary to "대기중"
        ParticipantPaymentStatus.COMPLETED -> Color(0xFF4CAF50) to "완료"
        ParticipantPaymentStatus.FAILED -> MaterialTheme.colorScheme.error to "실패"
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}