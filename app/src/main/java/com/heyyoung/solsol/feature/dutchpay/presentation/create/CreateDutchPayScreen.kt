package com.heyyoung.solsol.feature.dutchpay.presentation.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heyyoung.solsol.feature.dutchpay.domain.model.User
import com.heyyoung.solsol.ui.theme.SolsolPrimary
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDutchPayScreen(
    onNavigateBack: () -> Unit,
    onDutchPayCreated: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateDutchPayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var showParticipantSearch by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            uiState.createdDutchPayId?.let { groupId ->
                onDutchPayCreated(groupId)
            }
        }
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 에러 처리 (스낵바 등)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("더치페이 생성") },
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GroupInfoSection(
                    groupName = uiState.groupName,
                    totalAmount = uiState.totalAmountText,
                    onGroupNameChanged = viewModel::onGroupNameChanged,
                    onTotalAmountChanged = viewModel::onTotalAmountChanged
                )
            }

            item {
                ParticipantSection(
                    selectedParticipants = uiState.selectedParticipants,
                    onAddParticipant = { showParticipantSearch = true },
                    onRemoveParticipant = viewModel::onParticipantRemoved
                )
            }

            if (showParticipantSearch) {
                item {
                    ParticipantSearchSection(
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        onSearchQueryChanged = { query ->
                            searchQuery = query
                            viewModel.onSearchQueryChanged(query)
                        },
                        onParticipantSelected = { user ->
                            viewModel.onParticipantAdded(user)
                            showParticipantSearch = false
                            searchQuery = ""
                        },
                        onCancel = { 
                            showParticipantSearch = false
                            searchQuery = ""
                        }
                    )
                }
            }

            item {
                SummarySection(
                    participantCount = uiState.selectedParticipants.size + 1,
                    amountPerPerson = uiState.amountPerPerson
                )
            }

            item {
                CreateButton(
                    isLoading = uiState.isLoading,
                    isEnabled = uiState.groupName.isNotBlank() && 
                               uiState.totalAmount > 0 && 
                               uiState.selectedParticipants.isNotEmpty(),
                    onClick = viewModel::onCreateDutchPay
                )
            }
        }
    }
}

@Composable
private fun GroupInfoSection(
    groupName: String,
    totalAmount: String,
    onGroupNameChanged: (String) -> Unit,
    onTotalAmountChanged: (String) -> Unit
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
                text = "정산 정보",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = groupName,
                onValueChange = onGroupNameChanged,
                label = { Text("그룹명") },
                placeholder = { Text("예: 팀플 회식비") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = totalAmount,
                onValueChange = onTotalAmountChanged,
                label = { Text("총 금액") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("원") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ParticipantSection(
    selectedParticipants: List<User>,
    onAddParticipant: () -> Unit,
    onRemoveParticipant: (User) -> Unit
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
                
                OutlinedButton(
                    onClick = onAddParticipant
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("참여자 추가")
                }
            }
            
            selectedParticipants.forEach { participant ->
                ParticipantItem(
                    user = participant,
                    onRemove = { onRemoveParticipant(participant) }
                )
            }
        }
    }
}

@Composable
private fun ParticipantItem(
    user: User,
    onRemove: () -> Unit
) {
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
                    text = user.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${user.studentNumber} • ${user.departmentName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "제거",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ParticipantSearchSection(
    searchQuery: String,
    searchResults: List<User>,
    onSearchQueryChanged: (String) -> Unit,
    onParticipantSelected: (User) -> Unit,
    onCancel: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "참여자 검색",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onCancel) {
                    Text("취소")
                }
            }
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                label = { Text("이름 또는 학번") },
                placeholder = { Text("검색어를 입력하세요") },
                modifier = Modifier.fillMaxWidth()
            )
            
            searchResults.forEach { user ->
                SearchResultItem(
                    user = user,
                    onClick = { onParticipantSelected(user) }
                )
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    user: User,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${user.studentNumber} • ${user.departmentName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummarySection(
    participantCount: Int,
    amountPerPerson: Double
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "정산 요약",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("총 참여자")
                Text("${participantCount}명")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1인당 금액")
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(amountPerPerson.toInt())}원",
                    fontWeight = FontWeight.Bold,
                    color = SolsolPrimary
                )
            }
        }
    }
}

@Composable
private fun CreateButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
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
                text = "더치페이 생성",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}