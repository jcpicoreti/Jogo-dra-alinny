package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CharacterId
import com.example.data.model.StageId
import com.example.ui.graphics.DentalCanvasRenderer
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun MainMenuScreen(viewModel: GameViewModel) {
    val context = LocalContext.current
    val meta by viewModel.playerMeta.collectAsState()
    val selectedChar by viewModel.selectedCharacter.collectAsState()
    val selectedStage by viewModel.selectedStage.collectAsState()
    val highestUnlocked = meta?.highestStageUnlocked ?: 1
    val isInfiniteUnlocked = highestUnlocked >= 6

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0369A1),
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
                .padding(top = 18.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Header: Sound, Coins Pill, Stage Progress Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.soundManager.isSfxEnabled = !viewModel.soundManager.isSfxEnabled
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (viewModel.soundManager.isSfxEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Alternar Som",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x33FFFFFF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FDE047)),
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.Upgrades) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🦷", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "${meta?.dentalCoins ?: 0}",
                            color = Color(0xFFFDE047),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x2238BDF8),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3338BDF8))
                ) {
                    Text(
                        text = "Fase $highestUnlocked / 6",
                        color = Color(0xFFBAE6FD),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // 2. Hero Mascot Showcase & Title Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hero Avatar (Dra. Alinny)
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(2.5.dp, Color(0xFF38BDF8)),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(60.dp)
                        .scale(pulseScale)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(54.dp)) {
                            // Sparkles
                            drawCircle(color = Color(0x4438BDF8), radius = size.width * 0.45f, center = center)
                            DentalCanvasRenderer.drawPlayer(
                                drawScope = this,
                                character = CharacterId.DRA_ALINNY,
                                x = size.width / 2f,
                                y = size.height / 2f + 4f,
                                time = 0f
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "DRA. ALINNY",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "VS OS GERMES DO MAL",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFDE047),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "SURVIVORS ODONTOLÓGICO 2D",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFBAE6FD),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // 3. Primary CTA: JOGAR PARTIDA
            Button(
                onClick = { viewModel.startMatch(selectedChar, selectedStage) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("play_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "JOGAR PARTIDA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }

            // 4. Hero & Stage Quick Selection Row (Side by Side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Character Selector Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(Screen.CharacterSelect) }
                        .testTag("character_select_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3338BDF8))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("HERÓI", fontSize = 9.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold)
                            Text(
                                selectedChar.displayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0284C7)
                        ) {
                            Text(
                                "Trocar",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Stage Selector Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(Screen.StageSelect) }
                        .testTag("stage_select_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3338BDF8))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("FASE ATUAL", fontSize = 9.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold)
                            Text(
                                selectedStage.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0284C7)
                        ) {
                            Text(
                                "Fases",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // 5. Special Mode: Plantão Noturno Infinito
            Button(
                onClick = {
                    if (isInfiniteUnlocked) {
                        viewModel.startMatch(selectedChar, StageId.STAGE_INFINITE)
                    } else {
                        viewModel.navigateTo(Screen.StageSelect)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("infinite_mode_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isInfiniteUnlocked) Color(0xFF8B5CF6) else Color(0xFF334155)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isInfiniteUnlocked) {
                        Icon(Icons.Default.AllInclusive, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "PLANTÃO NOTURNO INFINITO",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "MODO INFINITO (Complete Fase 5 para liberar)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            // 6. Main Options 2x2 Grid (Biblioteca, Clínica, Recordes, Conquistas)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactMenuCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("library_button"),
                        title = "BIBLIOTECA",
                        subtitle = "45 Poderes & Fusões",
                        iconEmoji = "📚",
                        accentColor = Color(0xFF0284C7),
                        onClick = { viewModel.navigateTo(Screen.Library) }
                    )

                    CompactMenuCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upgrades_button"),
                        title = "CLÍNICA",
                        subtitle = "Upgrades de Poder",
                        iconEmoji = "🏥",
                        accentColor = Color(0xFF059669),
                        onClick = { viewModel.navigateTo(Screen.Upgrades) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactMenuCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rankings_button"),
                        title = "RECORDES",
                        subtitle = "Placar e Melhores Tempos",
                        iconEmoji = "🏆",
                        accentColor = Color(0xFFD97706),
                        onClick = { viewModel.navigateTo(Screen.Rankings) }
                    )

                    CompactMenuCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("achievements_button"),
                        title = "CONQUISTAS",
                        subtitle = "22 Troféus Bucais",
                        iconEmoji = "⭐",
                        accentColor = Color(0xFF7C3AED),
                        onClick = { viewModel.navigateTo(Screen.Achievements) }
                    )
                }
            }

            // 7. Instagram Link to Dra. Alinny Gobetti
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("instagram_link_button")
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/alinny.gobetti/"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // ignore fallback
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x33E1306C)),
                border = androidx.compose.foundation.BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("📸", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Siga Dra. Alinny Gobetti no Instagram:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "@alinny.gobetti",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF9A8D4)
                    )
                }
            }

            // 8. Compact Gamepad Footer Tag
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0x18000000),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🎮", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Suporte total a controles Bluetooth / USB",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CompactMenuCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    iconEmoji: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(58.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, accentColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.25f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(iconEmoji, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 9.sp,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
