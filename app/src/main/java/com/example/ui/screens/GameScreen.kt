package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ActivePassive
import com.example.data.model.ActiveWeapon
import com.example.data.model.EvolutionType
import com.example.data.model.GameState
import com.example.data.model.LevelUpOption
import com.example.data.model.PassiveType
import com.example.data.model.WeaponType
import com.example.ui.graphics.DentalCanvasRenderer
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.Screen
import kotlin.math.sqrt

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val engine = viewModel.gameEngine ?: return
    val gameState by viewModel.activeGameState.collectAsState()
    val state = gameState ?: return

    BackHandler {
        viewModel.togglePause()
    }

    // Virtual Joystick State
    var joystickActive by remember { mutableStateOf(false) }
    var joystickBaseX by remember { mutableFloatStateOf(0f) }
    var joystickBaseY by remember { mutableFloatStateOf(0f) }
    var joystickThumbX by remember { mutableFloatStateOf(0f) }
    var joystickThumbY by remember { mutableFloatStateOf(0f) }
    val maxRadius = 140f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        joystickActive = true
                        joystickBaseX = offset.x
                        joystickBaseY = offset.y
                        joystickThumbX = offset.x
                        joystickThumbY = offset.y
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        joystickThumbX += dragAmount.x
                        joystickThumbY += dragAmount.y

                        var dx = joystickThumbX - joystickBaseX
                        var dy = joystickThumbY - joystickBaseY
                        val dist = sqrt(dx * dx + dy * dy)

                        if (dist > maxRadius) {
                            dx = (dx / dist) * maxRadius
                            dy = (dy / dist) * maxRadius
                            joystickThumbX = joystickBaseX + dx
                            joystickThumbY = joystickBaseY + dy
                        }

                        val normX = (dx / maxRadius).coerceIn(-1f, 1f)
                        val normY = (dy / maxRadius).coerceIn(-1f, 1f)
                        viewModel.setVirtualJoystickDirection(normX, normY)
                    },
                    onDragEnd = {
                        joystickActive = false
                        viewModel.setVirtualJoystickDirection(0f, 0f)
                    },
                    onDragCancel = {
                        joystickActive = false
                        viewModel.setVirtualJoystickDirection(0f, 0f)
                    }
                )
            }
    ) {
        val snapshot = engine.getRenderSnapshot()

        // 1. Game Canvas Renderer
        Canvas(modifier = Modifier.fillMaxSize()) {
            DentalCanvasRenderer.renderGameWorld(
                drawScope = this,
                gameState = state,
                enemies = snapshot.enemies,
                projectiles = snapshot.projectiles,
                lootItems = snapshot.lootItems,
                damageNumbers = snapshot.damageNumbers,
                particles = snapshot.particles
            )
        }

        // 2. Virtual Joystick Visual Overlay
        if (joystickActive) {
            // Base Circle
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (joystickBaseX - 60f).toInt(),
                            (joystickBaseY - 60f).toInt()
                        )
                    }
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0x44FFFFFF))
                    .border(2.dp, Color(0x8838BDF8), CircleShape)
            )
            // Thumb Stick
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (joystickThumbX - 25f).toInt(),
                            (joystickThumbY - 25f).toInt()
                        )
                    }
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC0284C7))
                    .border(2.dp, Color.White, CircleShape)
            )
        }

        // 3. Top HUD: XP Bar, Level, Health, Timer, Kills, Coins, Pause
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // XP Progress Bar
            val xpFraction = (state.currentXp.toFloat() / state.xpForNextLevel.toFloat()).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0284C7),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        "NV. ${state.level}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                LinearProgressIndicator(
                    progress = { xpFraction },
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFF38BDF8),
                    trackColor = Color(0x55000000)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Stats Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // HP Bar
                val hpFraction = (state.playerHp / state.playerMaxHp).coerceIn(0f, 1f)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x88000000)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("❤️", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        LinearProgressIndicator(
                            progress = { hpFraction },
                            modifier = Modifier
                                .width(80.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (hpFraction > 0.3f) Color(0xFF10B981) else Color(0xFFEF4444),
                            trackColor = Color(0x44FFFFFF)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${state.playerHp.toInt()}/${state.playerMaxHp.toInt()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Survival Timer
                val totalSec = state.gameTimeSeconds.toInt()
                val minutes = totalSec / 60
                val seconds = totalSec % 60
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x88000000)
                ) {
                    Text(
                        timeFormatted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                // Kills & Coins
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x88000000)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🦠", fontSize = 13.sp)
                        Text("${state.kills}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🦷", fontSize = 13.sp)
                        Text("${state.dentalCoins}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                    }
                }

                // Pause Button
                IconButton(
                    onClick = { viewModel.togglePause() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0x88000000), CircleShape)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pausar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Boss Bar if Boss is Active or Boss Countdown
            if (state.activeBossName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xCC7F1D1D),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Dangerous, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "CHEFE: ${state.activeBossName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { state.activeBossHpFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFEF4444),
                            trackColor = Color(0x55000000)
                        )
                    }
                }
            } else {
                val gameTime = state.gameTimeSeconds
                val nextBossSeconds = when {
                    gameTime < 150f -> (150f - gameTime).toInt()
                    gameTime < 270f -> (270f - gameTime).toInt()
                    else -> 0
                }
                val isElitePhase = gameTime in 210f..269f

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isElitePhase) Color(0xDD7C2D12) else Color(0x66000000),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isElitePhase) Color(0xFFF97316) else Color(0x33FFFFFF)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (isElitePhase) "💀 HORDA ELITE PRÓ-CHEFE!" else "⏰ Próximo Chefe em:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isElitePhase) Color(0xFFFED7AA) else Color(0xFF93C5FD)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val bMin = nextBossSeconds / 60
                            val bSec = nextBossSeconds % 60
                            Text(
                                String.format("%02d:%02d", bMin, bSec),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isElitePhase) Color(0xFFFDE047) else Color.White
                            )
                        }
                    }
                }
            }

            // Weapons and Passives Rack (Limit max 8 items)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (w in state.weapons) {
                    WeaponMiniPip(w)
                }
                for (p in state.passives) {
                    PassiveMiniPip(p)
                }
            }
        }

        // 4. Overlays: Level Up Choice Dialog
        if (state.isLevelingUp) {
            LevelUpOverlay(
                choices = state.levelUpChoices,
                onSelectChoice = { viewModel.selectLevelUpChoice(it) }
            )
        }

        // 5. Overlays: Chest Reward Dialog
        if (state.isOpeningChest) {
            ChestRewardOverlay(
                reward = state.chestReward,
                coins = state.chestCoins,
                onDismiss = { viewModel.dismissChestReward() }
            )
        }

        // 6. Overlays: Pause Menu
        if (state.isPaused && !state.isLevelingUp && !state.isOpeningChest) {
            PauseMenuOverlay(
                gameState = state,
                viewModel = viewModel,
                onResume = { viewModel.togglePause() },
                onQuit = { viewModel.navigateTo(Screen.Menu) }
            )
        }
    }
}

@Composable
fun WeaponMiniPip(weapon: ActiveWeapon) {
    val isEvo = weapon.isEvolved
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isEvo) Color(0xFF0D9488) else Color(0x880F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isEvo) Color(0xFF38BDF8) else Color(0x44FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val emoji = when (weapon.type) {
                WeaponType.ESCOVA_DENTE -> if (isEvo) "⚡🪥" else "🪥"
                WeaponType.FIO_DENTAL -> if (isEvo) "⚡🧵" else "🧵"
                WeaponType.JATO_BICARBONATO -> if (isEvo) "⚡🌊" else "💧"
                WeaponType.BROCA_ODONTO -> if (isEvo) "⚡🔩" else "🔩"
                WeaponType.ENXAGUANTE_AURA -> if (isEvo) "⚡❄️" else "🧴"
                WeaponType.LUZ_FOTOPOLIMERIZADORA -> if (isEvo) "⚡☀️" else "🔦"
                WeaponType.PASTA_EXPLOSIVA -> if (isEvo) "⚡🍬" else "💣"
                WeaponType.ESPELHO_CLINICO -> if (isEvo) "⚡🔮" else "🪞"
                WeaponType.SERINGA_ANESTESICA -> if (isEvo) "⚡🌫️" else "💉"
                WeaponType.SUGADOR_SALIVA -> if (isEvo) "⚡🌀" else "🌪️"
                WeaponType.CURETA_GRACEY -> if (isEvo) "⚡⚔️" else "🗡️"
                WeaponType.SPRAY_CLOREXIDINA -> if (isEvo) "⚡🍯" else "💨"
                WeaponType.FITA_EXPANSIVA -> if (isEvo) "⚡🕸️" else "🎗️"
                WeaponType.ULTRASSOM_ODONTO -> if (isEvo) "⚡💥" else "🔊"
                WeaponType.PINCA_CIRURGICA -> if (isEvo) "⚡🦾" else "🦀"
            }
            Text(emoji, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                if (isEvo) "MAX" else "L${weapon.level}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isEvo) Color(0xFF67E8F9) else Color.White
            )
        }
    }
}

@Composable
fun PassiveMiniPip(passive: ActivePassive) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0x881E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val emoji = when (passive.type) {
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
            Text(emoji, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(2.dp))
            Text("L${passive.level}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBAE6FD))
        }
    }
}

@Composable
fun LevelUpOverlay(
    choices: List<LevelUpOption>,
    onSelectChoice: (LevelUpOption) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "levelup")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF0284C7)),
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "SUBIU DE NÍVEL!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFDE047),
                    letterSpacing = 2.sp,
                    modifier = Modifier.scale(glow)
                )
                Text(
                    "Escolha um poder de higiene bucal:",
                    fontSize = 13.sp,
                    color = Color(0xFFBAE6FD),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    choices.forEach { option ->
                        LevelUpChoiceCard(option = option, onClick = { onSelectChoice(option) })
                    }
                }
            }
        }
    }
}

@Composable
fun LevelUpChoiceCard(option: LevelUpOption, onClick: () -> Unit) {
    val isEvolution = option is LevelUpOption.EvolveWeapon
    val cardColor = if (isEvolution) Color(0xFF065F46) else Color(0xFF1E293B)
    val borderColor = if (isEvolution) Color(0xFF34D399) else Color(0x3338BDF8)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isEvolution) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("level_up_option"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (isEvolution) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        "⚡ EVOLUÇÃO DISPONÍVEL! ⚡",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon
                val iconStr = when (option) {
                    is LevelUpOption.NewWeapon -> "🪥"
                    is LevelUpOption.UpgradeWeapon -> "⬆️"
                    is LevelUpOption.NewPassive -> "🛡️"
                    is LevelUpOption.UpgradePassive -> "⭐"
                    is LevelUpOption.EvolveWeapon -> "⚡"
                    is LevelUpOption.HealthBonus -> "❤️"
                    is LevelUpOption.CoinBonus -> "🦷"
                }
                Text(iconStr, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    val title = when (option) {
                        is LevelUpOption.NewWeapon -> "Nova Arma: ${option.weaponType.displayName}"
                        is LevelUpOption.UpgradeWeapon -> "${option.weapon.type.displayName} (Nível ${option.weapon.level + 1})"
                        is LevelUpOption.NewPassive -> "Novo Item: ${option.passiveType.displayName}"
                        is LevelUpOption.UpgradePassive -> "${option.passive.type.displayName} (Nível ${option.passive.level + 1})"
                        is LevelUpOption.EvolveWeapon -> option.evolution.displayName
                        is LevelUpOption.HealthBonus -> "Enxágue Curativo (+${option.healAmount.toInt()} HP)"
                        is LevelUpOption.CoinBonus -> "Bolsa de Dentes de Ouro (+${option.coinAmount})"
                    }

                    val desc = when (option) {
                        is LevelUpOption.NewWeapon -> option.weaponType.description
                        is LevelUpOption.UpgradeWeapon -> "+25% Dano e alcance ampliado."
                        is LevelUpOption.NewPassive -> option.passiveType.description
                        is LevelUpOption.UpgradePassive -> option.passive.type.perLevelBonus
                        is LevelUpOption.EvolveWeapon -> option.evolution.description
                        is LevelUpOption.HealthBonus -> "Recupera imediatamente parte da vida do seu herói."
                        is LevelUpOption.CoinBonus -> "Adiciona moedas de ouro para gastar em melhorias na clínica."
                    }

                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(desc, fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 15.sp)
                }
            }
        }
    }
}

@Composable
fun ChestRewardOverlay(
    reward: LevelUpOption?,
    coins: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFF59E0B)),
            shadowElevation = 20.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "ESTOJO DE OURO!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFDE047)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("🎁", fontSize = 60.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "+$coins Dentes de Ouro!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFBBF24)
                )

                if (reward != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Recompensa Odontológica:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(6.dp))
                    LevelUpChoiceCard(option = reward, onClick = onDismiss)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("COLETAR RECOMPENSA", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PauseMenuOverlay(
    gameState: GameState,
    viewModel: GameViewModel,
    onResume: () -> Unit,
    onQuit: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF0284C7)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("JOGO PAUSADO", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(modifier = Modifier.height(14.dp))

                // Current Run Stats
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x33FFFFFF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Estatísticas da Partida:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Germes Eliminados: ${gameState.kills}", fontSize = 12.sp, color = Color.White)
                        Text("• Tempo de Sobrevivência: ${gameState.gameTimeSeconds.toInt()}s", fontSize = 12.sp, color = Color.White)
                        Text("• Nível Alcançado: ${gameState.level}", fontSize = 12.sp, color = Color.White)
                        Text("• Moedas Ganhas: ${gameState.dentalCoins}", fontSize = 12.sp, color = Color(0xFFFDE047))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Efeitos Sonoros", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    IconButton(
                        onClick = {
                            viewModel.soundManager.isSfxEnabled = !viewModel.soundManager.isSfxEnabled
                        }
                    ) {
                        Icon(
                            imageVector = if (viewModel.soundManager.isSfxEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CONTINUAR PARTIDA", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onQuit,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SAIR PARA O MENU", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
