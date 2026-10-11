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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun MatchEndScreen(
    isVictory: Boolean,
    kills: Int,
    timeSec: Int,
    coins: Int,
    level: Int,
    viewModel: GameViewModel
) {
    BackHandler {
        viewModel.navigateTo(Screen.Menu)
    }

    var playerName by remember { mutableStateOf("") }
    var isScoreSaved by remember { mutableStateOf(false) }

    val totalScore = kills * 10 + timeSec * 5 + level * 50

    val minutes = timeSec / 60
    val seconds = timeSec % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Trophy / Status Header
            Surface(
                shape = CircleShape,
                color = if (isVictory) Color(0xFF10B981) else Color(0xFFDC2626),
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (isVictory) "🏆" else "🦷", fontSize = 48.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isVictory) "VITÓRIA ODONTOLÓGICA!" else "FIM DO PLANTÃO!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = if (isVictory) Color(0xFF34D399) else Color(0xFFF87171),
                letterSpacing = 1.5.sp
            )

            Text(
                text = if (isVictory) "A boca do paciente está 100% limpa e protegida!" else "Os germes venceram desta vez... tente novamente!",
                fontSize = 13.sp,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            if (isVictory) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x3310B981),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF34D399)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔓", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "NOVA FASE & EQUIPAMENTOS DESBLOQUEADOS!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF34D399)
                            )
                            Text(
                                "Você liberou a próxima fase e novos itens para a sua clínica e biblioteca!",
                                fontSize = 11.sp,
                                color = Color(0xFFBAE6FD)
                            )
                        }
                    }
                }
            }

            // Results Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("RESUMO DA BATALHA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.height(12.dp))

                    ResultRow("🦠 Bactérias Eliminadas", "$kills")
                    Spacer(modifier = Modifier.height(8.dp))
                    ResultRow("⏱️ Tempo Sobrevivido", timeFormatted)
                    Spacer(modifier = Modifier.height(8.dp))
                    ResultRow("⭐ Nível de Higiene", "$level")
                    Spacer(modifier = Modifier.height(8.dp))
                    ResultRow("🦷 Dentes de Ouro Ganhos", "+$coins")
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x3338BDF8),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("PONTUAÇÃO TOTAL", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text(
                                "$totalScore pts",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = Color(0xFFFDE047)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save to Local Ranking Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("SALVAR NO RANKING LOCAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isScoreSaved) {
                        OutlinedTextField(
                            value = playerName,
                            onValueChange = { playerName = it },
                            label = { Text("Seu Nome ou Apelido") },
                            placeholder = { Text("Digite seu nome...", color = Color(0xFF64748B)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("player_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0x66FFFFFF),
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color(0xFF94A3B8)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.saveMatchScore(playerName, kills, timeSec, level)
                                isScoreSaved = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_score_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SALVAR PONTUAÇÃO", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x3310B981),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✅", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Pontuação salva com sucesso no Ranking!",
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.navigateTo(Screen.Rankings) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("VER RANKING COMPLETO", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Button(
                onClick = { viewModel.startMatch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("play_again_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("JOGAR NOVAMENTE", fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { viewModel.navigateTo(Screen.Menu) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("main_menu_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VOLTAR AO MENU", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}
