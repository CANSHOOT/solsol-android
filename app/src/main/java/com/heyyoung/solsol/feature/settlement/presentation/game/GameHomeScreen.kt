package com.heyyoung.solsol.feature.settlement.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyyoung.solsol.feature.settlement.domain.game.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHomeScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHost: () -> Unit = {},
    onNavigateToJoin: () -> Unit = {},
    viewModel: GameViewModel = viewModel()
) {
    val purple = Color(0xFF8B5FBF)
    val textMain = Color(0xFF2D3748)
    val textSub = Color(0xFF718096)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFAFA),
                        Color.White
                    )
                )
            )
    ) {
        // 상단 앱바 - 모던 스타일
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "랜덤 게임",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textMain
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFFF7FAFC),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "뒤로",
                        tint = textMain,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "룰렛 게임으로 정산하기",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textMain,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "P2P 연결을 통해 친구들과\n실시간 룰렛 게임을 즐겨보세요!",
                fontSize = 17.sp,
                color = textSub,
                lineHeight = 26.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            GameModeCard(
                title = "방 만들기",
                description = "새로운 게임을 생성하고\n친구들을 초대해보세요",
                backgroundColor = Color(0xFFF8F6FF),
                borderColor = purple.copy(alpha = 0.2f),
                icon = Icons.Default.Add,
                onClick = onNavigateToHost
            )

            Spacer(modifier = Modifier.height(20.dp))

            GameModeCard(
                title = "방 참가하기",
                description = "주변 게임을 검색하고\n참가해보세요",
                backgroundColor = Color.White,
                borderColor = Color(0xFFE2E8F0),
                icon = Icons.Default.Search,
                onClick = onNavigateToJoin
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 게임 방법 섹션
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        purple.copy(alpha = 0.15f),
                                        purple.copy(alpha = 0.08f)
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = purple,
                                    shape = RoundedCornerShape(5.dp)
                                )
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "게임 방법",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = purple
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            spotColor = Color(0x1A8B5FBF),
                            ambientColor = Color(0x1A8B5FBF),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        GameRuleItem(
                            step = "1",
                            description = "호스트가 방을 만들고 참가자를 모집합니다"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GameRuleItem(
                            step = "2",
                            description = "모든 참가자가 모이면 룰렛 게임을 시작합니다"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GameRuleItem(
                            step = "3",
                            description = "룰렛이 돌아가며 한 명이 당첨됩니다"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GameRuleItem(
                            step = "4",
                            description = "당첨된 사람이 전체 정산을 담당합니다"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GameModeCard(
    title: String,
    description: String,
    backgroundColor: Color,
    borderColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val purple = Color(0xFF8B5FBF)
    val textMain = Color(0xFF2D3748)
    val textSub = Color(0xFF718096)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x1A8B5FBF),
                ambientColor = Color(0x1A8B5FBF),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(borderColor, borderColor))
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘 영역
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                purple.copy(alpha = 0.15f),
                                purple.copy(alpha = 0.08f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = purple,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // 텍스트 영역
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    fontSize = 15.sp,
                    color = textSub,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun GameRuleItem(
    step: String,
    description: String
) {
    val purple = Color(0xFF8B5FBF)
    val textMain = Color(0xFF2D3748)
    val textSub = Color(0xFF718096)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            purple.copy(alpha = 0.15f),
                            purple.copy(alpha = 0.08f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = purple
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = description,
            fontSize = 16.sp,
            color = textMain,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}