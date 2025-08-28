package com.heyyoung.solsol.feature.settlement.presentation.game

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyyoung.solsol.feature.settlement.domain.game.GameViewModel
import com.heyyoung.solsol.feature.settlement.domain.game.Phase
import com.heyyoung.solsol.feature.settlement.domain.game.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToRoom: () -> Unit = {},
    viewModel: GameViewModel = viewModel()
) {
    var roomTitle by remember { mutableStateOf("") }
    var roomAmountText by remember { mutableStateOf("") }

    val role by viewModel.role.collectAsState()
    val isAdvertising by viewModel.nearby.isAdvertising.collectAsState()
    val roomState by viewModel.roomState.collectAsState()

    val context = LocalContext.current

    /** 요청할 권한 목록: OS 버전에 맞춰 구성 */
    val requiredPerms = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            buildList {
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // (선택) Wi-Fi 근거리 탐색을 사용할 경우
                    add(Manifest.permission.NEARBY_WIFI_DEVICES)
                } else {
                    // Android 10~11 스캔 호환
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                }
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
        val amount = roomAmountText.toLongOrNull()
        if (granted && !roomTitle.isBlank() && amount != null && amount > 0L) {
            viewModel.createRoom(roomTitle.trim(), amount)        // ✅ 변경
        } else if (!granted) {
            Toast.makeText(context, "근거리 연결 권한을 모두 허용해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    // 광고 시작되거나 방 단계가 GATHERING이면 룸 화면으로 이동
    LaunchedEffect(isAdvertising, role, roomState?.phase) {
        if (role == Role.HOST && (isAdvertising || roomState?.phase == Phase.GATHERING)) {
            onNavigateToRoom()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CenterAlignedTopAppBar(
            title = { Text("방 만들기") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(text = "🌐", fontSize = 60.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "게임방 설정",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "방 제목을 입력해주세요",
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = roomTitle,
                onValueChange = { roomTitle = it },
                label = { Text("방 제목") },
                placeholder = { Text("예: 친구들과 저녁 정산") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5FBF),
                    focusedLabelColor = Color(0xFF8B5FBF),
                    cursorColor = Color(0xFF8B5FBF)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            // ✅ 정산 금액 입력 (숫자만)
            OutlinedTextField(
                value = roomAmountText,
                onValueChange = { input -> roomAmountText = input.filter { it.isDigit() } },
                label = { Text("정산 금액 (원)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "💡 호스트 안내",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E40AF)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• 방을 만들면 주변 기기에서 검색할 수 있습니다",
                        fontSize = 14.sp, color = Color(0xFF1E40AF), lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 참가자가 모이면 게임을 시작할 수 있습니다",
                        fontSize = 14.sp, color = Color(0xFF1E40AF), lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 게임 진행과 결과를 관리합니다",
                        fontSize = 14.sp, color = Color(0xFF1E40AF), lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amount = roomAmountText.toLongOrNull()
                    if (amount == null || amount <= 0L || roomTitle.isBlank()) {
                        Toast.makeText(context, "제목과 금액을 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (hasAllPermissions()) {
                        viewModel.createRoom(roomTitle.trim(), amount)    // ✅ 변경
                    } else {
                        permissionLauncher.launch(requiredPerms)
                    }
                },
                enabled = roomTitle.isNotBlank() && !isAdvertising,
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        spotColor = Color(0x26000000),
                        ambientColor = Color(0x26000000)
                    )
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5FBF),
                    disabledContainerColor = Color(0x4D8B5FBF)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isAdvertising) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "방 생성 중...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "방 만들기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
