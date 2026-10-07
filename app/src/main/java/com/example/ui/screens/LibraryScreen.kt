package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EvolutionType
import com.example.data.model.PassiveType
import com.example.data.model.WeaponType
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.Screen

enum class LibraryTab(val title: String, val icon: String) {
    WEAPONS("Armas", "🪥"),
    PASSIVES("Equipamentos", "🛡️"),
    FUSIONS("Fusões", "⚡")
}

@Composable
fun LibraryScreen(viewModel: GameViewModel) {
    BackHandler {
        viewModel.navigateTo(Screen.Menu)
    }

    val meta by viewModel.playerMeta.collectAsState()
    val highestUnlocked = meta?.highestStageUnlocked ?: 1
    val discoveredList by viewModel.discoveredItems.collectAsState()
    val discoveredMap = remember(discoveredList) {
        discoveredList.associateBy({ it.itemId }, { it.timesUsed })
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Total stats
    val totalCount = WeaponType.values().size + PassiveType.values().size + EvolutionType.values().size // 15 + 15 + 15 = 45
    val discoveredCount = (
        WeaponType.values().count { it.name in discoveredMap } +
        PassiveType.values().count { it.name in discoveredMap } +
        EvolutionType.values().count { it.name in discoveredMap }
    )
    val progressFraction = (discoveredCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Menu) },
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        "BIBLIOTECA ODONTOLÓGICA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Catálogo Clínico de Armas, Equipamentos e Fusões",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Overall Discovery Progress Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0x33FFFFFF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3338BDF8))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📚 Progresso da Enciclopédia",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBAE6FD)
                        )
                        Text(
                            "$discoveredCount / $totalCount (${(progressFraction * 100).toInt()}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0x44000000)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Buscar poder ou equipamento...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0x44FFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0x22000000),
                    unfocusedContainerColor = Color(0x22000000)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs: Armas (15) | Equipamentos (15) | Fusões (15)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LibraryTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedTab = index },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF0284C7) else Color(0x221E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF38BDF8) else Color(0x33475569)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(tab.icon, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                tab.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Weapons (15)
                    val filtered = remember(searchQuery) {
                        WeaponType.values().filter {
                            searchQuery.isEmpty() || it.displayName.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filtered, key = { it.name }) { weapon ->
                            val isDiscovered = weapon.name in discoveredMap
                            val times = discoveredMap[weapon.name] ?: 0
                            WeaponLibraryCard(
                                weapon = weapon,
                                isDiscovered = isDiscovered,
                                timesUsed = times,
                                highestUnlocked = highestUnlocked
                            )
                        }
                    }
                }
                1 -> {
                    // Passives (15)
                    val filtered = remember(searchQuery) {
                        PassiveType.values().filter {
                            searchQuery.isEmpty() || it.displayName.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filtered, key = { it.name }) { passive ->
                            val isDiscovered = passive.name in discoveredMap
                            val times = discoveredMap[passive.name] ?: 0
                            PassiveLibraryCard(
                                passive = passive,
                                isDiscovered = isDiscovered,
                                timesUsed = times,
                                highestUnlocked = highestUnlocked
                            )
                        }
                    }
                }
                2 -> {
                    // Fusions (15)
                    val filtered = remember(searchQuery) {
                        EvolutionType.values().filter {
                            searchQuery.isEmpty() || it.displayName.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filtered, key = { it.name }) { evo ->
                            val isDiscovered = evo.name in discoveredMap
                            val times = discoveredMap[evo.name] ?: 0
                            FusionLibraryCard(
                                evolution = evo,
                                isDiscovered = isDiscovered,
                                timesUsed = times,
                                highestUnlocked = highestUnlocked
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun stageTitleForNumber(stageNum: Int): String {
    return when (stageNum) {
        1 -> "Fase 1 (Superfície do Esmalte)"
        2 -> "Fase 2 (Sulco Gengival)"
        3 -> "Fase 3 (Canal Radicular)"
        4 -> "Fase 4 (Arena dos Braquetes)"
        5 -> "Fase 5 (Cidadela do Tártaro)"
        else -> "Fase $stageNum"
    }
}

@Composable
fun WeaponLibraryCard(weapon: WeaponType, isDiscovered: Boolean, timesUsed: Int, highestUnlocked: Int) {
    val isStageUnlocked = weapon.unlockedAtStage <= highestUnlocked

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                when {
                    isDiscovered -> Color(0x4438BDF8)
                    isStageUnlocked -> Color(0x330284C7)
                    else -> Color(0x15FFFFFF)
                },
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDiscovered -> Color(0xFF1E293B)
                isStageUnlocked -> Color(0xFF141C2E)
                else -> Color(0xFF0B1120)
            }
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
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isDiscovered -> Color(0x330284C7)
                    isStageUnlocked -> Color(0x220284C7)
                    else -> Color(0x15FFFFFF)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isDiscovered) {
                        val iconEmoji = when (weapon) {
                            WeaponType.ESCOVA_DENTE -> "🪥"
                            WeaponType.FIO_DENTAL -> "🧵"
                            WeaponType.JATO_BICARBONATO -> "💧"
                            WeaponType.BROCA_ODONTO -> "🔩"
                            WeaponType.ENXAGUANTE_AURA -> "🧴"
                            WeaponType.LUZ_FOTOPOLIMERIZADORA -> "🔦"
                            WeaponType.PASTA_EXPLOSIVA -> "💣"
                            WeaponType.ESPELHO_CLINICO -> "🪞"
                            WeaponType.SERINGA_ANESTESICA -> "💉"
                            WeaponType.SUGADOR_SALIVA -> "🌪️"
                            WeaponType.CURETA_GRACEY -> "🗡️"
                            WeaponType.SPRAY_CLOREXIDINA -> "💨"
                            WeaponType.FITA_EXPANSIVA -> "🎗️"
                            WeaponType.ULTRASSOM_ODONTO -> "🔊"
                            WeaponType.PINCA_CIRURGICA -> "🦀"
                        }
                        Text(iconEmoji, fontSize = 26.sp)
                    } else {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = if (isStageUnlocked) Color(0xFF38BDF8) else Color(0xFF64748B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (isDiscovered) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(weapon.displayName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x3338BDF8)
                            ) {
                                Text(
                                    "Fase ${weapon.unlockedAtStage}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7DD3FC),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x330284C7)
                            ) {
                                Text(
                                    "Usado: $timesUsed",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(weapon.description, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 16.sp)

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("💥 Dano: ${weapon.baseDamage.toInt()}", fontSize = 11.sp, color = Color(0xFFFDE047), fontWeight = FontWeight.SemiBold)
                        Text("⏱️ Recarga: ${weapon.cooldownSeconds}s", fontSize = 11.sp, color = Color(0xFF86EFAC), fontWeight = FontWeight.SemiBold)
                        Text("🎯 Nível Máx: ${weapon.maxLevel}", fontSize = 11.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.SemiBold)
                    }

                    if (weapon.evolvedWeapon != null && weapon.evolutionItem != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33059669)
                        ) {
                            Text(
                                "⚡ Fusão: Funde com ${weapon.evolutionItem.displayName} ➔ ${weapon.evolvedWeapon.displayName}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6EE7B7),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                } else if (isStageUnlocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(weapon.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBAE6FD))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3338BDF8)
                        ) {
                            Text(
                                "DISPONÍVEL NA FASE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        "Disponível nas partidas a partir da ${stageTitleForNumber(weapon.unlockedAtStage)}! Escolha em batalha para catalogar suas propriedades.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒 Arma Bloqueada", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x22EF4444)
                        ) {
                            Text(
                                "REQUER FASE ${weapon.unlockedAtStage}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFCA5A5),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        "Conclua a fase anterior para desbloquear a ${stageTitleForNumber(weapon.unlockedAtStage)} e ter acesso a este equipamento.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PassiveLibraryCard(passive: PassiveType, isDiscovered: Boolean, timesUsed: Int, highestUnlocked: Int) {
    val isStageUnlocked = passive.unlockedAtStage <= highestUnlocked

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                when {
                    isDiscovered -> Color(0x4410B981)
                    isStageUnlocked -> Color(0x3310B981)
                    else -> Color(0x15FFFFFF)
                },
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDiscovered -> Color(0xFF1E293B)
                isStageUnlocked -> Color(0xFF141E28)
                else -> Color(0xFF0B1120)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isDiscovered -> Color(0x33059669)
                    isStageUnlocked -> Color(0x22059669)
                    else -> Color(0x15FFFFFF)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isDiscovered) {
                        val iconEmoji = when (passive) {
                            PassiveType.PASTA_FLUOR -> "✨"
                            PassiveType.ESPELHO_PRECISAO -> "🎯"
                            PassiveType.FIO_EXTRA_FORTE -> "📏"
                            PassiveType.ENXAGUANTE_MENTA -> "⏱️"
                            PassiveType.MASCARA_CIRURGICA -> "😷"
                            PassiveType.LUVAS_LATEX -> "🧤"
                            PassiveType.PASTILHA_REVELADORA -> "🧲"
                            PassiveType.VITAMINA_C -> "🍊"
                            PassiveType.OCULOS_PROTECAO -> "🥽"
                            PassiveType.RESINA_COMPOSTA -> "💎"
                            PassiveType.SELANTE_FOSSULAS -> "🛡️"
                            PassiveType.AVENTAL_CHUMBO -> "🦺"
                            PassiveType.GEL_XILITOL -> "🍬"
                            PassiveType.MATRIZ_ACO -> "⛓️"
                            PassiveType.EXTRATO_PROPOLIS -> "🍯"
                        }
                        Text(iconEmoji, fontSize = 26.sp)
                    } else {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = if (isStageUnlocked) Color(0xFF34D399) else Color(0xFF64748B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (isDiscovered) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(passive.displayName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x3334D399)
                            ) {
                                Text(
                                    "Fase ${passive.unlockedAtStage}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA7F3D0),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x3310B981)
                            ) {
                                Text(
                                    "Usado: $timesUsed",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(passive.description, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 16.sp)

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x3338BDF8)
                    ) {
                        Text(
                            "⭐ Bônus por Nível: ${passive.perLevelBonus}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7DD3FC),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (isStageUnlocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(passive.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA7F3D0))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3310B981)
                        ) {
                            Text(
                                "DISPONÍVEL NA FASE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        "Disponível para coletar em partidas a partir da ${stageTitleForNumber(passive.unlockedAtStage)}! Equipe-o para catalogar suas propriedades.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒 Equipamento Bloqueado", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x22EF4444)
                        ) {
                            Text(
                                "REQUER FASE ${passive.unlockedAtStage}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFCA5A5),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        "Complete a fase anterior para liberar a ${stageTitleForNumber(passive.unlockedAtStage)} e ter este item nas opções de level up.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FusionLibraryCard(evolution: EvolutionType, isDiscovered: Boolean, timesUsed: Int, highestUnlocked: Int) {
    val isStageUnlocked = evolution.unlockedAtStage <= highestUnlocked

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                2.dp,
                when {
                    isDiscovered -> Color(0xFFF59E0B)
                    isStageUnlocked -> Color(0x44F59E0B)
                    else -> Color(0x15FFFFFF)
                },
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDiscovered -> Color(0xFF1E1B4B)
                isStageUnlocked -> Color(0xFF181830)
                else -> Color(0xFF0B1120)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(14.dp),
                color = when {
                    isDiscovered -> Color(0xFF4338CA)
                    isStageUnlocked -> Color(0x224338CA)
                    else -> Color(0x15FFFFFF)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isDiscovered) {
                        Text("⚡", fontSize = 28.sp)
                    } else {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = if (isStageUnlocked) Color(0xFFFBBF24) else Color(0xFF64748B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (isDiscovered) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(evolution.displayName, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFFFDE047))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFD97706)
                        ) {
                            Text(
                                "FUSÃO LENDÁRIA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(evolution.description, fontSize = 12.sp, color = Color(0xFFE2E8F0), lineHeight = 16.sp)

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x447C3AED)
                    ) {
                        Text(
                            "🧪 Receita: ${evolution.baseWeapon.displayName} (MAX) + ${evolution.requiredPassive.displayName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC084FC),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("💥 Dano: ${evolution.damage.toInt()}", fontSize = 11.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
                        Text("⏱️ Cooldown: ${evolution.cooldownSeconds}s", fontSize = 11.sp, color = Color(0xFF86EFAC), fontWeight = FontWeight.Bold)
                        Text("📏 Raio: ${evolution.rangeRadius.toInt()}px", fontSize = 11.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold)
                    }
                } else if (isStageUnlocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(evolution.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33F59E0B)
                        ) {
                            Text(
                                "RECEITA DISPONÍVEL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE047),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        "Funda ${evolution.baseWeapon.displayName} (Nível 5) + ${evolution.requiredPassive.displayName} em uma partida para registrar esta fusão lendária!",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 15.sp
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒 Fusão Bloqueada", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x22EF4444)
                        ) {
                            Text(
                                "REQUER FASE ${evolution.unlockedAtStage}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFCA5A5),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        "Desbloqueie a ${stageTitleForNumber(evolution.unlockedAtStage)} para ter acesso aos equipamentos necessários para criar esta fusão lendária.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
