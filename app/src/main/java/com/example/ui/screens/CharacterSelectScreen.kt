package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CharacterId
import com.example.ui.graphics.DentalCanvasRenderer
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSelectScreen(viewModel: GameViewModel) {
    BackHandler {
        viewModel.navigateTo(Screen.Menu)
    }

    val selectedChar by viewModel.selectedCharacter.collectAsState()
    val unlockedList by viewModel.unlockedCharacters.collectAsState()
    val meta by viewModel.playerMeta.collectAsState()
    val characters = CharacterId.values()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escolher Herói Odonto", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(characters) { charItem ->
                val isUnlocked = charItem == CharacterId.DRA_ALINNY ||
                    unlockedList.any { it.characterId == charItem.name && it.isUnlocked }
                val isSelected = selectedChar == charItem

                CharacterCard(
                    charItem = charItem,
                    isSelected = isSelected,
                    isUnlocked = isUnlocked,
                    meta = meta,
                    onSelect = {
                        if (isUnlocked) {
                            viewModel.selectCharacter(charItem)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CharacterCard(
    charItem: CharacterId,
    isSelected: Boolean,
    isUnlocked: Boolean,
    meta: com.example.data.local.PlayerMetaEntity?,
    onSelect: () -> Unit
) {
    val borderColor = when {
        isSelected -> Color(0xFF38BDF8)
        isUnlocked -> Color(0x33FFFFFF)
        else -> Color(0x11FFFFFF)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isSelected) 3.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(enabled = isUnlocked) { onSelect() }
            .testTag("character_card_${charItem.name}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF182030)
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
                    // Full Fidelity Cartoon Character Avatar Badge
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (isSelected) Color(0xFF38BDF8) else Color(0x5538BDF8)
                        ),
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.size(52.dp)) {
                                DentalCanvasRenderer.drawPlayer(
                                    drawScope = this,
                                    character = charItem,
                                    x = size.width / 2f,
                                    y = size.height / 2f + 4f,
                                    time = 0f
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = charItem.displayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = charItem.title,
                            fontSize = 12.sp,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Selection or Lock indicator
                if (!isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33EF4444)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bloqueado", fontSize = 11.sp, color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10B981),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = charItem.description,
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Special Trait Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x2238BDF8),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("✨", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        charItem.specialTraitDescription,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7DD3FC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats breakdown
            StatRow("Vida Máxima (HP)", "${charItem.baseHp.toInt()}", charItem.baseHp / 160f, Color(0xFFEF4444))
            Spacer(modifier = Modifier.height(4.dp))
            StatRow("Velocidade", "${(charItem.baseSpeed * 100).toInt()}%", charItem.baseSpeed / 1.5f, Color(0xFF10B981))
            Spacer(modifier = Modifier.height(4.dp))
            StatRow("Dano Base", "${(charItem.baseDamageMultiplier * 100).toInt()}%", charItem.baseDamageMultiplier / 1.5f, Color(0xFFF59E0B))
            Spacer(modifier = Modifier.height(4.dp))
            StatRow("Raio de Coleta", "${charItem.baseMagnetRadius.toInt()} px", charItem.baseMagnetRadius / 250f, Color(0xFF8B5CF6))
            Spacer(modifier = Modifier.height(4.dp))
            StatRow("Arma Inicial", charItem.startingWeapon.displayName, 1.0f, Color(0xFF06B6D4), isTextOnly = true)

            // Unlock condition if locked
            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x33DC2626),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66EF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        val conditionText = when (charItem) {
                            CharacterId.LORENZO -> "Elimine 1.000 germes (Atual: ${meta?.totalGermsEliminated ?: 0}/1.000)"
                            CharacterId.RAYNARA -> "Vença a Fase 1 e derrote a Cárie Gigante!"
                            CharacterId.JULIO -> "Vença a Fase 5 e derrote o Tártaro Titânico!"
                            CharacterId.RICK -> "Colete 500 itens de higiene bucal (Atual: ${meta?.totalLootCollected ?: 0}/500)"
                            CharacterId.BART -> "Faça uma Fusão Lendária (ex: Escova Ultrassônica ou Fio Laser)"
                            else -> ""
                        }
                        Text(
                            text = "🔒 Critério de Desbloqueio:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFCA5A5)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = conditionText,
                            fontSize = 11.sp,
                            color = Color(0xFFFED7AA),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, valueStr: String, fraction: Float, color: Color, isTextOnly: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8))
        if (isTextOnly) {
            Text(valueStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { fraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .width(90.dp)
                        .height(6.dp),
                    color = color,
                    trackColor = Color(0x33FFFFFF)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(valueStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(36.dp))
            }
        }
    }
}
