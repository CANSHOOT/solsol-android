package com.heyyoung.solsol.feature.dutchpay.presentation.search

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heyyoung.solsol.feature.dutchpay.domain.model.User
import com.heyyoung.solsol.feature.dutchpay.presentation.create.CreateDutchPayViewModel
import com.heyyoung.solsol.feature.dutchpay.presentation.nearby.CheckNearbyPermissions
import com.heyyoung.solsol.feature.dutchpay.presentation.nearby.NearbyBottomSheet
import com.heyyoung.solsol.ui.theme.SolsolPrimary

/**
 * 참여자 검색 화면
 * - 학번/이름으로 검색
 * - 근처 기기로 찾기 (추후 구현)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantSearchScreen(
    onNavigateBack: () -> Unit,
    onParticipantsSelected: (List<User>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateDutchPayViewModel = hiltViewModel()
) {
    val TAG = "ParticipantSearchScreen"
    
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val selectedParticipants by viewModel.uiState.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showNearbyBottomSheet by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // 🔍 검색 결과 변경 감지 및 로깅
    LaunchedEffect(searchResults, isSearching) {
        Log.d(TAG, "🔄 searchResults 변경 감지! 새로운 결과 개수: ${searchResults.size}, isSearching: $isSearching")
        searchResults.forEachIndexed { index, user ->
            Log.d(TAG, "   🔍 [$index] ${user.name} (${user.userId}) - ${user.departmentName}")
        }
        
        // 검색 중이고 결과가 변경되었다면 (성공 또는 실패) 검색 완료 처리
        if (isSearching) {
            Log.d(TAG, "✅ 검색 완료 - isSearching을 false로 변경")
            isSearching = false
        }
    }
    
    // 🔄 selectedParticipants 변경 감지
    LaunchedEffect(selectedParticipants.selectedParticipants) {
        Log.d(TAG, "👥 선택된 참여자 변경: ${selectedParticipants.selectedParticipants.size}명")
        selectedParticipants.selectedParticipants.forEach { user ->
            Log.d(TAG, "   ✓ ${user.name} (${user.userId})")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("참여자 선택") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { 
                            Log.d(TAG, "🎯 다음 단계로 진행! 선택된 참여자: ${selectedParticipants.selectedParticipants.size}명")
                            selectedParticipants.selectedParticipants.forEach { user ->
                                Log.d(TAG, "   📋 ${user.name} (${user.userId})")
                            }
                            onParticipantsSelected(selectedParticipants.selectedParticipants)
                        },
                        enabled = selectedParticipants.selectedParticipants.isNotEmpty()
                    ) {
                        Text(
                            if (selectedParticipants.selectedParticipants.isEmpty()) "다음" 
                            else "다음 (${selectedParticipants.selectedParticipants.size})",
                            color = if (selectedParticipants.selectedParticipants.isNotEmpty()) 
                                MaterialTheme.colorScheme.onPrimary 
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selectedParticipants.selectedParticipants.isNotEmpty()) 
                                FontWeight.Bold else FontWeight.Normal
                        )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 검색 입력 필드
            SearchSection(
                searchQuery = searchQuery,
                onSearchQueryChanged = { searchQuery = it },
                isSearching = isSearching,
                onSearchClick = {
                    if (searchQuery.isNotEmpty()) {
                        Log.d(TAG, "🔍 검색 버튼 클릭! searchQuery: '$searchQuery'")
                        isSearching = true
                        Log.d(TAG, "⏳ 검색 시작 - isSearching = true")
                        viewModel.onSearchQueryChanged(searchQuery)
                    } else {
                        Log.d(TAG, "❌ 검색 버튼 클릭했지만 검색어가 비어있음")
                    }
                }
            )
            
            // 근처 기기로 찾기 버튼
            NearbyDevicesSection(
                onNearbySearchClick = { 
                    showPermissionDialog = true
                }
            )
            
            // 선택된 참여자 리스트
            if (selectedParticipants.selectedParticipants.isNotEmpty()) {
                SelectedParticipantsSection(
                    selectedParticipants = selectedParticipants.selectedParticipants,
                    onRemoveParticipant = { user ->
                        Log.d(TAG, "🗑️ 선택된 참여자에서 제거: ${user.name}")
                        viewModel.onParticipantRemoved(user)
                    }
                )
            }
            
            // 검색 결과 표시 영역
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 🎯 조건문 분기 로깅 및 처리
                Log.d(TAG, "📋 LazyColumn 컴포지션 시작")
                Log.d(TAG, "   - searchResults.size: ${searchResults.size}")
                Log.d(TAG, "   - searchQuery: '$searchQuery'")
                Log.d(TAG, "   - isSearching: $isSearching")
                
                when {
                    searchResults.isNotEmpty() -> {
                        Log.d(TAG, "✅ 검색 결과 있음 - ${searchResults.size}개 아이템 표시")
                        items(searchResults) { user ->
                            val isSelected = selectedParticipants.selectedParticipants.contains(user)
                            Log.d(TAG, "🔧 SearchResultItem 생성: ${user.name} (선택됨: $isSelected)")
                            
                            SearchResultItem(
                                user = user,
                                isSelected = isSelected,
                                onSelectionChanged = { newIsSelected ->
                                    Log.d(TAG, "👆 사용자 선택 변경: ${user.name} -> $newIsSelected")
                                    if (newIsSelected) {
                                        Log.d(TAG, "➕ 참여자 추가: ${user.name}")
                                        viewModel.onParticipantAdded(user)
                                    } else {
                                        Log.d(TAG, "➖ 참여자 제거: ${user.name}")
                                        viewModel.onParticipantRemoved(user)
                                    }
                                }
                            )
                        }
                    }
                    searchQuery.isNotEmpty() && !isSearching -> {
                        Log.d(TAG, "📭 검색 결과 없음 - EmptySearchResult 표시")
                        item {
                            EmptySearchResult()
                        }
                    }
                    else -> {
                        Log.d(TAG, "🏠 초기 상태 - SearchGuide 표시")
                        item {
                            SearchGuide()
                        }
                    }
                }
            }
        }
    }
    
    // 권한 확인 다이얼로그
    if (showPermissionDialog) {
        CheckNearbyPermissions(
            onPermissionsGranted = {
                showPermissionDialog = false
                showNearbyBottomSheet = true
            },
            onPermissionsDenied = { deniedPermissions ->
                showPermissionDialog = false
                Log.d(TAG, "권한 거절됨: $deniedPermissions")
            }
        )
    }
    
    // Nearby 기기 검색 BottomSheet
    if (showNearbyBottomSheet) {
        NearbyBottomSheet(
            onDismiss = { showNearbyBottomSheet = false },
            onUserSelected = { user ->
                Log.d(TAG, "근처에서 사용자 선택: ${user.name}")
                viewModel.onParticipantAdded(user)
                showNearbyBottomSheet = false
            }
        )
    }
}

@Composable
private fun SearchSection(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    isSearching: Boolean,
    onSearchClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "참여자 검색",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    label = { Text("이름 또는 학번 검색") },
                    placeholder = { Text("홍길동 또는 20210001") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearchClick() }
                    )
                )
                
                Button(
                    onClick = onSearchClick,
                    enabled = searchQuery.isNotBlank() && !isSearching,
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolsolPrimary
                    )
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("검색")
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyDevicesSection(
    onNearbySearchClick: () -> Unit
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
                text = "근처에서 찾기",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedButton(
                onClick = onNearbySearchClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("근처 기기로 찾기")
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    user: User,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                SolsolPrimary.copy(alpha = 0.1f) 
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = SolsolPrimary
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${user.studentNumber} • ${user.departmentName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = SolsolPrimary
                )
            )
        }
    }
}

@Composable
private fun EmptySearchResult() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "검색 결과가 없습니다",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "다른 검색어를 입력해보세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectedParticipantsSection(
    selectedParticipants: List<User>,
    onRemoveParticipant: (User) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "선택된 참여자",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${selectedParticipants.size}명",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SolsolPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            selectedParticipants.forEach { participant ->
                SelectedParticipantItem(
                    user = participant,
                    onRemove = { onRemoveParticipant(participant) }
                )
            }
        }
    }
}

@Composable
private fun SelectedParticipantItem(
    user: User,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = SolsolPrimary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
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
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "제거",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SearchGuide() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "참여자를 검색해주세요",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "이름이나 학번으로 검색할 수 있어요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}