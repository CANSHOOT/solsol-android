package com.heyyoung.solsol.feature.studentcouncil.presentation

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrResultTestScreen(
    imageUri: Uri?,
    ocrText: String?,
    parsed: ReceiptFields?,
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("OCR 테스트 결과") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 이미지 미리보기
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "촬영 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }

            Text("파싱 결과", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("가맹점: ${parsed?.merchant ?: "-"}")
            Text("날짜: ${parsed?.date ?: "-"}")
            Text("총액: ${parsed?.total ?: "-"}")

            Divider()

            Text("인식 원문", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(ocrText ?: "(없음)")
        }
    }
}
