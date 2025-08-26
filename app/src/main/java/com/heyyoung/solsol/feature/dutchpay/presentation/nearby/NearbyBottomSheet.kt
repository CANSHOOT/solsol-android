package com.heyyoung.solsol.feature.dutchpay.presentation.nearby

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import com.heyyoung.solsol.feature.dutchpay.domain.model.User
import com.heyyoung.solsol.ui.theme.SolsolPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyBottomSheet(
    onDismiss: () -> Unit,
    onUserSelected: (User) -> Unit,
    modifier: Modifier = Modifier,
    nearbyViewModel: NearbyViewModel = hiltViewModel()
) {
    val isAdvertising by nearbyViewModel.isAdvertising.collectAsStateWithLifecycle()
    val isDiscovering by nearbyViewModel.isDiscovering.collectAsStateWithLifecycle()
    val discoveredUsers by nearbyViewModel.discoveredUsers.collectAsStateWithLifecycle()
    val currentUser by nearbyViewModel.currentUser.collectAsStateWithLifecycle()
    
    // 현재 사용자 정보 로드
    LaunchedEffect(Unit) {
        nearbyViewModel.loadCurrentUser()
    }
    
    // BottomSheet가 열릴 때 자동으로 광고와 검색 시작 (currentUser가 로드된 후)
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            nearbyViewModel.startAdvertising()
            nearbyViewModel.startDiscovery()
        } else {
            // currentUser가 null인 경우 Toast 표시 (사용자에게 알림)
        }
    }
    
    // BottomSheet가 닫힐 때 정리
    DisposableEffect(Unit) {
        onDispose {
            nearbyViewModel.stopAdvertising()
            nearbyViewModel.stopDiscovery()
        }
    }

    ModalBottomSheetLayout(
        sheetContent = {
            NearbySheetContent(
                isAdvertising = isAdvertising,
                isDiscovering = isDiscovering,
                discoveredUsers = discoveredUsers.toList(),
                onRefresh = {
                    nearbyViewModel.clearDiscoveredUsers()
                    nearbyViewModel.startDiscovery()
                },
                onUserSelected = onUserSelected,
                onClose = onDismiss
            )
        },
        modifier = modifier
    ) {
        // Empty content - BottomSheet만 표시
    }
}

@Composable
private fun ModalBottomSheetLayout(
    sheetContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // BottomSheet 컨테이너
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        sheetContent()
    }
}

@Composable
private fun NearbySheetContent(
    isAdvertising: Boolean,
    isDiscovering: Boolean,
    discoveredUsers: List<User>,
    onRefresh: () -> Unit,
    onUserSelected: (User) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 헤더
        NearbySheetHeader(
            isAdvertising = isAdvertising,
            isDiscovering = isDiscovering,
            onRefresh = onRefresh,
            onClose = onClose
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 상태 인디케이터
        NearbyStatusIndicator(
            isAdvertising = isAdvertising,
            isDiscovering = isDiscovering
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 발견된 사용자 목록
        NearbyUserList(
            users = discoveredUsers,
            onUserSelected = onUserSelected,
            isSearching = isDiscovering
        )
    }
}

@Composable
private fun NearbySheetHeader(
    isAdvertising: Boolean,
    isDiscovering: Boolean,
    onRefresh: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = SolsolPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "근처에서 찾기",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        Row {
            IconButton(
                onClick = onRefresh,
                enabled = !isDiscovering
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "새로고침",
                    tint = if (isDiscovering) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else SolsolPrimary
                )
            }
            
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NearbyStatusIndicator(
    isAdvertising: Boolean,
    isDiscovering: Boolean
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
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusRow(
                label = "내 정보 공유",
                isActive = isAdvertising,
                description = if (isAdvertising) "다른 기기에서 나를 찾을 수 있어요" else "내 정보를 공유하지 않고 있어요"
            )
            
            StatusRow(
                label = "주변 검색",
                isActive = isDiscovering,
                description = if (isDiscovering) "주변 기기를 검색하고 있어요" else "검색을 중지했어요"
            )
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    isActive: Boolean,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Box(
            modifier = Modifier
                .size(12.dp)
                .padding(2.dp)
        ) {
            if (isActive) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 2.dp,
                    color = SolsolPrimary
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                ) {}
            }
        }
    }
}

@Composable
private fun NearbyUserList(
    users: List<User>,
    onUserSelected: (User) -> Unit,
    isSearching: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "발견된 사용자",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            when {
                isSearching && users.isEmpty() -> {
                    SearchingIndicator()
                }
                users.isEmpty() -> {
                    EmptyUserList()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(users) { user ->
                            NearbyUserItem(
                                user = user,
                                onUserSelected = { onUserSelected(user) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            color = SolsolPrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = "주변 기기를 검색하고 있어요...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyUserList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "주변에서 사용자를 찾을 수 없어요",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "다른 사용자가 앱을 실행하고\n근처에 있는지 확인해보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NearbyUserItem(
    user: User,
    onUserSelected: () -> Unit
) {
    Card(
        onClick = onUserSelected,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = SolsolPrimary.copy(alpha = 0.1f)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = SolsolPrimary,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
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
            
            Surface(
                modifier = Modifier.size(8.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = Color.Green
            ) {}
        }
    }
}