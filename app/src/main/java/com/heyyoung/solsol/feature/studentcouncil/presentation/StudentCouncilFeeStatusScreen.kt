package com.heyyoung.solsol.feature.studentcouncil.presentation

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyyoung.solsol.core.network.FeeStatusResponse

private const val TAG = "FeeStatusScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCouncilFeeStatusScreen(
    onNavigateBack: () -> Unit = {},
    feeStatusList: List<FeeStatusResponse>
) {
    val paidCount = feeStatusList.count { it.paid }
    val totalCount = feeStatusList.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = { Text("회비 현황") },
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
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "우리 과 회비 납부 현황",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(24.dp))

            FeeStatusSummaryCard(paidCount = paidCount, totalCount = totalCount)

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(feeStatusList) { student ->
                    StudentFeeCard(student)
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
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
            .shadow(8.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
            .border(1.dp, Color(0xCC8B5FBF), RoundedCornerShape(12.dp))
            .width(342.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F7FF)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text("총 ", fontSize = 18.sp, color = Color(0xFF1C1C1E))
                Text("$totalCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5FBF))
                Text("명 중 ", fontSize = 18.sp, color = Color(0xFF1C1C1E))
                Text("$paidCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5FBF))
                Text("명 납부 완료", fontSize = 18.sp, color = Color(0xFF1C1C1E))
            }
        }
    }
}

@Composable
private fun StudentFeeCard(student: FeeStatusResponse) {
    Card(
        modifier = Modifier
            .shadow(4.dp, spotColor = Color(0x0D000000), ambientColor = Color(0x0D000000))
            .width(342.dp)
            .height(60.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 프로필 (원형)
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF999999))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(student.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1C1E))
                    Text("${student.department} • ${student.studentId}", fontSize = 12.sp, color = Color(0xFF666666))
                }
            }
            FeeStatusButton(isPaid = student.paid)
        }
    }
}

@Composable
private fun FeeStatusButton(isPaid: Boolean) {
    Button(
        onClick = { /* 납부 상태 토글 로직이 필요하면 여기에 */ },
        modifier = Modifier.height(32.dp).width(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isPaid) Color(0xFF8B5FBF) else Color.White),
        shape = RoundedCornerShape(16.dp),
        border = if (!isPaid) ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF8B5FBF))
        ) else null,
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(if (isPaid) "완료" else "미완료", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (isPaid) Color.White else Color(0xFF8B5FBF))
    }
}
