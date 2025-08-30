package com.heyyoung.solsol.feature.settlement.presentation.game

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyyoung.solsol.R
import com.heyyoung.solsol.feature.settlement.domain.game.GameViewModel
import com.heyyoung.solsol.feature.settlement.domain.game.Phase
import com.heyyoung.solsol.feature.settlement.domain.game.Role
import com.heyyoung.solsol.ui.theme.OneShinhan
import kotlinx.coroutines.delay

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

    // 애니메이션 상태
    var isVisible by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val backgroundOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "backgroundOffset"
    )

    // OS 버전에 맞춰 필요한 권한 구성
    val requiredPerms = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            buildList {
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.NEARBY_WIFI_DEVICES)
                } else {
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

    // 진입 애니메이션
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5FBF).copy(alpha = 0.1f),
                        Color(0xFF6366F1).copy(alpha = 0.05f),
                        Color(0xFFF8FAFF),
                        Color(0xFFFFFFFF)
                    ),
                    center = androidx.compose.ui.geometry.Offset(
                        x = 300f + backgroundOffset * 2,
                        y = 200f + backgroundOffset
                    ),
                    radius = 800f
                )
            )
    ) {
        // 배경 장식 요소들
        FloatingElements(backgroundOffset)

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
                            "방 만들기",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // 스크롤 가능한 컨텐츠
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Sol 로고와 애니메이션
                AnimatedVisibility(
                    visible = isVisible,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn()
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .shadow(
                                elevation = 24.dp,
                                shape = RoundedCornerShape(90.dp),
                                spotColor = Color(0xFF8B5FBF).copy(alpha = 0.2f)
                            )
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color(0xFFF8FAFF)
                                    )
                                ),
                                shape = RoundedCornerShape(90.dp)
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF8B5FBF).copy(alpha = 0.3f),
                                        Color(0xFF6366F1).copy(alpha = 0.2f)
                                    )
                                ),
                                shape = RoundedCornerShape(90.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sol),
                            contentDescription = "Sol 로고",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 제목과 설명
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically { 50 } + fadeIn()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "게임방 설정",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = OneShinhan,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "친구들과 함께할 재미있는 정산 게임을\n시작해보세요!",
                            fontSize = 16.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // 모던한 입력 필드들
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically { 100 } + fadeIn()
                ) {
                    Column {
                        GlassmorphicTextField(
                            value = roomTitle,
                            onValueChange = { roomTitle = it },
                            label = "방 제목",
                            placeholder = "예: 오늘 누가 커피 쏠까?",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        GlassmorphicTextField(
                            value = roomAmountText,
                            onValueChange = { input -> roomAmountText = input.filter { it.isDigit() } },
                            label = "정산 금액",
                            placeholder = "금액을 입력해주세요.",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            inlineSuffix = if (roomAmountText.isNotBlank()) "원" else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 트렌디한 안내 카드
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically { 100 } + fadeIn()
                ) {
                    GlassmorphicInfoCard()
                }

                Spacer(modifier = Modifier.height(40.dp))

                // 트렌디한 버튼
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically { 100 } + fadeIn()
                ) {
                    GlassmorphicButton(
                        text = if (isAdvertising) "방 생성 중..." else "방 만들기",
                        isLoading = isAdvertising,
                        enabled = roomTitle.isNotBlank() && !isAdvertising,
                        onClick = {
                            val amount = roomAmountText.toLongOrNull()
                            if (amount == null || amount <= 0L || roomTitle.isBlank()) {
                                Toast.makeText(context, "제목과 금액을 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                                return@GlassmorphicButton
                            }
                            if (hasAllPermissions()) {
                                viewModel.createRoom(roomTitle.trim(), amount)
                            } else {
                                permissionLauncher.launch(requiredPerms)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
private fun FloatingElements(offset: Float) {
    // 배경 장식 원들
    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(1.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = 50.dp, y = (100 + offset / 10).dp)
                .rotate(offset)
                .background(
                    Color(0xFF8B5FBF).copy(alpha = 0.1f),
                    RoundedCornerShape(50.dp)
                )
        )

        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(x = 300.dp, y = (200 - offset / 15).dp)
                .rotate(-offset)
                .background(
                    Color(0xFF6366F1).copy(alpha = 0.08f),
                    RoundedCornerShape(30.dp)
                )
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 20.dp, y = (400 + offset / 8).dp)
                .rotate(offset / 2)
                .background(
                    Color(0xFFEC4899).copy(alpha = 0.06f),
                    RoundedCornerShape(40.dp)
                )
        )
    }
}

@Composable
private fun GlassmorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    suffix: String? = null,
    inlineSuffix: String? = null // ✅ 추가
) {
    Column {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF374151),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Box {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(placeholder, color = Color(0xFFA0AEC0), fontSize = 16.sp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0xFF8B5FBF).copy(alpha = 0.1f)
                    ),
                keyboardOptions = keyboardOptions,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5FBF),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                    cursorColor = Color(0xFF8B5FBF)
                ),
                shape = RoundedCornerShape(20.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                // ✅ 값이 있을 때만 " 원"을 인라인으로 덧붙임
                visualTransformation = if (!value.isNullOrEmpty() && inlineSuffix != null)
                    SuffixVisualTransformation(inlineSuffix)
                else VisualTransformation.None
            )

            // 기존 바깥쪽 suffix는 인라인을 쓰지 않는 경우에만 표시
            if (inlineSuffix == null) {
                suffix?.let {
                    Text(
                        text = it,
                        color = Color(0xFF64748B),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                    )
                }
            }
        }
    }
}

private class SuffixVisualTransformation(
    private val suffix: String
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (text.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val builder = AnnotatedString.Builder()
        builder.append(text)
        builder.pushStyle(SpanStyle(color = Color(0xFF94A3B8))) // 살짝 옅은 색
        builder.append(" $suffix")
        builder.pop()
        val out = builder.toAnnotatedString()

        val originalLength = text.length
        val transformedLength = out.length
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // 원본 커서는 그대로 매핑, 접미사가 붙은 뒤 구간은 자동으로 보호
                return offset.coerceAtMost(originalLength)
            }
            override fun transformedToOriginal(offset: Int): Int {
                // 접미사 영역 클릭 시에도 원본 끝으로 매핑
                return offset.coerceAtMost(originalLength)
            }
        }
        return TransformedText(out, offsetMapping)
    }
}


@Composable
private fun GlassmorphicInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFF8B5FBF).copy(alpha = 0.15f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box {
            // 글래스모피즘 효과를 위한 배경
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color(0xFF8B5FBF).copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            )

            Column(
                modifier = Modifier.padding(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF8B5FBF).copy(alpha = 0.2f),
                                        Color(0xFF6366F1).copy(alpha = 0.1f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF8B5FBF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "호스트 가이드",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        fontFamily = OneShinhan
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                InfoItem(
                    icon = Icons.Default.Star,
                    text = "방을 만들면 주변 기기에서 검색할 수 있어요"
                )
                Spacer(modifier = Modifier.height(16.dp))
                InfoItem(
                    icon = Icons.Default.Star,
                    text = "참가자들이 모이면 재미있는 게임을 시작해요"
                )
                Spacer(modifier = Modifier.height(16.dp))
                InfoItem(
                    icon = Icons.Default.Star,
                    text = "게임 진행과 최종 결과를 관리합니다"
                )
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    Color(0xFF8B5FBF).copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF8B5FBF),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color(0xFF475569),
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GlassmorphicButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .scale(scale)
            .shadow(
                elevation = if (enabled) 20.dp else 8.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color(0xFF8B5FBF).copy(alpha = 0.4f)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(32.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled) {
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
                    shape = RoundedCornerShape(32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = OneShinhan
                )
            }
        }
    }
}