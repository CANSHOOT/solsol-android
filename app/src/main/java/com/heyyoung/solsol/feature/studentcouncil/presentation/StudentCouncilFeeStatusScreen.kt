package com.heyyoung.solsol.feature.studentcouncil.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.heyyoung.solsol.core.network.FeeStatusResponse
import com.heyyoung.solsol.feature.studentcouncil.StudentCouncilViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "FeeStatusScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCouncilFeeStatusScreen(
    onNavigateBack: () -> Unit = {},
    // Main에서 넘겨주는 형태에 맞춤
    feeStatusList: List<FeeStatusResponse>,
    viewModel: StudentCouncilViewModel = hiltViewModel()
) {
    // 화면 진입 시마다 refresh 실행
    LaunchedEffect(Unit) {
        viewModel.loadFeeStatus(councilId = 1, feeId = 10001L)
    }

    val isLoading by remember { androidx.compose.runtime.derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { androidx.compose.runtime.derivedStateOf { viewModel.errorMessage } }
    val feeStatus = feeStatusList.firstOrNull()

    Log.d(TAG, "loading=$isLoading, error=$errorMessage, feeStatus=$feeStatus")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CenterAlignedTopAppBar(
            title = { Text("회비 현황", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1C1C1E),
                navigationIconContentColor = Color(0xFF1C1C1E)
            )
        )

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                ErrorState(message = errorMessage ?: "알 수 없는 오류")
            }
            feeStatus == null -> {
                EmptyState("데이터가 없습니다.")
            }
            else -> {
                FeeStatusContent(feeStatus)
            }
        }
    }
}

@Composable
private fun FeeStatusContent(fee: FeeStatusResponse) {
    val total = fee.totalStudents
    val paid = fee.paidCount
    val ratio = if (total > 0) paid.toFloat() / total.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        // 요약 카드
        SummaryCard(totalCount = total, paidCount = paid, ratio = ratio)

        Spacer(Modifier.height(24.dp))

        // 학생 리스트
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(fee.students) { s ->
                StudentRow(
                    name = s.name,
                    dept = s.departmentName,
                    studentId = s.studentNumber,
                    isPaid = s.paid,
                    paidAt = s.paidAt
                )
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun SummaryCard(totalCount: Int, paidCount: Int, ratio: Float) {
    Box(
        modifier = Modifier
            .shadow(8.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
            .border(1.dp, Color(0xCC8B5FBF), RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .height(130.dp)
            .background(Color(0xFFF8F7FF), RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(
                text = "총 ${String.format("%,d", totalCount)}명 중 ${String.format("%,d", paidCount)}명 납부 완료",
                fontSize = 18.sp,
                color = Color(0xFF1C1C1E),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier.fillMaxWidth(),
                trackColor = Color(0xFFEDE7F6),
                color = Color(0xFF8B5FBF)
            )
        }
    }
}

@Composable
private fun StudentRow(
    name: String,
    dept: String,
    studentId: String,
    isPaid: Boolean,
    paidAt: String?
) {
    Box(
        modifier = Modifier
            .shadow(4.dp, spotColor = Color(0x0D000000), ambientColor = Color(0x0D000000))
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1C1E))
                Text("$dept • $studentId", fontSize = 12.sp, color = Color(0xFF666666))
                if (isPaid && !paidAt.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "납부일: ${formatIsoInstant(paidAt)}",
                        fontSize = 11.sp,
                        color = Color(0xFF8B5FBF)
                    )
                }
            }
            FeeStatusPill(isPaid)
        }
    }
}

@Composable
private fun FeeStatusPill(isPaid: Boolean) {
    if (isPaid) {
        Box(
            modifier = Modifier
                .width(45.dp)
                .height(20.dp)
                .background(Color(0xFF8B5FBF), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("완료", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }
    } else {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(20.dp)
                .border(1.dp, Color(0xFF8B5FBF), RoundedCornerShape(10.dp))
                .background(Color.White, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("미완료", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF8B5FBF))
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = Color(0xFFEF4444), fontSize = 14.sp)
    }
}

@Composable
private fun EmptyState(hint: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(hint, color = Color(0xFF666666), fontSize = 14.sp)
    }
}

/* ---- util ---- */

private fun formatIsoInstant(iso: String): String = try {
    Instant.parse(iso)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
} catch (_: Throwable) {
    iso
}
