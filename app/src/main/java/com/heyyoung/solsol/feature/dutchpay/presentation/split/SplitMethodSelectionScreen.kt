package com.heyyoung.solsol.feature.dutchpay.presentation.split

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.heyyoung.solsol.ui.theme.SolsolPrimary

/**
 * 분할 방식 선택 화면
 * - 똑같이 나누기
 * - 직접 입력하기
 * - 랜덤 게임으로 정하기
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitMethodSelectionScreen(
    onNavigateBack: () -> Unit,
    onEqualSplitSelected: () -> Unit,
    onCustomSplitSelected: () -> Unit,
    onRandomGameSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("정산 방식 선택") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SolsolPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "어떤 방식으로 나눌까요?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "상황에 맞는 정산 방식을 선택해주세요",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SplitMethodCard(
                title = "똑같이 나누기",
                description = "모든 참여자가 동일한 금액 분담",
                icon = Icons.Default.Face,
                onClick = onEqualSplitSelected
            )
            
            SplitMethodCard(
                title = "직접 입력하기",
                description = "각 참여자별로 분담 금액 직접 설정",
                icon = Icons.Default.Edit,
                onClick = onCustomSplitSelected
            )
            
            SplitMethodCard(
                title = "랜덤 게임으로 정하기",
                description = "재미있는 게임으로 분담 금액 결정",
                icon = Icons.Default.Star,
                onClick = onRandomGameSelected
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SplitMethodCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = SolsolPrimary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}