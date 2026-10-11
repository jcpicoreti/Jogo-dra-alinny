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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
                .statusBarsPadding()
                .padding(top = 18.dp, bottom = 8.dp)
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

            Spacer(modifier = Modifier.height(10.dp))

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
                            .testTag("tab_${tab.name.lowercase()}")
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
                                timesUsed = times
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
                                timesUsed = times
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
                                timesUsed = times
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeaponLibraryCard(weapon: WeaponType, isDiscovered: Boolean, timesUsed: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weapon_card_${weapon.name}")
            .border(
                1.dp,
                if (isDiscovered) Color(0x4438BDF8) else Color(0x2238BDF8),
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDiscovered) Color(0xFF1E293B) else Color(0xFF141C2E)
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
                color = if (isDiscovered) Color(0x330284C7) else Color(0x1F0284C7)
            ) {
                Box(contentAlignment = Alignment.Center) {
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
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(weapon.displayName, modifier = Modifier.weight(1f, fill = false), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isDiscovered) {
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
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x2238BDF8)
                            ) {
                                Text(
                                    "Equipamento Clínico",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF7DD3FC),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
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
            }
        }
    }
}

@Composable
fun PassiveLibraryCard(passive: PassiveType, isDiscovered: Boolean, timesUsed: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("passive_card_${passive.name}")
            .border(
                1.dp,
                if (isDiscovered) Color(0x4410B981) else Color(0x2210B981),
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDiscovered) Color(0xFF1E293B) else Color(0xFF141E28)
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
                color = if (isDiscovered) Color(0x33059669) else Color(0x1F059669)
            ) {
                Box(contentAlignment = Alignment.Center) {
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
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(passive.displayName, modifier = Modifier.weight(1f, fill = false), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isDiscovered) {
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
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x2210B981)
                            ) {
                                Text(
                                    "Item Clínico",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFA7F3D0),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
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
            }
        }
    }
}

@Composable
fun FusionLibraryCard(evolution: EvolutionType, isDiscovered: Boolean, timesUsed: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fusion_card_${evolution.name}")
            .border(
                2.dp,
                if (isDiscovered) Color(0xFFF59E0B) else Color(0x66F59E0B),
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDiscovered) Color(0xFF1E1B4B) else Color(0xFF151433)
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
                color = if (isDiscovered) Color(0xFF4338CA) else Color(0x334338CA)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val fusionEmoji = when (evolution) {
                        EvolutionType.ESCOVA_ULTRASSONICA -> "⚡🪥"
                        EvolutionType.FIO_LASER_TITANIO -> "⚡🧵"
                        EvolutionType.TSUNAMI_BICARBONATO -> "🌊💧"
                        EvolutionType.BROCA_INDELAVEL -> "⚡🔩"
                        EvolutionType.VENTANIA_CRIOGENICA -> "❄️🧴"
                        EvolutionType.RAIO_SOLAR_ODONTO -> "☀️🔦"
                        EvolutionType.BOMBARDEIO_XILITOL -> "💥💣"
                        EvolutionType.ESPELHO_PRISMATICO -> "🌈🪞"
                        EvolutionType.NEBULIZADOR_CIRURGICO -> "🌫️💉"
                        EvolutionType.VORTEX_GRAVITACIONAL -> "🌌🌪️"
                        EvolutionType.GUILHOTINA_PERIODONTAL -> "⚔️🗡️"
                        EvolutionType.NEVOA_BIOATIVA -> "🍯💨"
                        EvolutionType.REDE_CONTENCAO_TITANIO -> "🕸️🎗️"
                        EvolutionType.EMISSOR_CAVITACAO -> "🔊💥"
                        EvolutionType.GARRA_EXTRATORA_PESADA -> "🦾🦀"
                    }
                    Text(fusionEmoji, fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            val baseWeaponName = evolution.baseWeapon.displayName
            val passiveName = evolution.requiredPassive.displayName

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(evolution.displayName, modifier = Modifier.weight(1f, fill = false), fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFFFDE047))
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                        if (isDiscovered && timesUsed > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x33F59E0B)
                            ) {
                                Text(
                                    "Usado: $timesUsed",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDE047),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
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
                        "🧪 Receita: $baseWeaponName (MAX) + $passiveName",
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

                if (!isDiscovered) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "💡 Funda $baseWeaponName (Nível 5) + $passiveName em batalha para registrar!",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}
