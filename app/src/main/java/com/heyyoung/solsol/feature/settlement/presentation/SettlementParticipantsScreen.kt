package com.heyyoung.solsol.feature.settlement.presentation

import android.util.Log
import com.heyyoung.solsol.feature.settlement.domain.model.Person
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.feature.settlement.domain.model.toPerson
import com.heyyoung.solsol.feature.settlement.presentation.components.NearbyBottomSheet
import com.heyyoung.solsol.feature.settlement.presentation.viewmodel.NearbyViewModel

private const val TAG = "SettlementParticipantsScreen"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementParticipantsScreen(
    onNavigateBack: () -> Unit = {},
    onNext: (List<Person>) -> Unit = {},
    viewModel: SettlementParticipantsViewModel = hiltViewModel(),
    nearbyViewModel: NearbyViewModel = hiltViewModel()
) {
    // ViewModel 상태 관리
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    // Nearby 상태 관리
    val nearbyDiscoveryState by nearbyViewModel.discoveryState.collectAsState()
    val nearbyUsers by nearbyViewModel.discoveredUsers.collectAsState()
    val isNearbyBottomSheetVisible by nearbyViewModel.isBottomSheetVisible.collectAsState()
    
    // 로컬 상태 관리
    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("학번") } // "학번" 또는 "학과"
    var participants by remember { mutableStateOf<List<Person>>(emptyList()) }

    // 현재 사용자 정보 로드
    LaunchedEffect(Unit) {
        viewModel.loadCurrentUser()
        nearbyViewModel.initialize()
    }

    // 현재 사용자가 로드되면 participants 초기화
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (participants.isEmpty()) {
                participants = listOf(user)
                Log.d(TAG, "현재 사용자로 participants 초기화: ${user.name}")
            }
        }
    }

    // 중복 참여자 필터링을 위한 참여자 ID 세트
    val participantIds = participants.map { it.id }.toSet()
    
    // 검색 결과에서 이미 추가된 참여자 제외
    val filteredSearchResults = searchResults.filter { user ->
        !participantIds.contains(user.id)
    }

    Log.d(TAG, "사람 추가 화면 진입 - 현재 참여자 ${participants.size}명")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("정산하기") },
            navigationIcon = {
                IconButton(onClick = {
                    Log.d(TAG, "뒤로가기 클릭")
                    onNavigateBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1C1C1E),
                navigationIconContentColor = Color(0xFF1C1C1E)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 제목
            Text(
                text = "정산할 사람을 추가해주세요",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 검색창과 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { newValue ->
                        // 학번 탭일 때는 숫자만 입력 허용
                        val filteredValue = if (selectedTab == "학번") {
                            newValue.filter { it.isDigit() }
                        } else {
                            newValue
                        }
                        searchText = filteredValue
                        
                        // 입력이 비워지면 검색 결과 초기화
                        if (filteredValue.isBlank()) {
                            viewModel.clearSearchResults()
                        }
                        
                        Log.d(TAG, "검색어 입력: '$filteredValue' (탭: $selectedTab)")
                    },
                    placeholder = {
                        Text(
                            if (selectedTab == "학번") "학번을 입력하세요"
                            else "학과명을 입력하세요"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "검색",
                            tint = Color(0xFF8B5FBF)
                        )
                    },
                    trailingIcon = if (searchText.isNotEmpty()) {
                        {
                            IconButton(onClick = {
                                searchText = ""
                                viewModel.clearSearchResults()
                                Log.d(TAG, "검색어 초기화")
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "검색어 지우기",
                                    tint = Color(0xFF999999)
                                )
                            }
                        }
                    } else null,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5FBF),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true
                )

                // 검색 버튼
                Button(
                    onClick = {
                        if (searchText.isNotBlank()) {
                            Log.d(TAG, "🔍 검색 버튼 클릭: '$searchText' ($selectedTab)")
                            viewModel.searchUsers(searchText.trim())
                        }
                    },
                    enabled = searchText.isNotBlank() && !uiState.isSearching,
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5FBF),
                        disabledContainerColor = Color(0xFF8B5FBF).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "검색",
                            tint = Color.White
                        )
                    }
                }
                
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 학번/학과 탭
            TabSection(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    Log.d(TAG, "탭 선택: $tab (이전: $selectedTab)")
                    selectedTab = tab
                    // 탭 변경 시 검색어 및 검색 결과 초기화
                    if (searchText.isNotEmpty()) {
                        searchText = ""
                        viewModel.clearSearchResults()
                        Log.d(TAG, "탭 변경으로 검색어 초기화")
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 검색 결과 또는 참여자 리스트
            Box(
                modifier = Modifier.weight(1f)
            ) {
                // 오류 메시지 표시
                if (uiState.searchError != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ ${uiState.searchError}",
                            color = Color(0xFFE53E3E),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("다시 시도", color = Color(0xFF8B5FBF))
                        }
                    }
                } else if (filteredSearchResults.isNotEmpty()) {
                    // API 검색 결과 표시
                    SearchResultsSection(
                        searchResults = filteredSearchResults,
                        onAddPerson = { person ->
                            if (participants.size < 10) {
                                Log.d(TAG, "✅ 사용자 추가: ${person.name} (${participants.size + 1}/10)")
                                participants = participants + person
                                searchText = ""
                                viewModel.clearSearchResults()
                            } else {
                                Log.w(TAG, "❌ 최대 인원(10명) 초과로 추가 불가")
                            }
                        },
                        isMaxReached = participants.size >= 10
                    )
                } else if (currentUser != null) {
                    // 기존 참여자 리스트 + 추가 버튼
                    ParticipantsSection(
                        participants = participants,
                        onRemoveParticipant = { person ->
                            Log.d(TAG, "참여자 제거: ${person.name}")
                            participants = participants.filter { it.id != person.id }
                        },
                        onAddPersonClick = {
                            if (participants.size < 10) {
                                Log.d(TAG, "💡 + 사람 추가하기 클릭 - 주변 기기 검색 시작")
                                nearbyViewModel.showBottomSheet()
                            } else {
                                Log.d(TAG, "❌ 최대 인원(10명)으로 추가 불가")
                            }
                        },
                        isMaxReached = participants.size >= 10
                    )
                } else {
                    // 현재 사용자 정보 로딩 중
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF8B5FBF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "사용자 정보를 불러오는 중...",
                                color = Color(0xFF666666),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 하단 총 인원 + 다음 버튼
            Column {
                Text(
                    text = "총 ${participants.size}명",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1C1C1E),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "최대 10명까지 추가 가능해요",
                    fontSize = 14.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 다음 버튼
                Button(
                    onClick = {
                        Log.d(TAG, "다음 버튼 클릭 - 총 ${participants.size}명")
                        onNext(participants)
                    },
                    enabled = participants.size >= 2, // 최소 2명 이상
                    modifier = Modifier
                        .shadow(
                            elevation = 8.dp,
                            spotColor = Color(0x26000000),
                            ambientColor = Color(0x26000000)
                        )
                        .width(342.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xE58B5FBF),
                        disabledContainerColor = Color(0x4D8B5FBF)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "다음",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
    
    // Nearby Bottom Sheet
    if (isNearbyBottomSheetVisible) {
        NearbyBottomSheet(
            discoveryState = nearbyDiscoveryState,
            discoveredUsers = nearbyUsers,
            onStartSearch = {
                Log.d(TAG, "주변 기기 검색 시작")
                nearbyViewModel.startNearbySearch()
            },
            onStopSearch = {
                Log.d(TAG, "주변 기기 검색 중지")
                nearbyViewModel.stopNearbySearch()
            },
            onUserSelect = { nearbyUser ->
                if (participants.size < 10) {
                    val person = nearbyUser.userProfile.toPerson()
                    // 중복 확인
                    if (!participants.any { it.id == person.id }) {
                        participants = participants + person
                        Log.d(TAG, "✅ 주변 사용자 추가: ${person.name} (${participants.size}/10)")
                    } else {
                        Log.d(TAG, "이미 추가된 사용자: ${person.name}")
                    }
                } else {
                    Log.w(TAG, "❌ 최대 인원(10명) 초과로 추가 불가")
                }
            },
            onCloseSheet = {
                Log.d(TAG, "주변 기기 검색 바텀시트 닫기")
                nearbyViewModel.hideBottomSheet()
            }
        )
    }
}

@Composable
private fun SearchResultsSection(
    searchResults: List<Person>,
    onAddPerson: (Person) -> Unit,
    isMaxReached: Boolean = false
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (searchResults.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "검색 결과가 없어요",
                        fontSize = 16.sp,
                        color = Color(0xFF999999)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "다른 키워드로 검색해보세요",
                        fontSize = 14.sp,
                        color = Color(0xFFCCCCCC)
                    )
                }
            }
        } else {
            items(searchResults) { person ->
                SearchResultCard(
                    person = person,
                    onAdd = { onAddPerson(person) },
                    isMaxReached = isMaxReached
                )
            }

            // 최대 인원 도달 시 안내 메시지
            if (isMaxReached) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3CD)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚠️ 최대 10명까지만 추가할 수 있어요",
                            fontSize = 14.sp,
                            color = Color(0xFF856404),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantsSection(
    participants: List<Person>,
    onRemoveParticipant: (Person) -> Unit,
    onAddPersonClick: () -> Unit,
    isMaxReached: Boolean = false
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(participants) { person ->
            ParticipantCard(
                person = person,
                onRemove = if (!person.isMe) {
                    { onRemoveParticipant(person) }
                } else null
            )
        }

        // + 사람 추가하기 버튼
        item {
            AddPersonButton(
                onClick = onAddPersonClick,
                isMaxReached = isMaxReached
            )
        }
    }
}

@Composable
private fun SearchResultCard(
    person: Person,
    onAdd: () -> Unit,
    isMaxReached: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 아이콘
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE0E0E0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.name.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 이름 + 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = person.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
                Text(
                    text = "${person.department} · ${person.studentId}",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }

            // 추가 버튼
            Button(
                onClick = onAdd,
                enabled = !isMaxReached,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5FBF),
                    disabledContainerColor = Color(0xFFCCCCCC)
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(32.dp)
                    .defaultMinSize(minWidth = 72.dp)
            ) {
                Text(
                    text = if (isMaxReached) "최대" else "추가",
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isMaxReached) Color(0xFF999999) else Color.White
                )
            }
        }
    }
}

@Composable
private fun TabSection(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("학번", "학과").forEach { tab ->
            Button(
                onClick = { onTabSelected(tab) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == tab) Color(0xFF8B5FBF) else Color.Transparent,
                    contentColor = if (selectedTab == tab) Color.White else Color(0xFF666666)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = tab,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ParticipantCard(
    person: Person,
    onRemove: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (person.isMe) Color(0xFFE8F5E8) else Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 아이콘
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE0E0E0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.name.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 이름 + 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = person.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (person.isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(나)",
                            fontSize = 14.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                Text(
                    text = "${person.department} · ${person.studentId}",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }

            // 제거 버튼 또는 초록색 버튼
            if (person.isMe) {
                RoleChip(text = "총무")
            } else if (onRemove != null) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "제거",
                        tint = Color(0xFFB0B0B0), // 살짝 옅은 회색
                        modifier = Modifier.size(20.dp)
                    )
                }
            }


        }
    }
}

@Composable
private fun AddPersonButton(
    onClick: () -> Unit,
    isMaxReached: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isMaxReached) { onClick() }
            .border(
                width = 2.dp,
                color = if (isMaxReached) Color(0xFFCCCCCC) else Color(0xFF8B5FBF),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "주변에서 찾기",
                tint = if (isMaxReached) Color(0xFFCCCCCC) else Color(0xFF8B5FBF),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isMaxReached) "최대 10명 도달" else "주변에서 찾기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isMaxReached) Color(0xFFCCCCCC) else Color(0xFF8B5FBF)
            )
        }
    }
}

@Composable
private fun RoleChip(text: String) {
    Surface(
        color = Color(0xFFEDE4FF), // 연보라 배경
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF8B5FBF)),
        shadowElevation = 0.dp
    ) {
        Text(
            text = text,
            color = Color(0xFF8B5FBF),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
