package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StageId
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageSelectScreen(viewModel: GameViewModel) {
    BackHandler {
        viewModel.navigateTo(Screen.Menu)
    }

    val selectedStage by viewModel.selectedStage.collectAsState()
    val meta by viewModel.playerMeta.collectAsState()
    val highestUnlocked = meta?.highestStageUnlocked ?: 1
    val stages = StageId.values()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecionar Fase Odonto", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(stages) { stageItem ->
                val isUnlocked = stageItem.stageNumber <= highestUnlocked
                val isSelected = selectedStage == stageItem && isUnlocked
                val prevStage = stages.find { it.stageNumber == stageItem.stageNumber - 1 }

                StageCard(
                    stage = stageItem,
                    isSelected = isSelected,
                    isUnlocked = isUnlocked,
                    previousStageName = prevStage?.title,
                    onSelect = {
                        if (isUnlocked) {
                            viewModel.selectStage(stageItem)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StageCard(
    stage: StageId,
    isSelected: Boolean,
    isUnlocked: Boolean,
    previousStageName: String?,
    onSelect: () -> Unit
) {
    val themeColor = if (isUnlocked) Color(stage.themeColorHex) else Color(0xFF475569)
    val borderColor = when {
        isSelected -> Color(0xFF38BDF8)
        isUnlocked -> Color(0x33FFFFFF)
        else -> Color(0x15FFFFFF)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isSelected) 3.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(enabled = isUnlocked) { onSelect() }
            .testTag("stage_card_${stage.name}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> Color(0xFF1E293B)
                isUnlocked -> Color(0xFF182030)
                else -> Color(0xFF101726)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = themeColor,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!isUnlocked) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Dangerous,
                                    contentDescription = "Bloqueado",
                                    tint = Color(0xFFCBD5E1),
                                    modifier = Modifier.size(22.dp)
                                )
                            } else if (stage == StageId.STAGE_INFINITE) {
                                Icon(Icons.Default.AllInclusive, contentDescription = null, tint = Color.White)
                            } else {
                                Text("${stage.stageNumber}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stage.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isUnlocked) Color.White else Color(0xFF94A3B8)
                            )
                            if (!isUnlocked) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0x33EF4444)
                                ) {
                                    Text(
                                        "BLOQUEADA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFCA5A5),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            stage.subtitle,
                            fontSize = 12.sp,
                            color = if (isUnlocked) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10B981),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x22EF4444),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44EF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (stage == StageId.STAGE_INFINITE) {
                                "Complete a Cidadela do Tártaro (Fase 5) para desbloquear o Modo Infinito!"
                            } else {
                                "Termine a Fase ${stage.stageNumber - 1}${if (previousStageName != null) " ($previousStageName)" else ""} para desbloquear esta fase e seus equipamentos!"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFFFCA5A5),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Duration & Boss Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = if (isUnlocked) Color(0xFF38BDF8) else Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (stage == StageId.STAGE_INFINITE) "Sobrevivência Infinita" else "Duração: 5 minutos",
                        fontSize = 12.sp,
                        color = if (isUnlocked) Color(0xFFBAE6FD) else Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dangerous, contentDescription = null, tint = if (isUnlocked) Color(0xFFEF4444) else Color(0xFF78716C), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Chefe: ${stage.bossName}",
                        fontSize = 11.sp,
                        color = if (isUnlocked) Color(0xFFFCA5A5) else Color(0xFF78716C),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                stage.bossDescription,
                fontSize = 11.sp,
                color = if (isUnlocked) Color(0xFF94A3B8) else Color(0xFF475569),
                lineHeight = 15.sp
            )
        }
    }
}
