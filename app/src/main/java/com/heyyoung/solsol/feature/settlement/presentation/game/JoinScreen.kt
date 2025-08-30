package com.heyyoung.solsol.feature.settlement.presentation.game

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyyoung.solsol.feature.settlement.domain.game.GameViewModel
import com.heyyoung.solsol.feature.settlement.domain.game.Phase
import com.heyyoung.solsol.feature.settlement.domain.game.Role
import com.heyyoung.solsol.ui.theme.OneShinhan
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToRoom: () -> Unit = {},
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current

    // ==== State ====
    val role by viewModel.role.collectAsState()
    val isDiscovering by viewModel.nearby.isDiscovering.collectAsState()
    val discoveredRooms by viewModel.nearby.discoveredRooms.collectAsState()
    val roomState by viewModel.roomState.collectAsState()

    // 애니메이션 상태
    var isVisible by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val backgroundOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "backgroundOffset"
    )

    // ==== Permissions ====
    val requiredPerms = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            buildList {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                // Android 13+ 에서 Wi-Fi 근거리 탐색을 쓴다면:
                 add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }.toTypedArray()
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun hasAllPermissions(): Boolean =
        requiredPerms.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it }
        if (granted) {
            // ✅ 여기서는 컴포저블 호출 금지 (viewModel 호출만)
            viewModel.startDiscovering()
        } else {
            Toast.makeText(context, "근거리 연결 권한을 모두 허용해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    fun deniedPerms(): Array<String> =
        requiredPerms.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()


    if (!hasAllPermissions()) {
        Button(onClick = { permissionLauncher.launch(deniedPerms()) }) {
            Text("권한 허용")
        }
    }

    // 최초 진입: 권한 확인 후 탐색 시작
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
        if (hasAllPermissions()) {
            viewModel.startDiscovering()
        } else {
            permissionLauncher.launch(requiredPerms)
        }
    }

    // 방으로 이동 조건
    LaunchedEffect(role, roomState?.phase) {
        if (role == Role.PARTICIPANT && roomState != null && roomState?.phase != Phase.IDLE) {
            onNavigateToRoom()
        }
    }

    // 화면 떠날 때 탐색 중지
    DisposableEffect(Unit) {
        onDispose { viewModel.nearby.stopDiscovery() }
    }

    // ==== UI ====
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5FBF).copy(alpha = 0.08f),
                        Color(0xFF6366F1).copy(alpha = 0.04f),
                        Color(0xFFF8FAFF),
                        Color(0xFFFFFFFF)
                    ),
                    center = androidx.compose.ui.geometry.Offset(
                        x = 200f + backgroundOffset * 1.5f,
                        y = 300f + backgroundOffset * 0.8f
                    ),
                    radius = 600f
                )
            )
    ) {
        // 배경 장식 요소들
        FloatingSearchElements(backgroundOffset)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            // 트렌디한 탑 앱바
            CenterAlignedTopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically { -50 } + fadeIn()
                    ) {
                        Text(
                            "방 참가하기",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B),
                            fontFamily = OneShinhan
                        )
                    }
                },
                navigationIcon = {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally { -100 } + fadeIn()
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(48.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    spotColor = Color(0xFF8B5FBF).copy(alpha = 0.3f)
                                )
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.White,
                                            Color(0xFFF1F5F9)
                                        )
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "뒤로",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally { 100 } + fadeIn()
                    ) {
                        val refreshScale by animateFloatAsState(
                            targetValue = if (isDiscovering) 1.1f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "refreshScale"
                        )
                        val refreshRotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = if (isDiscovering) 360f else 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "refreshRotation"
                        )

                        IconButton(
                            onClick = {
                                viewModel.nearby.stopDiscovery()
                                if (hasAllPermissions()) {
                                    viewModel.startDiscovering()
                                } else {
                                    permissionLauncher.launch(requiredPerms)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .scale(refreshScale)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    spotColor = Color(0xFF8B5FBF).copy(alpha = 0.2f)
                                )
                                .background(
                                    if (isDiscovering) {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF8B5FBF).copy(alpha = 0.15f),
                                                Color(0xFF6366F1).copy(alpha = 0.1f)
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color.White,
                                                Color(0xFFF8FAFF)
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(24.dp)
                                )
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "새로고침",
                                tint = if (isDiscovering) Color(0xFF8B5FBF) else Color(0xFF64748B),
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(if (isDiscovering) refreshRotation else 0f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // 헤더 섹션
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically { 50 } + fadeIn()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "주변 게임방",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E293B),
                                fontFamily = OneShinhan
                            )
                            Text(
                                text = "참가하고 싶은 방을 선택해보세요",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (isDiscovering) {
                            GlowingSearchIndicator()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 방 목록
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically { 100 } + fadeIn()
                ) {
                    when {
                        discoveredRooms.isEmpty() && isDiscovering -> {
                            SearchingState()
                        }
                        discoveredRooms.isEmpty() && !isDiscovering -> {
                            EmptyState()
                        }
                        else -> {
                            val rooms = remember(discoveredRooms) {
                                discoveredRooms.entries.toList().sortedBy { it.value }
                            }
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                itemsIndexed(rooms) { index, (endpointId, roomTitle) ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = slideInVertically { 50 } + fadeIn(
                                            animationSpec = tween(
                                                durationMillis = 300,
                                                delayMillis = index * 100
                                            )
                                        )
                                    ) {
                                        ModernRoomCard(
                                            roomTitle = roomTitle,
                                            onJoinClick = { viewModel.joinRoom(endpointId) },
                                            isEnabled = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FloatingSearchElements(offset: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 80.dp, y = (150 + offset / 12).dp)
                .rotate(offset)
                .background(
                    Color(0xFF8B5FBF).copy(alpha = 0.06f),
                    RoundedCornerShape(40.dp)
                )
        )

        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(x = 320.dp, y = (250 - offset / 15).dp)
                .rotate(-offset)
                .background(
                    Color(0xFF6366F1).copy(alpha = 0.05f),
                    RoundedCornerShape(30.dp)
                )
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .offset(x = 50.dp, y = (450 + offset / 10).dp)
                .rotate(offset / 3)
                .background(
                    Color(0xFFEC4899).copy(alpha = 0.04f),
                    RoundedCornerShape(20.dp)
                )
        )
    }
}

@Composable
private fun GlowingSearchIndicator() {
    val pulseScale by animateFloatAsState(
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .scale(pulseScale)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5FBF).copy(alpha = 0.3f),
                        Color(0xFF8B5FBF).copy(alpha = 0.1f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(30.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    Color(0xFF8B5FBF).copy(alpha = 0.2f),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF8B5FBF),
                modifier = Modifier.size(20.dp),
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
private fun SearchingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(50.dp),
                        spotColor = Color(0xFF8B5FBF).copy(alpha = 0.2f)
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF8B5FBF).copy(alpha = 0.1f),
                                Color(0xFF6366F1).copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(50.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF8B5FBF),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "주변 게임방을 찾고 있습니다",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "잠시만 기다려주세요",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(50.dp),
                        spotColor = Color(0xFF94A3B8).copy(alpha = 0.2f)
                    )
                    .background(
                        Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(50.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Wifi,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "주변에 게임방이 없습니다",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "새로고침을 눌러 다시 검색해보세요",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModernRoomCard(
    roomTitle: String,
    onJoinClick: () -> Unit,
    isEnabled: Boolean
) {
    val cardScale by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFF8B5FBF).copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(24.dp),
        onClick = onJoinClick
    ) {
        Box {
            // 글래스모피즘 배경
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.4f),
                                Color(0xFF8B5FBF).copy(alpha = 0.03f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 방 아이콘
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color(0xFF8B5FBF).copy(alpha = 0.2f)
                        )
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF8B5FBF).copy(alpha = 0.15f),
                                    Color(0xFF6366F1).copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        tint = Color(0xFF8B5FBF),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = roomTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    Color(0xFF10B981),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "참가 가능",
                            fontSize = 14.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onJoinClick,
                    enabled = isEnabled,
                    modifier = Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color(0xFF8B5FBF).copy(alpha = 0.3f)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = if (isEnabled) {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF8B5FBF),
                                            Color(0xFF6366F1)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF8B5FBF).copy(alpha = 0.4f),
                                            Color(0xFF6366F1).copy(alpha = 0.4f)
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "참가",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}