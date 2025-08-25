package com.heyyoung.solsol

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.heyyoung.solsol.feature.auth.presentation.LoginScreen
import com.heyyoung.solsol.feature.home.presentation.HomeScreen
import com.heyyoung.solsol.ui.theme.SolsolTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.i(TAG, "쏠쏠해영 앱 시작")
        Log.d(TAG, "MainActivity 생성")

        setContent {
            SolsolTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SolsolApp()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "MainActivity 시작")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "▶MainActivity 활성화")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "⏸MainActivity 일시정지")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "쏠쏠해영 앱 종료")
    }

}

@Composable
fun SolsolApp() {
    val TAG = "SolsolApp"

    // 현재 어떤 화면을 보여줄지 결정하는 상태
    var currentScreen by remember { mutableStateOf("login") }
    var currentUserEmail by remember { mutableStateOf("") }
    var scannedQRData by remember { mutableStateOf<String?>(null) }

    // 앱 상태 로깅
    LaunchedEffect(currentScreen) {
        Log.i(TAG, "🔄 화면 전환: $currentScreen")
        when (currentScreen) {
            "login" -> Log.d(TAG, "🔑 로그인 화면 활성화")
            "home" -> Log.d(TAG, "🏠 홈 화면 활성화 (사용자: $currentUserEmail)")
        }
    }

    when (currentScreen) {
        "login" -> {
            // 로그인 화면 표시
            LoginScreen(
                onLoginSuccess = {
                    // 로그인 성공 시 홈 화면으로 이동
                    currentScreen = "home"
                    Log.i(TAG, "✅ 로그인 성공! 홈 화면으로 이동")
                }
            )
        }

        "home" -> {
            // 홈 화면 표시
            HomeScreen(
                onLogout = {
                    // 로그아웃 시 로그인 화면으로 이동
                    currentScreen = "login"
                    currentUserEmail = ""
                    Log.i(TAG, "로그아웃! 로그인 화면으로 이동")
                },
                onNavigateToQrScan = {
                    Log.d(TAG, "QR 스캔 화면으로 이동")
                    currentScreen = "qr"
                },
                onNavigateToSettlement = {
                    Log.d(TAG, "정산 화면으로 이동 (미구현)")
                    // TODO: 정산 화면 구현 후 연결
                },
                onNavigateToCouncil = {
                    Log.d(TAG, "학생회 화면으로 이동 (미구현)")
                    // TODO: 학생회 화면 구현 후 연결
                }
            )
        }

        "qr" -> {
            // QR 스캔 화면
            com.heyyoung.solsol.feature.payment.presentation.QRScanScreen(
                onNavigateBack = {
                    currentScreen = "home"
                },
                onQRScanned = { qrData ->
                    Log.d(TAG, "QR 스캔 완료: $qrData")
                    scannedQRData = qrData
                    currentScreen = "payment" // ← 결제 화면으로 이동
                }
            )
        }

        "payment" -> {
            // 결제 화면
            com.heyyoung.solsol.feature.payment.presentation.PaymentScreen(
                qrData = scannedQRData ?: "",
                onNavigateBack = {
                    // 뒤로가기 시 QR 스캔 화면으로 돌아가기
                    currentScreen = "qr"
                },
                onPaymentComplete = {
                    Log.d(TAG, "결제 완료! 홈으로 이동")
                    // 결제 완료 시 홈으로 이동
                    currentScreen = "home"
                    scannedQRData = null // QR 데이터 초기화
                }
            )
        }


        else -> {
            // 예상치 못한 화면 상태
            Log.e(TAG, "❌ 알 수 없는 화면 상태: $currentScreen")
            currentScreen = "login" // 안전하게 로그인 화면으로 돌아가기
        }
    }
}