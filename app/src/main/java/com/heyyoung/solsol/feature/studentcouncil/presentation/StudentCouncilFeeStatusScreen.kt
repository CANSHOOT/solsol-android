package com.heyyoung.solsol.feature.studentcouncil.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TAG = "FeeStatusScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCouncilFeeStatusScreen(
    onNavigateBack: () -> Unit = {}
) {
    // 회비 납부 데이터 (데모용)
    val feeStatusList = remember { createFeeStatusList() }
    val paidCount = feeStatusList.count { it.isPaid }
    val totalCount = feeStatusList.size

    Log.d(TAG, "회비 현황 화면 - 총 ${totalCount}명 중 ${paidCount}명 납부")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("회비 현황") },
            navigationIcon = {
                IconButton(onClick = {
                    Log.d(TAG, "뒤로가기 클릭")
                    onNavigateBack()
                }) {
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
            Spacer(modifier = Modifier.height(20.dp))

            // 페이지 제목
            Text(
                text = "우리 과 회비 납부 현황",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 납부 현황 요약 카드
            FeeStatusSummaryCard(
                paidCount = paidCount,
                totalCount = totalCount
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 학생 목록
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(feeStatusList) { student ->
                    StudentFeeCard(student = student)
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun FeeStatusSummaryCard(
    paidCount: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .border(
                width = 1.dp,
                color = Color(0xCC8B5FBF),
                shape = RoundedCornerShape(size = 12.dp)
            )
            .width(342.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F7FF)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "총 ",
                    fontSize = 18.sp,
                    color = Color(0xFF1C1C1E)
                )
                Text(
                    text = "$totalCount",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B5FBF)
                )
                Text(
                    text = "명 중 ",
                    fontSize = 18.sp,
                    color = Color(0xFF1C1C1E)
                )
                Text(
                    text = "$paidCount",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B5FBF)
                )
                Text(
                    text = "명 납부 완료",
                    fontSize = 18.sp,
                    color = Color(0xFF1C1C1E)
                )
            }
        }
    }
}

@Composable
private fun StudentFeeCard(student: StudentFeeStatus) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x0D000000),
                ambientColor = Color(0x0D000000)
            )
            .width(342.dp)
            .height(60.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 이미지 (원형)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFFE0E0E0),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF999999),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 학생 정보
                Column {
                    Text(
                        text = student.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = "${student.department} • ${student.studentId}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            // 납부 상태 버튼
            FeeStatusButton(isPaid = student.isPaid)
        }
    }
}

@Composable
private fun FeeStatusButton(isPaid: Boolean) {
    Button(
        onClick = {
            // 클릭 시 상태 변경 로직 (데모에서는 비활성화)
            Log.d(TAG, "회비 상태 변경 클릭")
        },
        modifier = Modifier
            .height(32.dp)
            .width(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPaid) Color(0xFF8B5FBF) else Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (!isPaid) ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF8B5FBF))
        ) else null,
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = if (isPaid) "완료" else "미완료",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isPaid) Color.White else Color(0xFF8B5FBF)
        )
    }
}

// 데모 데이터 생성
private fun createFeeStatusList(): List<StudentFeeStatus> = listOf(
    StudentFeeStatus("김신한", "컴퓨터공학과", "20251234", true),
    StudentFeeStatus("이지헌", "컴퓨터공학과", "20251234", false),
    StudentFeeStatus("박민수", "컴퓨터공학과", "20251234", true),
    StudentFeeStatus("김신한", "컴퓨터공학과", "20251234", true),
    StudentFeeStatus("이지헌", "컴퓨터공학과", "20251234", false),
    StudentFeeStatus("박민수", "컴퓨터공학과", "20251234", true),
    StudentFeeStatus("최영희", "컴퓨터공학과", "20251235", true),
    StudentFeeStatus("한석봉", "컴퓨터공학과", "20251236", false),
    StudentFeeStatus("윤서연", "컴퓨터공학과", "20251237", true),
    StudentFeeStatus("임창수", "컴퓨터공학과", "20251238", true),
    StudentFeeStatus("조미정", "컴퓨터공학과", "20251239", true),
    StudentFeeStatus("강태호", "컴퓨터공학과", "20251240", false),
    StudentFeeStatus("송은지", "컴퓨터공학과", "20251241", true),
    StudentFeeStatus("배준혁", "컴퓨터공학과", "20251242", true),
    StudentFeeStatus("신유진", "컴퓨터공학과", "20251243", false),
    StudentFeeStatus("오성민", "컴퓨터공학과", "20251244", true),
    StudentFeeStatus("허지우", "컴퓨터공학과", "20251245", true),
    StudentFeeStatus("남혜원", "컴퓨터공학과", "20251246", true),
    StudentFeeStatus("권도현", "컴퓨터공학과", "20251247", false),
    StudentFeeStatus("장소영", "컴퓨터공학과", "20251248", true),
    StudentFeeStatus("홍길동", "컴퓨터공학과", "20251249", true),
    StudentFeeStatus("김영수", "컴퓨터공학과", "20251250", true),
    StudentFeeStatus("박서준", "컴퓨터공학과", "20251251", true),
    StudentFeeStatus("이민호", "컴퓨터공학과", "20251252", false),
    StudentFeeStatus("정하나", "컴퓨터공학과", "20251253", true)
)

/**
 * 학생 회비 납부 상태 데이터
 */
data class StudentFeeStatus(
    val name: String,
    val department: String,
    val studentId: String,
    val isPaid: Boolean
)