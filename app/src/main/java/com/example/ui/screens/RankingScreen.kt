package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ScoreEntity
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(viewModel: GameViewModel) {
    BackHandler {
        viewModel.navigateTo(Screen.Menu)
    }

    val highScores by viewModel.highScores.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ranking Local dos Dentistas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Menu) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0369A1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0F172A))
                .padding(16.dp)
        ) {
            if (highScores.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏆", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Nenhum recorde registrado ainda!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Jogue uma partida e salve seu nome para inaugurar o Hall da Fama.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize().testTag("ranking_list")
                ) {
                    itemsIndexed(highScores) { index, entry ->
                        RankingItemCard(rank = index + 1, entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
fun RankingItemCard(rank: Int, entry: ScoreEntity) {
    val medal = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "${rank}º"
    }

    val rankColor = when (rank) {
        1 -> Color(0xFFFDE047)
        2 -> Color(0xFFE2E8F0)
        3 -> Color(0xFFF97316)
        else -> Color(0xFF94A3B8)
    }

    val minutes = entry.timeSurvivedSeconds / 60
    val seconds = entry.timeSurvivedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rank <= 3) Color(0xFF1E293B) else Color(0xFF182030)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Number or Medal
                Box(
                    modifier = Modifier.width(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = medal,
                        fontSize = if (rank <= 3) 22.sp else 16.sp,
                        fontWeight = FontWeight.Black,
                        color = rankColor
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.playerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            entry.stageId,
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(" • ", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(
                            "⏱️ $timeFormatted",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                        Text(" • ", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(
                            "🦠 ${entry.kills}",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Score Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0x33FBBF24)
            ) {
                Text(
                    text = "${entry.score} pts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFDE047),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
