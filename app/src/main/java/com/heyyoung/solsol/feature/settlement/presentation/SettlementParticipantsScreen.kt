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
import androidx.compose.material.icons.filled.Person
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
        // 상단 앱바 - 더 깔끔하게
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "정산하기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748) // solsol_dark_text
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    Log.d(TAG, "뒤로가기 클릭")
                    onNavigateBack()
                }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "뒤로",
                        tint = Color(0xFF2D3748) // solsol_dark_text
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF2D3748), // solsol_dark_text
                navigationIconContentColor = Color(0xFF2D3748) // solsol_dark_text
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // 20dp에서 16dp로 조정

            // 제목 - 더 트렌디하게
            Column {
                Text(
                    text = "정산할 사람을 추가해주세요",
                    fontSize = 22.sp, // 20sp에서 22sp로 증가
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748), // solsol_dark_text
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "최대 10명까지 함께 정산할 수 있어요",
                    fontSize = 14.sp,
                    color = Color(0xFF718096), // solsol_gray_text
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(28.dp)) // 24dp에서 28dp로 증가

            // 검색창과 버튼들 - 돋보기 중복 문제 해결
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
                            else "학과명을 입력하세요",
                            color = Color(0xFF718096) // solsol_gray_text
                        )
                    },
                    // 검색창 leadingIcon을 Person 아이콘으로 변경 (돋보기 중복 해결)
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "사용자",
                            tint = Color(0xFF8B5FBF) // solsol_purple
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
                                    tint = Color(0xFF718096) // solsol_gray_text
                                )
                            }
                        }
                    } else null,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp), // 12dp에서 16dp로 증가
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5FBF), // solsol_purple
                        unfocusedBorderColor = Color(0xFFE2E8F0) // solsol_light_gray
                    ),
                    singleLine = true
                )

                // 검색 버튼 - 더 트렌디한 디자인
                Button(
                    onClick = {
                        if (searchText.isNotBlank()) {
                            Log.d(TAG, "검색 버튼 클릭: '$searchText' ($selectedTab)")
                            viewModel.searchUsers(searchText.trim())
                        }
                    },
                    enabled = searchText.isNotBlank() && !uiState.isSearching,
                    modifier = Modifier
                        .height(56.dp)
                        .shadow(
                            elevation = 8.dp,
                            spotColor = Color(0x1A8B5FBF),
                            ambientColor = Color(0x1A8B5FBF)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5FBF), // solsol_purple
                        disabledContainerColor = Color(0xFF8B5FBF).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp) // 12dp에서 16dp로 증가
                ) {
                    if (uiState.isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFFFFFFFF), // solsol_white
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "검색",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFFFFF) // solsol_white
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp)) // 16dp에서 20dp로 증가

            // 학번/학과 탭 - 더 트렌디하게
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

            Spacer(modifier = Modifier.height(24.dp)) // 20dp에서 24dp로 증가

            // 검색 결과 또는 참여자 리스트
            Box(
                modifier = Modifier.weight(1f)
            ) {
                // 오류 메시지 표시 - 더 깔끔하게
                if (uiState.searchError != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = Color(0xFFFF6B6B).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = Color(0xFFFF6B6B),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.searchError!!,
                            color = Color(0xFF2D3748), // solsol_dark_text
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = { viewModel.clearError() },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF8B5FBF) // solsol_purple
                            )
                        ) {
                            Text(
                                "다시 시도",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else if (filteredSearchResults.isNotEmpty()) {
                    // API 검색 결과 표시
                    SearchResultsSection(
                        searchResults = filteredSearchResults,
                        onAddPerson = { person ->
                            if (participants.size < 10) {
                                Log.d(TAG, "사용자 추가: ${person.name} (${participants.size + 1}/10)")
                                participants = participants + person
                                searchText = ""
                                viewModel.clearSearchResults()
                            } else {
                                Log.w(TAG, "최대 인원(10명) 초과로 추가 불가")
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
                                Log.d(TAG, "+ 사람 추가하기 클릭 - 주변 기기 검색 시작")
                                nearbyViewModel.showBottomSheet()
                            } else {
                                Log.d(TAG, "최대 인원(10명)으로 추가 불가")
                            }
                        },
                        isMaxReached = participants.size >= 10
                    )
                } else {
                    // 현재 사용자 정보 로딩 중 - 더 트렌디하게
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF8B5FBF), // solsol_purple
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "사용자 정보를 불러오는 중...",
                                color = Color(0xFF718096), // solsol_gray_text
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp)) // 16dp에서 20dp로 증가

            // 하단 총 인원 + 다음 버튼 - 더 깔끔하게
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "총 ${participants.size}명",
                            fontSize = 18.sp, // 16sp에서 18sp로 증가
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748) // solsol_dark_text
                        )
                        Text(
                            text = "최소 2명 이상 필요해요",
                            fontSize = 13.sp, // 14sp에서 13sp로 조정
                            color = Color(0xFF718096), // solsol_gray_text
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // 진행률 표시기
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFF8B5FBF).copy(alpha = 0.1f), // solsol_purple with transparency
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${participants.size}/10",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5FBF) // solsol_purple
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 다음 버튼 - 더 트렌디하게
                Button(
                    onClick = {
                        Log.d(TAG, "다음 버튼 클릭 - 총 ${participants.size}명")
                        onNext(participants)
                    },
                    enabled = participants.size >= 2, // 최소 2명 이상
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 16.dp, // 8dp에서 16dp로 증가
                            spotColor = Color(0x308B5FBF),
                            ambientColor = Color(0x308B5FBF)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5FBF), // solsol_purple
                        disabledContainerColor = Color(0xFF8B5FBF).copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp) // 28dp에서 16dp로 조정 (더 모던한 느낌)
                ) {
                    Text(
                        text = "다음",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF) // solsol_white
                    )
                }

                Spacer(modifier = Modifier.height(32.dp)) // 40dp에서 32dp로 조정
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
                        Log.d(TAG, "주변 사용자 추가: ${person.name} (${participants.size}/10)")
                    } else {
                        Log.d(TAG, "이미 추가된 사용자: ${person.name}")
                    }
                } else {
                    Log.w(TAG, "최대 인원(10명) 초과로 추가 불가")
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
        verticalArrangement = Arrangement.spacedBy(16.dp) // 12dp에서 16dp로 증가
    ) {
        if (searchResults.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp), // 40dp에서 60dp로 증가
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = Color(0xFF8B5FBF).copy(alpha = 0.1f), // solsol_purple with transparency
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFF8B5FBF) // solsol_purple
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "검색 결과가 없어요",
                        fontSize = 18.sp, // 16sp에서 18sp로 증가
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748) // solsol_dark_text
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "다른 키워드로 검색해보세요",
                        fontSize = 14.sp,
                        color = Color(0xFF718096) // solsol_gray_text
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

            // 최대 인원 도달 시 안내 메시지 - 더 트렌디하게
            if (isMaxReached) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFFF6B6B).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFFFF6B6B).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = Color(0xFFFF6B6B).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = Color(0xFFFF6B6B),
                                            shape = RoundedCornerShape(3.dp)
                                        )
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "최대 10명까지만 추가할 수 있어요",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFF6B6B)
                            )
                        }
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
        verticalArrangement = Arrangement.spacedBy(16.dp) // 12dp에서 16dp로 증가
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
                elevation = 8.dp, // 2dp에서 8dp로 증가
                spotColor = Color(0x1A8B5FBF),
                ambientColor = Color(0x1A8B5FBF)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF) // solsol_card_white
        ),
        shape = RoundedCornerShape(16.dp) // 12dp에서 16dp로 증가
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // 16dp에서 20dp로 증가
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 아이콘 - 더 트렌디하게
            Box(
                modifier = Modifier
                    .size(48.dp) // 40dp에서 48dp로 증가
                    .background(
                        color = Color(0xFF8B5FBF).copy(alpha = 0.1f), // solsol_purple with transparency
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.name.first().toString(),
                    color = Color(0xFF8B5FBF), // solsol_purple
                    fontSize = 18.sp, // 크기 증가
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp)) // 12dp에서 16dp로 증가

            // 이름 + 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = person.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748) // solsol_dark_text
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${person.department} · ${person.studentId}",
                    fontSize = 14.sp,
                    color = Color(0xFF718096) // solsol_gray_text
                )
            }

            // 추가 버튼 - 더 트렌디하게
            Button(
                onClick = onAdd,
                enabled = !isMaxReached,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5FBF), // solsol_purple
                    disabledContainerColor = Color(0xFF718096).copy(alpha = 0.3f) // solsol_gray_text with transparency
                ),
                shape = RoundedCornerShape(12.dp), // 16dp에서 12dp로 조정
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), // 패딩 조정
                modifier = Modifier.height(36.dp) // 32dp에서 36dp로 증가
            ) {
                Text(
                    text = if (isMaxReached) "최대" else "추가",
                    fontSize = 13.sp, // 12sp에서 13sp로 증가
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isMaxReached) Color(0xFF718096) else Color(0xFFFFFFFF) // solsol_gray_text or solsol_white
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
        horizontalArrangement = Arrangement.spacedBy(12.dp) // 8dp에서 12dp로 증가
    ) {
        listOf("학번", "학과").forEach { tab ->
            Button(
                onClick = { onTabSelected(tab) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == tab) Color(0xFF8B5FBF) else Color.Transparent, // solsol_purple
                    contentColor = if (selectedTab == tab) Color(0xFFFFFFFF) else Color(0xFF718096) // solsol_white or solsol_gray_text
                ),
                shape = RoundedCornerShape(16.dp), // 20dp에서 16dp로 조정
                modifier = Modifier
                    .height(40.dp) // 36dp에서 40dp로 증가
                    .shadow(
                        elevation = if (selectedTab == tab) 4.dp else 0.dp,
                        spotColor = Color(0x1A8B5FBF),
                        ambientColor = Color(0x1A8B5FBF)
                    )
            ) {
                Text(
                    text = tab,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold // Medium에서 SemiBold로 변경
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
                elevation = 8.dp, // 2dp에서 8dp로 증가
                spotColor = Color(0x1A8B5FBF),
                ambientColor = Color(0x1A8B5FBF)
            ),
        colors = CardDefaults.cardColors(
            // "나" 구분 색상을 연보라색으로 변경
            containerColor = if (person.isMe) Color(0xFFF3EFFF) else Color(0xFFFFFFFF) // 연보라색 or solsol_card_white
        ),
        shape = RoundedCornerShape(16.dp) // 12dp에서 16dp로 증가
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // 16dp에서 20dp로 증가
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 아이콘 - 더 트렌디하게
            Box(
                modifier = Modifier
                    .size(48.dp) // 40dp에서 48dp로 증가
                    .background(
                        color = if (person.isMe)
                            Color(0xFF8B5FBF).copy(alpha = 0.15f) // "나"일 때 더 진한 보라색
                        else
                            Color(0xFF8B5FBF).copy(alpha = 0.1f), // solsol_purple with transparency
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.name.first().toString(),
                    color = Color(0xFF8B5FBF), // solsol_purple
                    fontSize = 18.sp, // 크기 증가
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp)) // 12dp에서 16dp로 증가

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
                        color = Color(0xFF2D3748), // solsol_dark_text
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (person.isMe) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(나)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8B5FBF) // 연보라색으로 변경
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${person.department} · ${person.studentId}",
                    fontSize = 14.sp,
                    color = Color(0xFF718096) // solsol_gray_text
                )
            }

            // 제거 버튼 또는 총무 칩
            if (person.isMe) {
                RoleChip(text = "총무")
            } else if (onRemove != null) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp) // 32dp에서 36dp로 증가
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "제거",
                        tint = Color(0xFF718096), // solsol_gray_text
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
                color = if (isMaxReached) Color(0xFFE2E8F0) else Color(0xFF8B5FBF), // solsol_light_gray or solsol_purple
                shape = RoundedCornerShape(16.dp) // 12dp에서 16dp로 증가
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp) // 12dp에서 16dp로 증가
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // 16dp에서 20dp로 증가
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "주변에서 찾기",
                tint = if (isMaxReached) Color(0xFF718096) else Color(0xFF8B5FBF), // solsol_gray_text or solsol_purple
                modifier = Modifier.size(24.dp) // 20dp에서 24dp로 증가
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isMaxReached) "최대 10명 도달" else "주변에서 찾기",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold, // Medium에서 SemiBold로 변경
                color = if (isMaxReached) Color(0xFF718096) else Color(0xFF8B5FBF) // solsol_gray_text or solsol_purple
            )
        }
    }
}

@Composable
private fun RoleChip(text: String) {
    Surface(
        color = Color(0xFF8B5FBF).copy(alpha = 0.1f), // 연보라 배경으로 변경
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF8B5FBF)), // solsol_purple
        shadowElevation = 0.dp
    ) {
        Text(
            text = text,
            color = Color(0xFF8B5FBF), // solsol_purple
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp) // 패딩 증가
        )
    }
}