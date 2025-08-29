package com.heyyoung.solsol.feature.settlement.presentation.game

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyyoung.solsol.feature.settlement.domain.game.GameViewModel
import com.heyyoung.solsol.feature.settlement.domain.game.Phase
import com.heyyoung.solsol.feature.settlement.domain.game.Role

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

    // ==== Permissions (only non-composable operations inside callbacks) ====
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
        if (hasAllPermissions()) {
            viewModel.startDiscovering()
        } else {
            permissionLauncher.launch(requiredPerms)
        }
    }

    // 방으로 이동 조건: 참가자 역할이고 방 상태가 생성되어 페이즈가 진행 중일 때
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CenterAlignedTopAppBar(
            title = { Text("방 참가하기") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        viewModel.nearby.stopDiscovery()
                        if (hasAllPermissions()) {
                            viewModel.startDiscovering()
                        } else {
                            permissionLauncher.launch(requiredPerms)
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "새로고침",
                        tint = if (isDiscovering) Color(0xFF8B5FBF) else Color(0xFF666666)
                    )
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "주변 게임방",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
                if (isDiscovering) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = Color(0xFF8B5FBF),
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("검색 중...", fontSize = 12.sp, color = Color(0xFF666666))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                discoveredRooms.isEmpty() && isDiscovering -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF8B5FBF),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "주변 게임방을 찾고 있습니다...",
                                fontSize = 16.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
                discoveredRooms.isEmpty() && !isDiscovering -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🌐", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("주변에 게임방이 없습니다", fontSize = 16.sp, color = Color(0xFF666666))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("새로고침을 눌러 다시 검색해보세요", fontSize = 14.sp, color = Color(0xFF999999))
                        }
                    }
                }
                else -> {
                    // 정렬은 remember 블록에서 "데이터만" 가공 (컴포저블 호출 금지)
                    val rooms = remember(discoveredRooms) {
                        discoveredRooms.entries.toList().sortedBy { it.value }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(rooms) { (endpointId, roomTitle) ->
                            RoomCard(
                                roomTitle = roomTitle,
                                onJoinClick = { viewModel.joinRoom(endpointId) },
                                isEnabled = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RoomCard(
    roomTitle: String,
    onJoinClick: () -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        onClick = onJoinClick // 카드 자체를 클릭해도 참가
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF8B5FBF).copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌐", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = roomTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "참가 가능", fontSize = 12.sp, color = Color(0xFF10B981))
            }

            Button(
                onClick = onJoinClick,
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5FBF),
                    disabledContainerColor = Color(0x4D8B5FBF)
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "참가",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
