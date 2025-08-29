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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
    feeStatusList: List<FeeStatusResponse>,
    viewModel: StudentCouncilViewModel = hiltViewModel()
) {
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
            title = {
                Text(
                    "회비 현황",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로", tint = Color(0xFF1C1C1E))
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
                    CircularProgressIndicator(color = Color(0xFF8B5FBF))
                }
            }
            errorMessage != null -> ErrorState(errorMessage ?: "알 수 없는 오류")
            feeStatus == null -> EmptyState("데이터가 없습니다.")
            else -> FeeStatusContent(feeStatus)
        }
    }
}

@Composable
private fun FeeStatusContent(fee: FeeStatusResponse) {
    val total = fee.totalStudents
    val paid = fee.paidCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "우리 과 회비 납부 현황",
            fontSize = 14.sp,
            color = Color(0xFF4A5568),
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(14.dp))

        SummaryCardNoBar(totalCount = total, paidCount = paid)

        Spacer(Modifier.height(18.dp))

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

/** 하단 바(프로그레스바) 없이, 숫자만 크게/보라색으로 강조한 요약 카드 */
@Composable
private fun SummaryCardNoBar(totalCount: Int, paidCount: Int) {
    val purple = Color(0xFF8B5FBF)

    Box(
        modifier = Modifier
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xCC8B5FBF),
                shape = RoundedCornerShape(12.dp)
            )
            .fillMaxWidth()
            .height(100.dp)
            .background(
                color = Color(0xFFF8F7FF),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // 한 줄에서 숫자만 스타일 다르게
        val line = buildAnnotatedString {
            append("총 ")
            withStyle(
                SpanStyle(
                    color = purple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    letterSpacing = 0.26.sp
                )
            ) { append(String.format("%,d", totalCount)) }
            append("명 중 ")
            withStyle(
                SpanStyle(
                    color = purple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    letterSpacing = 0.26.sp
                )
            ) { append(String.format("%,d", paidCount)) }
            append("명 납부 완료")
        }

        Text(
            text = line,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2D3748),
            letterSpacing = 0.24.sp,
            maxLines = 1
        )
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
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                spotColor = Color(0x12000000),
                ambientColor = Color(0x12000000),
                shape = RoundedCornerShape(16.dp)
            )
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // 프로필 아이콘(요청 스타일)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF8B5FBF).copy(alpha = 0.10f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.first().toString(),
                        color = Color(0xFF8B5FBF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("$dept · $studentId", fontSize = 12.sp, color = Color(0xFF79808A))
                    if (isPaid && !paidAt.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "납부일: ${formatIsoInstant(paidAt)}",
                            fontSize = 11.sp,
                            color = Color(0xFF8B5FBF),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            FeeStatusPill(isPaid)
        }
    }
}

/** 완료/미완료 가로폭 동일(60.dp) */
@Composable
private fun FeeStatusPill(isPaid: Boolean) {
    val purple = Color(0xFF8B5FBF)
    val pillWidth = 60.dp
    val pillHeight = 28.dp

    if (isPaid) {
        Box(
            modifier = Modifier
                .height(pillHeight)
                .width(pillWidth)
                .background(purple, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("완료", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    } else {
        Box(
            modifier = Modifier
                .height(pillHeight)
                .width(pillWidth)
                .border(1.dp, purple, RoundedCornerShape(14.dp))
                .background(Color.White, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("미완료", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = purple)
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
