package com.heyyoung.solsol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.heyyoung.solsol.feature.dutchpay.presentation.create.CreateDutchPayScreen
import com.heyyoung.solsol.feature.dutchpay.presentation.payment.DutchPaymentScreen
import com.heyyoung.solsol.ui.theme.SolsolPrimary
import com.heyyoung.solsol.ui.theme.SolsolTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolsolTheme {
                SolsolApp()
            }
        }
    }
}

@Composable
fun SolsolApp() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
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
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("main") {
                when (selectedTab) {
                    0 -> AcademicScreen(
                        onNavigateToCreateDutchPay = {
                            navController.navigate("create_dutch_pay")
                        }
                    )
                    1 -> BenefitScreen()
                    2 -> MenuScreen()
                }
            }
            
            composable("create_dutch_pay") {
                CreateDutchPayScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onDutchPayCreated = { groupId ->
                        navController.navigate("dutch_payment/$groupId") {
                            popUpTo("main")
                        }
                    }
                )
            }
            
            composable("dutch_payment/{groupId}") {
                DutchPaymentScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
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