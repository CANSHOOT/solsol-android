package com.heyyoung.solsol.feature.settlement.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.feature.settlement.domain.model.NearbyUser
import com.heyyoung.solsol.feature.settlement.domain.model.NearbyConnectionStatus
import com.heyyoung.solsol.feature.settlement.domain.model.NearbyDiscoveryState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyBottomSheet(
    discoveryState: NearbyDiscoveryState,
    discoveredUsers: List<NearbyUser>,
    onStartSearch: () -> Unit,
    onStopSearch: () -> Unit,
    onUserSelect: (NearbyUser) -> Unit,
    onCloseSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Simple modal implementation instead of BottomSheetScaffold
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onCloseSheet() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .clickable(enabled = false) { }, // Prevent closing when clicking on the sheet
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            NearbyBottomSheetContent(
                discoveryState = discoveryState,
                discoveredUsers = discoveredUsers,
                onStartSearch = onStartSearch,
                onStopSearch = onStopSearch,
                onUserSelect = onUserSelect,
                onCloseSheet = onCloseSheet
            )
        }
    }
}

@Composable
private fun NearbyBottomSheetContent(
    discoveryState: NearbyDiscoveryState,
    discoveredUsers: List<NearbyUser>,
    onStartSearch: () -> Unit,
    onStopSearch: () -> Unit,
    onUserSelect: (NearbyUser) -> Unit,
    onCloseSheet: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "주변 기기에서 사용자 찾기",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onCloseSheet) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Control
        SearchControlSection(
            discoveryState = discoveryState,
            onStartSearch = onStartSearch,
            onStopSearch = onStopSearch
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status and Results
        when (discoveryState.status) {
            NearbyConnectionStatus.IDLE -> {
                EmptyStateContent()
            }
            NearbyConnectionStatus.DISCOVERING, 
            NearbyConnectionStatus.ADVERTISING -> {
                SearchingStateContent(discoveredUsers.size)
            }
            NearbyConnectionStatus.ERROR -> {
                ErrorStateContent(discoveryState.error ?: "알 수 없는 오류")
            }
            else -> {
                EmptyStateContent()
            }
        }
        
        // User List
        if (discoveredUsers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            UserListSection(
                users = discoveredUsers,
                onUserSelect = onUserSelect
            )
        }
    }
}

@Composable
private fun SearchControlSection(
    discoveryState: NearbyDiscoveryState,
    onStartSearch: () -> Unit,
    onStopSearch: () -> Unit
) {
    val isSearching = discoveryState.isSearching
    
    Button(
        onClick = if (isSearching) onStopSearch else onStartSearch,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSearching) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
            contentDescription = if (isSearching) "검색 중지" else "검색 시작"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isSearching) "검색 중지" else "주변 기기 검색"
        )
    }
}

@Composable
private fun EmptyStateContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "검색",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "주변에 있는 다른 사용자를 찾아보세요",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bluetooth와 Wi-Fi가 켜져 있어야 합니다",
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun SearchingStateContent(userCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "주변 기기를 검색하는 중...",
            textAlign = TextAlign.Center
        )
        if (userCount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${userCount}명 발견됨",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ErrorStateContent(errorMessage: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "오류",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "검색 중 오류가 발생했습니다",
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun UserListSection(
    users: List<NearbyUser>,
    onUserSelect: (NearbyUser) -> Unit
) {
    Column {
        Text(
            text = "발견된 사용자 (${users.size}명)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users) { user ->
                NearbyUserItem(
                    user = user,
                    onUserSelect = onUserSelect
                )
            }
        }
    }
}

@Composable
private fun NearbyUserItem(
    user: NearbyUser,
    onUserSelect: (NearbyUser) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = { onUserSelect(user) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "사용자",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.userProfile.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${user.userProfile.department} • ${user.userProfile.studentNumber}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // Distance indicator
            Text(
                text = user.distance ?: "근거리",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}