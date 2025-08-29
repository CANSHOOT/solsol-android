package com.heyyoung.solsol

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.messaging.FirebaseMessaging
import com.heyyoung.solsol.feature.auth.presentation.LoginScreen
import com.heyyoung.solsol.feature.home.presentation.HomeScreen
import com.heyyoung.solsol.feature.remittance.presentation.RemittanceMainScreen
import com.heyyoung.solsol.feature.remittance.presentation.RemittanceScreen
import com.heyyoung.solsol.feature.remittance.presentation.RemittanceSuccessScreen
import com.heyyoung.solsol.feature.settlement.presentation.MoneyTransferScreen
import com.heyyoung.solsol.feature.settlement.presentation.SettlementEqualScreen
import com.heyyoung.solsol.feature.settlement.presentation.SettlementManualScreen
import com.heyyoung.solsol.feature.studentcouncil.StudentCouncilViewModel
import com.heyyoung.solsol.feature.studentcouncil.presentation.OcrCameraScreen
import com.heyyoung.solsol.feature.studentcouncil.presentation.ReceiptFields
import com.heyyoung.solsol.feature.studentcouncil.presentation.StudentCouncilMainScreen
import com.heyyoung.solsol.feature.studentcouncil.presentation.StudentCouncilExpenseHistoryScreen
import com.heyyoung.solsol.feature.studentcouncil.presentation.StudentCouncilFeeStatusScreen
import com.heyyoung.solsol.feature.studentcouncil.presentation.StudentCouncilScreen
import com.heyyoung.solsol.ui.theme.SolsolTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val TAG = "MainActivity"
    private var startDestination by mutableStateOf("login")
    private var initialPayAmount by mutableStateOf<String?>(null)
    private var initialPayeeName by mutableStateOf<String?>(null)
    private var initialGroupId by mutableStateOf<Long?>(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleNotificationIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        // FCM 토큰 테스트
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "FCM 토큰: $token")
        }
        enableEdgeToEdge()

        Log.i(TAG, "쏠쏠해영 앱 시작")
        Log.d(TAG, "MainActivity 생성")

        setContent {
            SolsolTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SolsolApp(
                        initialScreen = startDestination,
                        payeeName = initialPayeeName,
                        payAmount = initialPayAmount,
                        pushGroupId = initialGroupId
                    )
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent) {
        when (intent.getStringExtra("notification_action")) {
            "PAY_NOW" -> {
                initialPayAmount = intent.getStringExtra("pay_amount")
                initialPayeeName  = intent.getStringExtra("payee_name")
                val groupIdStr  = intent.getStringExtra("group_id")
                initialGroupId = groupIdStr?.toLongOrNull() ?: 0L
                startDestination  = "remittance"
                Log.d(TAG, "PAY_NOW 인텐트 수신: name=$initialPayeeName, amount=$initialPayAmount")
            }
            "OPEN_SETTLEMENT" -> {
                startDestination = "settlement_method"
            }
        }
    }


}

@Composable
fun SolsolApp(initialScreen: String = "login",
              payeeName: String? = null,
              payAmount: String? = null,
              pushGroupId: Long? = 0
              ) {
    val TAG = "SolsolApp"

    // 현재 어떤 화면을 보여줄지 결정하는 상태
    //var currentScreen by remember { mutableStateOf("login") }
    var currentScreen by remember(initialScreen) { mutableStateOf(initialScreen) }
    var currentUserEmail by remember { mutableStateOf("") }
    var scannedQRData by remember { mutableStateOf<String?>(null) }

    // 정산 관련 상태
    var selectedSettlementMethod by remember { mutableStateOf<String?>(null) }
    
    var settlementParticipants by remember { mutableStateOf<List<com.heyyoung.solsol.feature.settlement.domain.model.Person>>(emptyList()) }
    var completedSettlement by remember { mutableStateOf<com.heyyoung.solsol.feature.settlement.domain.model.SettlementGroup?>(null) }
    var settlementTotalAmount by remember { mutableStateOf(0) }
    var settlementAmountPerPerson by remember { mutableStateOf(0) }

    // OCR 테스트 페이지로 넘길 상태
    var lastOcrImageUri by remember { mutableStateOf<Uri?>(null) }
    var lastOcrText by remember { mutableStateOf<String?>(null) }
    var lastReceiptFields by remember { mutableStateOf<ReceiptFields?>(null) }
    val viewModel: StudentCouncilViewModel = hiltViewModel()

    // ✅ 송금 화면으로 전달할 선택 항목 상태
    var remittanceReceiverName by remember { mutableStateOf<String?>(null) }
    var remittanceAmount by remember { mutableStateOf<Long?>(null) }

    // 이체용 상태
    var remittanceGroupId by remember { mutableStateOf<Long?>(null) }

    // 앱 상태 로깅
    LaunchedEffect(currentScreen) {
        Log.i(TAG, "🔄 화면 전환: $currentScreen")
        when (currentScreen) {
            "login" -> Log.d(TAG, "로그인 화면 활성화")
            "home" -> Log.d(TAG, "홈 화면 활성화 (사용자: $currentUserEmail)")
            "qr" -> Log.d(TAG, "QR 스캔 화면 활성화")
            "payment" -> Log.d(TAG, "결제 화면 활성화")
            "settlement_method" -> Log.d(TAG, "정산 방식 선택 화면 활성화")
            "settlement_participants" -> Log.d(TAG, "정산 참여자 화면 활성화")
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
                onNavigateToPaymentHistory = {
                    Log.d(TAG, "결제 내역 화면으로 이동")
                    currentScreen = "payment_history"
                },
                onNavigateToSettlement = {
                    Log.d(TAG, "정산 화면으로 이동")
                    currentScreen = "settlement_method"
                },
                onNavigateToCouncil = {
                    Log.d(TAG, "학생회 화면으로 이동")
                    currentScreen = "council"
                },
                onNavigateToMoneyTransfer = {  // 이 부분 추가
                    Log.d(TAG, "송금하기 화면으로 이동")
                    currentScreen = "money_transfer"
                },
                onNavigateToCoupon = {
                    Log.d(TAG, "쿠폰 화면으로 이동")
                    currentScreen = "coupon"
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

        "payment_history" -> {
            // 결제 내역 화면
            com.heyyoung.solsol.feature.payment.presentation.PaymentHistoryScreen(
                onNavigateBack = {
                    Log.d(TAG, "결제 내역에서 홈으로 돌아가기")
                    currentScreen = "home"
                }
            )
        }

        "settlement_method" -> {
            // 정산 방식 선택 화면
            com.heyyoung.solsol.feature.settlement.presentation.SettlementMethodScreen(
                onNavigateBack = {
                    Log.d(TAG, "정산 방식 선택에서 홈으로 돌아가기")
                    currentScreen = "home"
                },
                onMethodSelected = { method ->
                    Log.d(TAG, "정산 방식 선택됨: $method")
                    selectedSettlementMethod = method
                    currentScreen = "settlement_participants"
                },
                onNavigateToGame = {
                    Log.d(TAG, "랜덤 게임으로 네비게이션")
                    currentScreen = "game_home"
                }
            )
        }

        "settlement_participants" -> {
            // 정산 참여자 선택 화면
            com.heyyoung.solsol.feature.settlement.presentation.SettlementParticipantsScreen(
                onNavigateBack = {
                    Log.d(TAG, "참여자 선택에서 방식 선택으로 돌아가기")
                    currentScreen = "settlement_method"
                },
                onNext = { participants ->
                    Log.d(TAG, "참여자 선택 완료: ${participants.size}명, 방식: $selectedSettlementMethod")
                    settlementParticipants = participants
                    when (selectedSettlementMethod) {
                        "equal" -> {
                            Log.d(TAG, "똑같이 나누기 화면으로 이동")
                            currentScreen = "settlement_equal"
                        }

                        "manual" -> {
                            Log.d(TAG, "직접 입력하기 화면으로 이동")
                            currentScreen = "settlement_manual"
                        }

                        "random" -> {
                            Log.d(TAG, "랜덤 게임 화면으로 이동 (미구현)")
                            // TODO: 랜덤 게임 화면 구현 후 연결
                            currentScreen = "home" // 임시로 홈으로
                        }

                        else -> {
                            Log.w(TAG, "알 수 없는 정산 방식: $selectedSettlementMethod")
                            currentScreen = "home"
                        }
                    }
                }
            )
        }

        "settlement_equal" -> {
            // 똑같이 나누기 화면
            SettlementEqualScreen(
                participants = settlementParticipants,
                onNavigateBack = {
                    Log.d(TAG, "똑같이 나누기에서 참여자 선택으로 돌아가기")
                    currentScreen = "settlement_participants"
                },
                onRequestSettlement = { totalAmount, settlementMap ->
                    Log.d(TAG, "똑같이 나누기 정산 요청 완료!")
                    Log.d(TAG, "총액: ${totalAmount}원, 참여자: ${settlementMap.size}명")

                    settlementMap.forEach { (person, amount) ->
                        Log.d(TAG, "  ${person.name}: ${amount}원")
                    }
                    // 해커톤용: 정산 완료 후 홈으로 이동
                    currentScreen = "home"
                    // 정산 상태 초기화
                    selectedSettlementMethod = null
                    settlementParticipants = emptyList()
                },
                onNavigateToComplete = { settlementGroup, participants, totalAmount, amountPerPerson ->
                    Log.d(TAG, "✅ 정산 생성 성공 - 완료 화면으로 이동")
                    completedSettlement = settlementGroup
                    settlementTotalAmount = totalAmount
                    settlementAmountPerPerson = amountPerPerson
                    currentScreen = "settlement_complete"
                }
            )
        }

        "settlement_manual" -> {
            SettlementManualScreen(
                participants = settlementParticipants,
                onNavigateBack = {
                    Log.d(TAG, "직접 입력하기에서 참여자 선택으로 돌아가기")
                    currentScreen = "settlement_participants"
                },
                onNavigateToComplete = { settlementGroup, participants, totalAmount ->
                    Log.d(TAG, "✅ 수동 정산 성공 - 완료 화면으로 이동")
                    completedSettlement = settlementGroup
                    settlementParticipants = participants
                    settlementTotalAmount = totalAmount
                    settlementAmountPerPerson = 0 // 수동 입력이라 0으로 처리
                    currentScreen = "settlement_complete"
                }
            )
        }

        "settlement_complete" -> {
            // 정산 완료 화면
            com.heyyoung.solsol.feature.settlement.presentation.SettlementCompleteScreen(
                settlementGroup = completedSettlement,
                participants = settlementParticipants,
                totalAmount = settlementTotalAmount,
                amountPerPerson = settlementAmountPerPerson,
                onNavigateToHome = {
                    Log.d(TAG, "정산 완료 화면에서 홈으로 이동")
                    currentScreen = "home"
                    // 정산 상태 초기화
                    selectedSettlementMethod = null
                    settlementParticipants = emptyList()
                    completedSettlement = null
                    settlementTotalAmount = 0
                    settlementAmountPerPerson = 0
                }
            )
        }

        // ✅ 송금 목록
        "money_transfer" -> {
            MoneyTransferScreen(
                onNavigateBack = { currentScreen = "home" },
                onNavigateToRemittance = { groupId, receiverName, amount ->
                    // 선택값 저장 후 송금 화면으로 이동
                    remittanceGroupId = groupId
                    remittanceReceiverName = receiverName
                    remittanceAmount = amount
                    currentScreen = "remittance_main"
                }
            )
        }

        // ✅ 송금 성공 화면
        "remittance_success" -> {
            RemittanceSuccessScreen(
                receiverName = remittanceReceiverName ?: "",
                amount = String.format("%,d", remittanceAmount ?: 0),
                onComplete = {
                    // 홈으로 이동
                    currentScreen = "home"
                    // ✅ 상태 초기화
                    remittanceGroupId = null
                    remittanceReceiverName = null
                    remittanceAmount = null
                }
            )
        }

        // 학생회 메인
        "council" -> {
            StudentCouncilMainScreen(
                deptId = 1L,          // 필요 시 실제 값으로 교체
                councilId = 1L,       // 필요 시 실제 값으로 교체
                onNavigateBack = { currentScreen = "home" }
            )
        }

        // 학생회 지출 내역
        "council_history" -> {
            StudentCouncilExpenseHistoryScreen(
                onNavigateBack = { currentScreen = "council" },
                onNavigateToRegister = { currentScreen = "council_register" },
                expenseList = viewModel.expenditureList,
                currentBalance = viewModel.currentBalance
            )
        }

        // 학생회 지출 등록(OCR 카메라)
        "council_register" -> {
            OcrCameraScreen(
                onNavigateBack = { currentScreen = "council" },
                onOcrResult = { result ->
                    // 필요시 기존 호환용 로직
                    Log.d("SolsolApp", "OCR Result: $result")
                }
            )
        }

        // 학생회 회비 현황
        "council_fee_status" -> {
            StudentCouncilFeeStatusScreen(
                onNavigateBack = { currentScreen = "council" },
                feeStatusList = viewModel.feeStatus?.let { listOf(it) } ?: emptyList()
            )
        }


        // ✅ 송금 실행 화면
        "remittance_main" -> {
            RemittanceMainScreen(
                groupId = remittanceGroupId,
                receiverName = remittanceReceiverName ?: "",
                receiverInfo = "", // 필요 시 이메일/계좌 등 표시
                amount = String.format("%,d", remittanceAmount ?: 0),
                cardNumber = "**** **** **** 1234", // TODO: 실제 카드/계좌 연동
                onNavigateBack = { currentScreen = "money_transfer" },
                onRemittanceComplete = {
                    currentScreen = "remittance_success"
                }
            )
        }

        "remittance" -> {
            RemittanceScreen(
                groupId = pushGroupId ?: 0L,
                receiverName = payeeName ?: "상대방",
                amount = payAmount ?: "0",
                onNavigateBack = { currentScreen = "home" },
                onRemittanceComplete = { currentScreen = "home" }
            )
        }


        "game_home" -> {
            // 게임 홈 화면
            com.heyyoung.solsol.feature.settlement.presentation.game.GameHomeScreen(
                onNavigateBack = {
                    Log.d(TAG, "게임 홈에서 정산 방식 선택으로 돌아가기")
                    currentScreen = "settlement_method"
                },
                onNavigateToHost = {
                    Log.d(TAG, "호스트 화면으로 이동")
                    currentScreen = "game_host"
                },
                onNavigateToJoin = {
                    Log.d(TAG, "참가 화면으로 이동")
                    currentScreen = "game_join"
                }
            )
        }

        "game_host" -> {
            // 게임 호스트 화면
            com.heyyoung.solsol.feature.settlement.presentation.game.HostScreen(
                onNavigateBack = {
                    Log.d(TAG, "호스트 화면에서 게임 홈으로 돌아가기")
                    currentScreen = "game_home"
                },
                onNavigateToRoom = {
                    Log.d(TAG, "게임 룸으로 이동")
                    currentScreen = "game_room"
                }
            )
        }

        "game_join" -> {
            // 게임 참가 화면
            com.heyyoung.solsol.feature.settlement.presentation.game.JoinScreen(
                onNavigateBack = {
                    Log.d(TAG, "참가 화면에서 게임 홈으로 돌아가기")
                    currentScreen = "game_home"
                },
                onNavigateToRoom = {
                    Log.d(TAG, "게임 룸으로 이동")
                    currentScreen = "game_room"
                }
            )
        }

        "game_room" -> {
            // 게임 룸 화면
            com.heyyoung.solsol.feature.settlement.presentation.game.GameRoomScreen(
                onNavigateBack = {
                    Log.d(TAG, "게임 룸에서 게임 홈으로 돌아가기")
                    currentScreen = "game_home"
                },
                onGameFinished = {
                    // 게임 완료 후 홈으로 이동
                    currentScreen = "home"
                },
                onNavigateRemittance = {
                    currentScreen = "money_transfer"
                }
            )
        }

        "coupon" -> {
            // 쿠폰 화면
            com.heyyoung.solsol.feature.coupon.presentation.CouponScreen(
                onNavigateBack = {
                    Log.d(TAG, "쿠폰 화면에서 홈으로 돌아가기")
                    currentScreen = "home"
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