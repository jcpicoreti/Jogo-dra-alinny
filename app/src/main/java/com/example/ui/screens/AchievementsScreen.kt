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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ALL_ACHIEVEMENTS
import com.example.data.repository.AchievementDef
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(viewModel: GameViewModel) {
    BackHandler {
        viewModel.navigateTo(Screen.Menu)
    }

    val unlockedEntities by viewModel.achievements.collectAsState()
    val unlockedMap = unlockedEntities.associateBy { it.id }

    val unlockedCount = ALL_ACHIEVEMENTS.count { unlockedMap[it.id]?.isUnlocked == true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conquistas ($unlockedCount/${ALL_ACHIEVEMENTS.size})", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0F172A))
                .padding(16.dp)
                .testTag("achievements_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(ALL_ACHIEVEMENTS) { def ->
                val entity = unlockedMap[def.id]
                val isUnlocked = entity?.isUnlocked == true
                val progress = entity?.currentProgress ?: 0

                AchievementCard(def = def, isUnlocked = isUnlocked, progress = progress)
            }
        }
    }
}

@Composable
fun AchievementCard(
    def: AchievementDef,
    isUnlocked: Boolean,
    progress: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF1E293B) else Color(0xFF182030)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUnlocked) Color(0xFF10B981) else Color(0x22FFFFFF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = if (isUnlocked) Color(0xFF10B981) else Color(0x33FFFFFF),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isUnlocked) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        def.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isUnlocked) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3310B981)
                        ) {
                            Text(
                                "CONCLUÍDO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (def.maxProgress > 1) {
                        Text(
                            "$progress/${def.maxProgress}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    def.description,
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 16.sp
                )

                if (!isUnlocked && def.maxProgress > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val fraction = (progress.toFloat() / def.maxProgress.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0x33FFFFFF)
                    )
                }
            }
        }
    }
}
