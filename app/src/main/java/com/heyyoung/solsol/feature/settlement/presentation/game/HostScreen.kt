package com.heyyoung.solsol.feature.settlement.presentation.game

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyyoung.solsol.R
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

    // OS 버전에 맞춰 필요한 권한 구성
    val requiredPerms = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            buildList {
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // (선택) Wi-Fi 근거리 탐색 사용 시
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
            viewModel.createRoom(roomTitle.trim(), amount)
        } else if (!granted) {
            Toast.makeText(context, "근거리 연결 권한을 모두 허용해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    // 광고가 시작되었거나 방 단계가 GATHERING이면 룸 화면으로 이동
    LaunchedEffect(isAdvertising, role, roomState?.phase) {
        if (role == Role.HOST && (isAdvertising || roomState?.phase == Phase.GATHERING)) {
            onNavigateToRoom()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0F4FF),
                        Color(0xFFFFFFFF),
                        Color(0xFFF8FAFF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 트렌디한 탑 앱바
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "방 만들기",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = Color(0xFF8B5CF6).copy(alpha = 0.2f)
                            )
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color(0xFFFAFBFF)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "뒤로",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Sol 로고 이미지
                Image(
                    painter = painterResource(id = R.drawable.sol),
                    contentDescription = "Sol 로고",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "게임방 설정",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "방 제목과 정산 금액을 입력해주세요",
                    fontSize = 16.sp,
                    color = Color(0xFF718096)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // 모던한 입력 필드들
                ModernTextField(
                    value = roomTitle,
                    onValueChange = { roomTitle = it },
                    label = "방 제목",
                    placeholder = "예: 친구들과 저녁 정산",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(16.dp))

                ModernTextField(
                    value = roomAmountText,
                    onValueChange = { input -> roomAmountText = input.filter { it.isDigit() } },
                    label = "정산 금액 (원)",
                    placeholder = "0",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 모던한 안내 카드
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color(0xFF6366F1).copy(alpha = 0.1f)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        Color(0xFF6366F1).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "💡",
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "호스트 안내",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A202C)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoItem(text = "방을 만들면 주변 기기에서 검색할 수 있습니다")
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoItem(text = "참가자가 모이면 게임을 시작할 수 있습니다")
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoItem(text = "게임 진행과 결과를 관리합니다")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 모던한 버튼
                Button(
                    onClick = {
                        val amount = roomAmountText.toLongOrNull()
                        if (amount == null || amount <= 0L || roomTitle.isBlank()) {
                            Toast.makeText(context, "제목과 금액을 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (hasAllPermissions()) {
                            viewModel.createRoom(roomTitle.trim(), amount)
                        } else {
                            permissionLauncher.launch(requiredPerms)
                        }
                    },
                    enabled = roomTitle.isNotBlank() && !isAdvertising,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color(0xFF8B5FBF).copy(alpha = 0.3f)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5FBF),
                        disabledContainerColor = Color(0xFF8B5FBF).copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    if (isAdvertising) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "방 생성 중...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "방 만들기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4A5568),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    color = Color(0xFFA0AEC0)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = Color.Black.copy(alpha = 0.05f)
                ),
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8B5FBF),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color(0xFF8B5FBF)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun InfoItem(text: String) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    Color(0xFF6366F1),
                    shape = RoundedCornerShape(3.dp)
                )
                .padding(top = 6.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF4A5568),
            lineHeight = 20.sp
        )
    }
}