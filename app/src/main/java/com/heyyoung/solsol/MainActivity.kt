package com.heyyoung.solsol

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.heyyoung.solsol.feature.auth.domain.repository.AuthRepository
import com.heyyoung.solsol.feature.auth.presentation.login.LoginScreen
import com.heyyoung.solsol.feature.auth.presentation.signup.SignUpScreen
import com.heyyoung.solsol.feature.dutchpay.presentation.split.SplitMethodSelectionScreen
import com.heyyoung.solsol.feature.dutchpay.presentation.search.ParticipantSearchScreen
import com.heyyoung.solsol.feature.dutchpay.presentation.amount.AmountInputScreen
import com.heyyoung.solsol.feature.dutchpay.presentation.complete.PaymentCompleteScreen
import com.heyyoung.solsol.feature.dutchpay.domain.model.User
import com.heyyoung.solsol.feature.dutchpay.presentation.shared.DutchPayFlowViewModel
import com.heyyoung.solsol.feature.dutchpay.presentation.shared.SplitMethod
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.util.Log
import com.heyyoung.solsol.ui.theme.SolsolPrimary
import com.heyyoung.solsol.ui.theme.SolsolTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 솔솔 캠퍼스페이 메인 액티비티
 * - 하단 네비게이션: 학사/혜택/전체메뉴
 * - 학사 탭: 바로가기 메뉴 (정산요청만 활성화)
 * - 솔솔 브랜드 컬러 테마 적용
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolsolTheme {
                SolsolApp(authRepository = authRepository)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SolsolApp(authRepository: AuthRepository) {
    val navController = rememberNavController()
    val isLoggedIn = authRepository.isLoggedIn()
    
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "main" else "login"
    ) {
        // 인증 관련 화면들
        composable("login") {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate("signup")
                },
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("signup") {
            SignUpScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    navController.navigate("main") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }
        
        // 메인 앱 화면
        composable("main") {
            MainAppScreen(navController = navController)
        }
        
        // 1단계: 분할 방식 선택
        composable("split_method_selection") {
            val flowViewModel: DutchPayFlowViewModel = hiltViewModel()
            
            SplitMethodSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEqualSplitSelected = {
                    flowViewModel.setSplitMethod(SplitMethod.EQUAL_SPLIT)
                    navController.navigate("participant_search")
                },
                onCustomSplitSelected = {
                    flowViewModel.setSplitMethod(SplitMethod.CUSTOM_SPLIT)
                    navController.navigate("participant_search")
                },
                onRandomGameSelected = {
                    flowViewModel.setSplitMethod(SplitMethod.RANDOM_GAME)
                    navController.navigate("participant_search")
                }
            )
        }
        
        // 2단계: 참여자 검색
        composable("participant_search") {
            val flowViewModel: DutchPayFlowViewModel = hiltViewModel()
            
            ParticipantSearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onParticipantsSelected = { participants ->
                    Log.d("MainActivity", "🚀 참여자 검색에서 금액 입력으로 이동")
                    Log.d("MainActivity", "📋 받은 참여자 수: ${participants.size}")
                    participants.forEachIndexed { index, user ->
                        Log.d("MainActivity", "   [$index] ${user.name} (${user.userId})")
                    }
                    flowViewModel.setSelectedParticipants(participants)
                    navController.navigate("amount_input")
                }
            )
        }
        
        // 3단계: 금액 입력
        composable("amount_input") {
            val flowViewModel: DutchPayFlowViewModel = hiltViewModel()
            val selectedParticipants by flowViewModel.selectedParticipants.collectAsState()
            
            Log.d("MainActivity", "💰 금액 입력 화면 진입")
            Log.d("MainActivity", "📋 전달받은 참여자 수: ${selectedParticipants.size}")
            selectedParticipants.forEachIndexed { index, user ->
                Log.d("MainActivity", "   [$index] ${user.name} (${user.userId})")
            }
            
            AmountInputScreen(
                participants = selectedParticipants,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRequestPayment = { totalAmount, participants ->
                    flowViewModel.setTotalAmount(totalAmount)
                    navController.navigate("payment_complete") {
                        popUpTo("main")
                    }
                }
            )
        }
        
        // 4단계: 완료
        composable("payment_complete") {
            val flowViewModel: DutchPayFlowViewModel = hiltViewModel()
            val totalAmount by flowViewModel.totalAmount.collectAsState()
            val selectedParticipants by flowViewModel.selectedParticipants.collectAsState()
            
            PaymentCompleteScreen(
                totalAmount = totalAmount,
                participantCount = selectedParticipants.size + 1, // +1 for current user
                onNavigateToHome = {
                    flowViewModel.clearAll() // 플로우 완료 후 데이터 초기화
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(navController: androidx.navigation.NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SolSol") },
                actions = {
                    IconButton(onClick = {
                        // 간단한 로그아웃 - 로그인 화면으로 이동
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.Default.ExitToApp, 
                            contentDescription = "로그아웃",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SolsolPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = SolsolPrimary
            ) {
                NavigationBarItem(
                    icon = { },
                    label = { Text("학사") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = SolsolPrimary,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = SolsolPrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { },
                    label = { Text("혜택") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = SolsolPrimary,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = SolsolPrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { },
                    label = { Text("전체 메뉴") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = SolsolPrimary,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = SolsolPrimary.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> AcademicScreen(
                    onNavigateToCreateDutchPay = {
                        navController.navigate("split_method_selection")
                    }
                )
                1 -> BenefitScreen()
                2 -> MenuScreen()
            }
        }
    }
}

@Composable
fun AcademicScreen(
    onNavigateToCreateDutchPay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 상단 헤더
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = SolsolPrimary.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "솔솔 캠퍼스페이",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = SolsolPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "학사 서비스",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 바로가기 메뉴
        Text(
            text = "바로가기",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        val shortcutItems = listOf(
            ShortcutItem("결제", false) { },
            ShortcutItem("내역조회", false) { },
            ShortcutItem("정산요청", true, onNavigateToCreateDutchPay),
            ShortcutItem("송금하기", false) { },
            ShortcutItem("학생회", false) { },
            ShortcutItem("쿠폰", false) { }
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(shortcutItems) { item ->
                ShortcutCard(item = item)
            }
        }
    }
}

@Composable
fun ShortcutCard(item: ShortcutItem) {
    Card(
        onClick = if (item.enabled) { item.onClick } else { {} },
        colors = CardDefaults.cardColors(
            containerColor = if (item.enabled) SolsolPrimary.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.aspectRatio(1f),
        enabled = item.enabled
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = if (item.enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
        }
    }
}

@Composable
fun BenefitScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "혜택 화면",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "준비 중입니다",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun MenuScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "전체 메뉴",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "준비 중입니다",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

data class ShortcutItem(
    val title: String,
    val enabled: Boolean,
    val onClick: () -> Unit = {}
)