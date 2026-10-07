package com.example.ui.graphics

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.data.model.CharacterId
import com.example.data.model.DamageNumber
import com.example.data.model.EnemyInstance
import com.example.data.model.EnemyType
import com.example.data.model.EvolutionType
import com.example.data.model.GameState
import com.example.data.model.LootInstance
import com.example.data.model.LootType
import com.example.data.model.PassiveType
import com.example.data.model.ProjectileInstance
import com.example.data.model.StageId
import com.example.data.model.VisualParticle
import com.example.data.model.WeaponType
import com.example.game.GameEngine
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object DentalCanvasRenderer {

    fun renderGameWorld(
        drawScope: DrawScope,
        gameState: GameState,
        enemies: List<EnemyInstance>,
        projectiles: List<ProjectileInstance>,
        lootItems: List<LootInstance>,
        damageNumbers: List<DamageNumber>,
        particles: List<VisualParticle>
    ) {
        val canvasWidth = drawScope.size.width
        val canvasHeight = drawScope.size.height

        // Camera offset centered on player with screen shake
        val cameraX = canvasWidth / 2f - gameState.playerX + gameState.screenShakeX
        val cameraY = canvasHeight / 2f - gameState.playerY + gameState.screenShakeY

        // 1. Draw Themed Background Arena with Rich Patterns
        drawArenaFloor(drawScope, gameState.stageId, cameraX, cameraY, canvasWidth, canvasHeight, gameState.gameTimeSeconds)

        // 1.2 Draw Stage Boundary Limits (Visible Enamel Barrier Walls & Danger Striping)
        drawStageBoundaries(drawScope, cameraX, cameraY, canvasWidth, canvasHeight, gameState.gameTimeSeconds, gameState.stageId)

        // 1.5 Radiant Clean Enamel Halo under player (Lighting ambiance)
        val playerPx = gameState.playerX + cameraX
        val playerPy = gameState.playerY + cameraY
        val haloPulse = 65f + (sin((gameState.gameTimeSeconds * 5f).toDouble()) * 8f).toFloat()
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x3538BDF8), Color(0x1806B6D4), Color.Transparent),
                center = Offset(playerPx, playerPy),
                radius = haloPulse
            ),
            radius = haloPulse,
            center = Offset(playerPx, playerPy)
        )

        // 2. Draw Loot Items on Ground
        lootItems.forEach { loot ->
            val screenX = loot.x + cameraX
            val screenY = loot.y + cameraY
            if (screenX >= -60f && screenX <= canvasWidth + 60f && screenY >= -60f && screenY <= canvasHeight + 60f) {
                drawLootItem(drawScope, loot, screenX, screenY, gameState.gameTimeSeconds)
            }
        }

        // 3. Draw Weapon Active Auras (underneath player and enemies)
        val auraWeapon = gameState.weapons.find { it.type == WeaponType.ENXAGUANTE_AURA }
        if (auraWeapon != null) {
            val px = gameState.playerX + cameraX
            val py = gameState.playerY + cameraY
            val auraRadius = auraWeapon.type.rangeRadius * (1f + auraWeapon.level * 0.15f)
            
            // Outer refreshing mint pool
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x4406B6D4), Color(0x220284C7), Color(0x050369A1), Color.Transparent),
                    center = Offset(px, py),
                    radius = auraRadius
                ),
                radius = auraRadius,
                center = Offset(px, py)
            )
            drawScope.drawCircle(
                color = Color(0x8867E8F9),
                radius = auraRadius,
                center = Offset(px, py),
                style = Stroke(width = 3f)
            )
            // Floating mint leaves in aura
            for (i in 0 until 4) {
                val leafAng = (gameState.gameTimeSeconds * 60f + i * 90f) % 360f
                val leafRad = Math.toRadians(leafAng.toDouble())
                val lx = px + (cos(leafRad) * auraRadius * 0.7f).toFloat()
                val ly = py + (sin(leafRad) * auraRadius * 0.7f).toFloat()
                drawScope.drawOval(
                    color = Color(0xAA10B981),
                    topLeft = Offset(lx - 6f, ly - 3f),
                    size = Size(12f, 6f)
                )
            }
        }

        // Ultrasonic Brush Evolution Pulsing Field
        val ultraBrush = gameState.weapons.find { it.isEvolved && it.evolvedType == EvolutionType.ESCOVA_ULTRASSONICA }
        if (ultraBrush != null) {
            val px = gameState.playerX + cameraX
            val py = gameState.playerY + cameraY
            val pulseRadius = 160f + (14.0 * sin((gameState.gameTimeSeconds * 14f).toDouble())).toFloat()
            
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x5567E8F9), Color(0x3306B6D4), Color(0x110284C7), Color.Transparent),
                    center = Offset(px, py),
                    radius = pulseRadius
                ),
                radius = pulseRadius,
                center = Offset(px, py)
            )
            drawScope.drawCircle(
                color = Color(0xCCA5F3FC),
                radius = pulseRadius,
                center = Offset(px, py),
                style = Stroke(width = 4f)
            )
            drawScope.drawCircle(
                color = Color(0x8838BDF8),
                radius = pulseRadius * 0.65f,
                center = Offset(px, py),
                style = Stroke(width = 2.5f)
            )
        }

        // 4. Draw Enemies & Bosses
        enemies.forEach { enemy ->
            val screenX = enemy.x + cameraX
            val screenY = enemy.y + cameraY
            if (screenX >= -120f && screenX <= canvasWidth + 120f && screenY >= -120f && screenY <= canvasHeight + 120f) {
                drawEnemy(drawScope, enemy, screenX, screenY, gameState.gameTimeSeconds, gameState.isFreezeActive)
            }
        }

        // 5. Draw Player Character
        val playerScreenX = gameState.playerX + cameraX
        val playerScreenY = gameState.playerY + cameraY
        drawPlayer(drawScope, gameState.characterId, playerScreenX, playerScreenY, gameState.gameTimeSeconds)

        // 6. Draw Projectiles & Weapon Effects
        projectiles.forEach { p ->
            val screenX = p.x + cameraX
            val screenY = p.y + cameraY
            if (screenX >= -120f && screenX <= canvasWidth + 120f && screenY >= -120f && screenY <= canvasHeight + 120f) {
                drawProjectile(drawScope, p, screenX, screenY, gameState.gameTimeSeconds)
            }
        }

        // 7. Draw Visual Particles
        particles.forEach { pt ->
            val screenX = pt.x + cameraX
            val screenY = pt.y + cameraY
            val alpha = (pt.life / pt.maxLife).coerceIn(0f, 1f)
            drawScope.drawCircle(
                color = Color(pt.color).copy(alpha = alpha),
                radius = pt.radius * alpha,
                center = Offset(screenX, screenY)
            )
        }

        // 8. Draw Damage Numbers
        damageNumbers.forEach { dmg ->
            val screenX = dmg.x + cameraX
            val screenY = dmg.y + cameraY
            val alpha = (dmg.life / 1.0f).coerceIn(0f, 1f)
            val rad = if (dmg.isCrit) 8f else 5.5f
            drawScope.drawCircle(
                color = Color(dmg.color).copy(alpha = alpha),
                radius = rad,
                center = Offset(screenX, screenY)
            )
        }

        // 9. Vignette / Lighting atmosphere overlay
        drawScope.drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Transparent, Color(0x66000000)),
                center = Offset(canvasWidth / 2f, canvasHeight / 2f),
                radius = canvasWidth * 0.75f
            ),
            size = Size(canvasWidth, canvasHeight)
        )

        // 10. Proximity Warning HUD when near boundary limit
        val limit = GameEngine.WORLD_LIMIT
        val distToEdge = minOf(
            limit - kotlin.math.abs(gameState.playerX),
            limit - kotlin.math.abs(gameState.playerY)
        )
        if (distToEdge < 350f) {
            val warningAlpha = ((350f - distToEdge) / 350f * 0.45f).coerceIn(0f, 0.45f)
            val flash = (sin((gameState.gameTimeSeconds * 8f).toDouble()) * 0.15f + 0.85f).toFloat()
            drawScope.drawRect(
                color = Color(0xFFEF4444).copy(alpha = warningAlpha * flash),
                size = Size(canvasWidth, canvasHeight)
            )
        }
    }

    private fun drawStageBoundaries(
        drawScope: DrawScope,
        cameraX: Float,
        cameraY: Float,
        screenWidth: Float,
        screenHeight: Float,
        time: Float,
        stage: StageId
    ) {
        val limit = GameEngine.WORLD_LIMIT
        val wallThickness = 120f
        val pulse = (sin((time * 4f).toDouble()) * 0.2f + 0.8f).toFloat()

        val leftScreen = -limit + cameraX
        val rightScreen = limit + cameraX
        val topScreen = -limit + cameraY
        val bottomScreen = limit + cameraY

        val themeColor = Color(stage.themeColorHex)
        val barrierColor = Color(0xFFEF4444)

        // 1. Outside dark abyss / void
        // Left void
        if (leftScreen > 0) {
            drawScope.drawRect(
                color = Color(0xFF030712),
                topLeft = Offset(0f, 0f),
                size = Size(leftScreen, screenHeight)
            )
        }
        // Right void
        if (rightScreen < screenWidth) {
            drawScope.drawRect(
                color = Color(0xFF030712),
                topLeft = Offset(rightScreen, 0f),
                size = Size(screenWidth - rightScreen, screenHeight)
            )
        }
        // Top void
        if (topScreen > 0) {
            drawScope.drawRect(
                color = Color(0xFF030712),
                topLeft = Offset(0f, 0f),
                size = Size(screenWidth, topScreen)
            )
        }
        // Bottom void
        if (bottomScreen < screenHeight) {
            drawScope.drawRect(
                color = Color(0xFF030712),
                topLeft = Offset(0f, bottomScreen),
                size = Size(screenWidth, screenHeight - bottomScreen)
            )
        }

        // 2. Visible Barrier Forcefield Borders (Electric laser line + teeth wall barrier)
        // Left barrier wall
        if (leftScreen >= -wallThickness && leftScreen <= screenWidth + wallThickness) {
            drawScope.drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(barrierColor.copy(alpha = 0.5f * pulse), Color.Transparent),
                    startX = leftScreen - 20f,
                    endX = leftScreen + 80f
                ),
                topLeft = Offset(leftScreen - 20f, 0f),
                size = Size(100f, screenHeight)
            )
            drawScope.drawLine(
                color = barrierColor.copy(alpha = 0.9f * pulse),
                start = Offset(leftScreen, 0f),
                end = Offset(leftScreen, screenHeight),
                strokeWidth = 6f
            )
            // Hazard teeth along the wall
            var y = 0f
            while (y < screenHeight) {
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = 0.8f * pulse),
                    radius = 4f,
                    center = Offset(leftScreen, y)
                )
                y += 40f
            }
        }

        // Right barrier wall
        if (rightScreen >= -wallThickness && rightScreen <= screenWidth + wallThickness) {
            drawScope.drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, barrierColor.copy(alpha = 0.5f * pulse)),
                    startX = rightScreen - 80f,
                    endX = rightScreen + 20f
                ),
                topLeft = Offset(rightScreen - 80f, 0f),
                size = Size(100f, screenHeight)
            )
            drawScope.drawLine(
                color = barrierColor.copy(alpha = 0.9f * pulse),
                start = Offset(rightScreen, 0f),
                end = Offset(rightScreen, screenHeight),
                strokeWidth = 6f
            )
            var y = 0f
            while (y < screenHeight) {
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = 0.8f * pulse),
                    radius = 4f,
                    center = Offset(rightScreen, y)
                )
                y += 40f
            }
        }

        // Top barrier wall
        if (topScreen >= -wallThickness && topScreen <= screenHeight + wallThickness) {
            drawScope.drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(barrierColor.copy(alpha = 0.5f * pulse), Color.Transparent),
                    startY = topScreen - 20f,
                    endY = topScreen + 80f
                ),
                topLeft = Offset(0f, topScreen - 20f),
                size = Size(screenWidth, 100f)
            )
            drawScope.drawLine(
                color = barrierColor.copy(alpha = 0.9f * pulse),
                start = Offset(0f, topScreen),
                end = Offset(screenWidth, topScreen),
                strokeWidth = 6f
            )
            var x = 0f
            while (x < screenWidth) {
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = 0.8f * pulse),
                    radius = 4f,
                    center = Offset(x, topScreen)
                )
                x += 40f
            }
        }

        // Bottom barrier wall
        if (bottomScreen >= -wallThickness && bottomScreen <= screenHeight + wallThickness) {
            drawScope.drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, barrierColor.copy(alpha = 0.5f * pulse)),
                    startY = bottomScreen - 80f,
                    endY = bottomScreen + 20f
                ),
                topLeft = Offset(0f, bottomScreen - 80f),
                size = Size(screenWidth, 100f)
            )
            drawScope.drawLine(
                color = barrierColor.copy(alpha = 0.9f * pulse),
                start = Offset(0f, bottomScreen),
                end = Offset(screenWidth, bottomScreen),
                strokeWidth = 6f
            )
            var x = 0f
            while (x < screenWidth) {
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = 0.8f * pulse),
                    radius = 4f,
                    center = Offset(x, bottomScreen)
                )
                x += 40f
            }
        }
    }

    private fun drawArenaFloor(
        drawScope: DrawScope,
        stage: StageId,
        cameraX: Float,
        cameraY: Float,
        width: Float,
        height: Float,
        time: Float
    ) {
        val groundColor = Color(stage.groundColorHex)
        val themeColor = Color(stage.themeColorHex)

        // Base ground
        drawScope.drawRect(color = groundColor, size = Size(width, height))

        val tileSize = 90f
        val startX = (cameraX % tileSize) - tileSize
        val startY = (cameraY % tileSize) - tileSize

        var x = startX
        while (x < width + tileSize) {
            drawScope.drawLine(
                color = themeColor.copy(alpha = 0.15f),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 2f
            )
            x += tileSize
        }

        var y = startY
        while (y < height + tileSize) {
            drawScope.drawLine(
                color = themeColor.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 2f
            )
            y += tileSize
        }

        // Themed floor details per stage
        when (stage) {
            StageId.STAGE_1_ESMALTE -> {
                // Shiny tooth enamel sparkles on tiles
                val sparkleStep = tileSize * 2
                var sx = (cameraX % sparkleStep) - sparkleStep
                while (sx < width + sparkleStep) {
                    var sy = (cameraY % sparkleStep) - sparkleStep
                    while (sy < height + sparkleStep) {
                        drawScope.drawCircle(color = Color(0x3338BDF8), radius = 6f, center = Offset(sx + 45f, sy + 45f))
                        drawScope.drawCircle(color = Color(0x55FFFFFF), radius = 2f, center = Offset(sx + 45f, sy + 45f))
                        sy += sparkleStep
                    }
                    sx += sparkleStep
                }
            }
            StageId.STAGE_2_GENGIVA -> {
                // Soft vascular cell micro-patterns
                val step = tileSize * 1.5f
                var sx = (cameraX % step) - step
                while (sx < width + step) {
                    var sy = (cameraY % step) - step
                    while (sy < height + step) {
                        drawScope.drawOval(color = Color(0x22F43F5E), topLeft = Offset(sx + 15f, sy + 15f), size = Size(20f, 12f))
                        sy += step
                    }
                    sx += step
                }
            }
            StageId.STAGE_3_CANAL -> {
                // Pulp canal bio-tubules
                val step = tileSize * 2.5f
                var sx = (cameraX % step) - step
                while (sx < width + step) {
                    var sy = (cameraY % step) - step
                    while (sy < height + step) {
                        drawScope.drawCircle(color = Color(0x229333EA), radius = 16f, center = Offset(sx + 50f, sy + 50f))
                        drawScope.drawCircle(color = Color(0x33A855F7), radius = 8f, center = Offset(sx + 50f, sy + 50f))
                        sy += step
                    }
                    sx += step
                }
            }
            StageId.STAGE_4_APARELHO -> {
                // Metallic archwires running across floor
                val wireStep = tileSize * 3
                var sy = (cameraY % wireStep) - wireStep
                while (sy < height + wireStep) {
                    drawScope.drawLine(
                        color = Color(0x44CBD5E1),
                        start = Offset(0f, sy + 30f),
                        end = Offset(width, sy + 30f),
                        strokeWidth = 3f
                    )
                    // Brackets
                    var bx = (cameraX % (tileSize * 1.5f)) - tileSize
                    while (bx < width + tileSize) {
                        drawScope.drawRect(color = Color(0x6664748B), topLeft = Offset(bx + 20f, sy + 24f), size = Size(14f, 12f))
                        bx += (tileSize * 1.5f)
                    }
                    sy += wireStep
                }
            }
            StageId.STAGE_5_TARTARO -> {
                // Calcified stone runes and cracks
                val step = tileSize * 2f
                var sx = (cameraX % step) - step
                while (sx < width + step) {
                    var sy = (cameraY % step) - step
                    while (sy < height + step) {
                        drawScope.drawRect(color = Color(0x2278716C), topLeft = Offset(sx + 20f, sy + 20f), size = Size(32f, 32f))
                        sy += step
                    }
                    sx += step
                }
            }
            StageId.STAGE_INFINITE -> {
                // High-tech clinic linoleum with glowing blue accents
                val step = tileSize * 2f
                var sx = (cameraX % step) - step
                while (sx < width + step) {
                    var sy = (cameraY % step) - step
                    while (sy < height + step) {
                        // Blue medical cross
                        drawScope.drawRect(color = Color(0x220284C7), topLeft = Offset(sx + 35f, sy + 25f), size = Size(10f, 30f))
                        drawScope.drawRect(color = Color(0x220284C7), topLeft = Offset(sx + 25f, sy + 35f), size = Size(30f, 10f))
                        sy += step
                    }
                    sx += step
                }
            }
        }
    }

    fun drawPlayer(
        drawScope: DrawScope,
        character: CharacterId,
        x: Float,
        y: Float,
        time: Float
    ) {
        val wobble = (sin((time * 10f).toDouble()) * 2.5).toFloat()
        val breathe = (sin((time * 4f).toDouble()) * 1.0).toFloat()

        // Shadow under player with soft ambient occlusion
        drawScope.drawOval(
            color = Color(0x44000000),
            topLeft = Offset(x - 24f, y + 18f),
            size = Size(48f, 14f)
        )

        when (character) {
            CharacterId.DRA_ALINNY -> {
                // === DRA. ALINNY - HIGH-POLISH 2D CARTOON HEROINE ===
                // Hair flowing in ponytail (behind body)
                val auburnHairDark = Color(0xFF9A3412)
                val auburnHairMain = Color(0xFFC2410C)
                val auburnHairLight = Color(0xFFEA580C)

                // Large wavy ponytail curving to the side
                drawScope.drawOval(
                    color = auburnHairDark,
                    topLeft = Offset(x - 28f, y - 26f + wobble),
                    size = Size(20f, 32f)
                )
                drawScope.drawOval(
                    color = auburnHairMain,
                    topLeft = Offset(x - 26f, y - 24f + wobble),
                    size = Size(18f, 28f)
                )
                drawScope.drawOval(
                    color = auburnHairLight,
                    topLeft = Offset(x - 24f, y - 22f + wobble),
                    size = Size(10f, 18f)
                )

                // Legs / Navy trousers
                drawScope.drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(x - 10f, y + 14f + wobble),
                    size = Size(8f, 14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(x + 2f, y + 14f + wobble),
                    size = Size(8f, 14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                // White clinic sneakers
                drawScope.drawRoundRect(color = Color.White, topLeft = Offset(x - 12f, y + 25f + wobble), size = Size(10f, 6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
                drawScope.drawRoundRect(color = Color.White, topLeft = Offset(x + 2f, y + 25f + wobble), size = Size(10f, 6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))

                // Lab Coat Body (White coat over sage green scrub)
                drawScope.drawRoundRect(
                    color = Color(0xFFF1F5F9),
                    topLeft = Offset(x - 16f, y - 6f + wobble + breathe),
                    size = Size(32f, 24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                // Dark sage green scrub collar
                val scrubPath = Path().apply {
                    moveTo(x - 7f, y - 6f + wobble + breathe)
                    lineTo(x, y + 4f + wobble + breathe)
                    lineTo(x + 7f, y - 6f + wobble + breathe)
                    close()
                }
                drawScope.drawPath(scrubPath, color = Color(0xFF15803D))

                // Coat lapels & chest pocket with dental explorer
                drawScope.drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(x, y + 4f + wobble),
                    end = Offset(x, y + 18f + wobble),
                    strokeWidth = 2f
                )
                drawScope.drawRect(
                    color = Color(0xFFE2E8F0),
                    topLeft = Offset(x + 5f, y + 2f + wobble),
                    size = Size(7f, 6f)
                )
                // Dental probe tool in pocket
                drawScope.drawLine(color = Color(0xFF94A3B8), start = Offset(x + 7f, y + 2f + wobble), end = Offset(x + 7f, y - 2f + wobble), strokeWidth = 1.5f)

                // Head - Beautiful cartoon face
                val skinColor = Color(0xFFFFDFC4)
                val skinShadow = Color(0xFFF5CBAC)
                drawScope.drawCircle(color = skinShadow, radius = 15f, center = Offset(x, y - 16f + wobble))
                drawScope.drawCircle(color = skinColor, radius = 14.5f, center = Offset(x, y - 16.5f + wobble))

                // Auburn bangs & side wavy hair locks
                drawScope.drawCircle(color = auburnHairMain, radius = 16f, center = Offset(x, y - 21f + wobble))
                drawScope.drawOval(color = auburnHairMain, topLeft = Offset(x - 16f, y - 22f + wobble), size = Size(10f, 16f))
                drawScope.drawOval(color = auburnHairMain, topLeft = Offset(x + 8f, y - 22f + wobble), size = Size(10f, 16f))
                // Hair shine curve
                drawScope.drawArc(
                    color = auburnHairLight,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(x - 12f, y - 30f + wobble),
                    size = Size(24f, 14f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )

                // Pearl earring on left ear
                drawScope.drawCircle(color = Color.White, radius = 2.5f, center = Offset(x - 14f, y - 13f + wobble))
                drawScope.drawCircle(color = Color(0xFFE2E8F0), radius = 2.5f, center = Offset(x - 14f, y - 13f + wobble), style = Stroke(width = 0.8f))

                // Warm brown eyes (left open, right winking playfully like in the reference art!)
                // Left Eye
                drawScope.drawOval(color = Color.White, topLeft = Offset(x - 9f, y - 19f + wobble), size = Size(7f, 8f))
                drawScope.drawCircle(color = Color(0xFF78350F), radius = 2.8f, center = Offset(x - 5.5f, y - 15f + wobble))
                drawScope.drawCircle(color = Color(0xFF1E293B), radius = 1.6f, center = Offset(x - 5.5f, y - 15f + wobble))
                drawScope.drawCircle(color = Color.White, radius = 1.0f, center = Offset(x - 6.2f, y - 16f + wobble)) // eye shine

                // Right Eye (Winking arc with eyelashes)
                drawScope.drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(x + 3f, y - 15f + wobble),
                    end = Offset(x + 10f, y - 15f + wobble),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
                drawScope.drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(x + 8f, y - 15f + wobble),
                    end = Offset(x + 11f, y - 17f + wobble),
                    strokeWidth = 1.8f,
                    cap = StrokeCap.Round
                )

                // Freckles across nose and cheeks (just like user's heroine image!)
                val freckleColor = Color(0xFFD97706)
                drawScope.drawCircle(color = freckleColor, radius = 0.9f, center = Offset(x - 7f, y - 12f + wobble))
                drawScope.drawCircle(color = freckleColor, radius = 0.9f, center = Offset(x - 4f, y - 13f + wobble))
                drawScope.drawCircle(color = freckleColor, radius = 0.9f, center = Offset(x - 1f, y - 12f + wobble))
                drawScope.drawCircle(color = freckleColor, radius = 0.9f, center = Offset(x + 3f, y - 12f + wobble))
                drawScope.drawCircle(color = freckleColor, radius = 0.9f, center = Offset(x + 6f, y - 13f + wobble))

                // Rosy blushing cheeks
                drawScope.drawCircle(color = Color(0x44FB7185), radius = 4f, center = Offset(x - 8f, y - 11f + wobble))
                drawScope.drawCircle(color = Color(0x44FB7185), radius = 4f, center = Offset(x + 8f, y - 11f + wobble))

                // Joyful wide smile showing clean white dental teeth
                val lipPath = Path().apply {
                    moveTo(x - 5f, y - 9.5f + wobble)
                    quadraticBezierTo(x, y - 5f + wobble, x + 5f, y - 9.5f + wobble)
                }
                drawScope.drawPath(lipPath, color = Color(0xFFE11D48), style = Fill)
                // Teeth inside smile
                drawScope.drawRect(color = Color.White, topLeft = Offset(x - 3.5f, y - 9.5f + wobble), size = Size(7f, 2.5f))

                // Hand giving thumbs up with golden sparkle!
                drawScope.drawCircle(color = Color.White, radius = 4f, center = Offset(x + 15f, y + 2f + wobble)) // gloved fist
                drawScope.drawRoundRect(color = Color.White, topLeft = Offset(x + 15f, y - 5f + wobble), size = Size(4f, 7f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)) // thumb up
                // Sparkle on thumb
                val sparkleAlpha = (0.7f + 0.3f * sin((time * 8f).toDouble())).toFloat()
                drawScope.drawCircle(color = Color(0xFFFDE047).copy(alpha = sparkleAlpha), radius = 4f, center = Offset(x + 17f, y - 6f + wobble))
                drawScope.drawCircle(color = Color.White.copy(alpha = sparkleAlpha), radius = 1.8f, center = Offset(x + 17f, y - 6f + wobble))

                // Dental Mirror held in left hand
                drawScope.drawLine(color = Color(0xFF94A3B8), start = Offset(x - 14f, y + 6f + wobble), end = Offset(x - 24f, y - 6f + wobble), strokeWidth = 3f, cap = StrokeCap.Round)
                drawScope.drawCircle(color = Color(0xFF38BDF8), radius = 6.5f, center = Offset(x - 26f, y - 8f + wobble))
                drawScope.drawCircle(color = Color(0xFF64748B), radius = 6.5f, center = Offset(x - 26f, y - 8f + wobble), style = Stroke(width = 1.8f))
                drawScope.drawCircle(color = Color.White, radius = 2f, center = Offset(x - 27f, y - 9f + wobble))
            }

            CharacterId.LORENZO -> {
                // Lorenzo: 7yo blonde boy in denim overalls with blowing pink bubblegum bubble
                drawScope.drawRoundRect(color = Color(0xFF1D4ED8), topLeft = Offset(x - 13f, y - 4f + wobble), size = Size(26f, 22f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f))
                // Yellow t-shirt sleeves
                drawScope.drawCircle(color = Color(0xFFFACC15), radius = 6f, center = Offset(x - 13f, y - 2f + wobble))
                drawScope.drawCircle(color = Color(0xFFFACC15), radius = 6f, center = Offset(x + 13f, y - 2f + wobble))

                drawScope.drawCircle(color = Color(0xFFFFDFC4), radius = 14f, center = Offset(x, y - 16f + wobble))
                // Textured blonde spiky hair
                drawScope.drawCircle(color = Color(0xFFFBBF24), radius = 15f, center = Offset(x, y - 20f + wobble))
                drawScope.drawOval(color = Color(0xFFFDE047), topLeft = Offset(x - 14f, y - 26f + wobble), size = Size(28f, 12f))

                // Eyes
                drawScope.drawCircle(color = Color(0xFF1E293B), radius = 2.5f, center = Offset(x - 4f, y - 16f + wobble))
                drawScope.drawCircle(color = Color(0xFF1E293B), radius = 2.5f, center = Offset(x + 4f, y - 16f + wobble))

                // Animated bubblegum bubble
                val bubbleSize = 7f + (sin((time * 6f).toDouble()) * 2f).toFloat()
                drawScope.drawCircle(color = Color(0xEEF472B6), radius = bubbleSize, center = Offset(x + 6f, y - 11f + wobble))
                drawScope.drawCircle(color = Color(0x99FFFFFF), radius = bubbleSize * 0.35f, center = Offset(x + 4.5f, y - 13f + wobble))
            }

            CharacterId.RAYNARA -> {
                // Raynara: 14yo stylish black teenage girl, warm brown skin tone, colorful braids, purple jacket & shiny braces
                drawScope.drawRoundRect(color = Color(0xFF7E22CE), topLeft = Offset(x - 14f, y - 5f + wobble), size = Size(28f, 24f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f))
                // Warm rich brown skin tone (melanin warm chestnut-caramel brown):
                drawScope.drawCircle(color = Color(0xFFA56D52), radius = 14.5f, center = Offset(x, y - 16f + wobble))
                // Subtle warm skin glow/highlight on forehead
                drawScope.drawCircle(color = Color(0x33FBBF24), radius = 6.5f, center = Offset(x, y - 20f + wobble))

                // Braided hair bun & side dreadlocks with beads
                drawScope.drawCircle(color = Color(0xFF1E1B4B), radius = 16f, center = Offset(x, y - 21f + wobble))
                drawScope.drawCircle(color = Color(0xFF1E1B4B), radius = 6f, center = Offset(x - 15f, y - 14f + wobble))
                drawScope.drawCircle(color = Color(0xFF1E1B4B), radius = 6f, center = Offset(x + 15f, y - 14f + wobble))
                // Colorful hair beads
                drawScope.drawCircle(color = Color(0xFF06B6D4), radius = 2.5f, center = Offset(x - 15f, y - 9f + wobble))
                drawScope.drawCircle(color = Color(0xFFF59E0B), radius = 2.5f, center = Offset(x + 15f, y - 9f + wobble))

                // Expressive dark brown eyes
                drawScope.drawCircle(color = Color(0xFF1E293B), radius = 2.8f, center = Offset(x - 5f, y - 16f + wobble))
                drawScope.drawCircle(color = Color(0xFF1E293B), radius = 2.8f, center = Offset(x + 5f, y - 16f + wobble))
                drawScope.drawCircle(color = Color.White, radius = 1f, center = Offset(x - 6f, y - 17f + wobble))
                drawScope.drawCircle(color = Color.White, radius = 1f, center = Offset(x + 4f, y - 17f + wobble))

                // Warm blush on cheeks
                drawScope.drawCircle(color = Color(0x44B91C1C), radius = 3.5f, center = Offset(x - 8f, y - 13f + wobble))
                drawScope.drawCircle(color = Color(0x44B91C1C), radius = 3.5f, center = Offset(x + 8f, y - 13f + wobble))

                // Brilliant smile with Orthodontic Braces & glint
                drawScope.drawLine(color = Color(0xFFCBD5E1), start = Offset(x - 5f, y - 9.5f + wobble), end = Offset(x + 5f, y - 9.5f + wobble), strokeWidth = 2.2f)
                // Individual colorful brackets
                drawScope.drawRect(color = Color(0xFF38BDF8), topLeft = Offset(x - 4f, y - 10.5f + wobble), size = Size(2f, 2f))
                drawScope.drawRect(color = Color(0xFFEC4899), topLeft = Offset(x - 1f, y - 10.5f + wobble), size = Size(2f, 2f))
                drawScope.drawRect(color = Color(0xFF38BDF8), topLeft = Offset(x + 2f, y - 10.5f + wobble), size = Size(2f, 2f))
            }

            CharacterId.JULIO -> {
                // Julio: Navy military officer with white naval tunic, peaked captain cap with golden anchor
                drawScope.drawRoundRect(color = Color(0xFFF8FAFC), topLeft = Offset(x - 16f, y - 6f + wobble), size = Size(32f, 26f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f))
                // Navy blue & gold shoulder epaulets
                drawScope.drawRect(color = Color(0xFF1E3A8A), topLeft = Offset(x - 17f, y - 6f + wobble), size = Size(7f, 4f))
                drawScope.drawRect(color = Color(0xFFFBBF24), topLeft = Offset(x - 17f, y - 2f + wobble), size = Size(7f, 2f))
                drawScope.drawRect(color = Color(0xFF1E3A8A), topLeft = Offset(x + 10f, y - 6f + wobble), size = Size(7f, 4f))
                drawScope.drawRect(color = Color(0xFFFBBF24), topLeft = Offset(x + 10f, y - 2f + wobble), size = Size(7f, 2f))

                // Head
                drawScope.drawCircle(color = Color(0xFFFFDFC4), radius = 14.5f, center = Offset(x, y - 17f + wobble))

                // Navy Officer Peaked Cap
                drawScope.drawRoundRect(color = Color.White, topLeft = Offset(x - 15f, y - 29f + wobble), size = Size(30f, 11f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f))
                drawScope.drawRect(color = Color(0xFF0F172A), topLeft = Offset(x - 14f, y - 21f + wobble), size = Size(28f, 4f))
                // Black visor with shine
                drawScope.drawRoundRect(color = Color(0xFF09090B), topLeft = Offset(x - 16f, y - 18f + wobble), size = Size(32f, 4f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
                // Gold anchor crest
                drawScope.drawCircle(color = Color(0xFFFBBF24), radius = 3.5f, center = Offset(x, y - 23f + wobble))

                // Determined eyes & neat mustache
                drawScope.drawCircle(color = Color(0xFF1E293B), radius = 2.5f, center = Offset(x - 5f, y - 15f + wobble))
                drawScope.drawCircle(color = Color(0xFF1E293B), radius = 2.5f, center = Offset(x + 5f, y - 15f + wobble))
                drawScope.drawRoundRect(color = Color(0xFF475569), topLeft = Offset(x - 5f, y - 11f + wobble), size = Size(10f, 2.5f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f))
            }

            CharacterId.RICK -> {
                // Rick: Classic black & white Border Collie!
                val tailWag = (sin((time * 16f).toDouble()) * 18f).toFloat()

                // Wagging tail behind
                drawScope.drawLine(color = Color(0xFF18181B), start = Offset(x + 12f, y + 4f + wobble), end = Offset(x + 24f + tailWag, y - 4f + wobble), strokeWidth = 5f, cap = StrokeCap.Round)
                drawScope.drawCircle(color = Color.White, radius = 3.5f, center = Offset(x + 24f + tailWag, y - 4f + wobble))

                // Body (black coat with fluffy white chest ruff)
                drawScope.drawOval(color = Color(0xFF18181B), topLeft = Offset(x - 16f, y - 8f + wobble), size = Size(32f, 24f))
                drawScope.drawOval(color = Color.White, topLeft = Offset(x - 9f, y - 5f + wobble), size = Size(18f, 20f))

                // Head
                drawScope.drawCircle(color = Color(0xFF18181B), radius = 13.5f, center = Offset(x, y - 16f + wobble))
                // White symmetrical blaze down snout
                drawScope.drawRoundRect(color = Color.White, topLeft = Offset(x - 4f, y - 21f + wobble), size = Size(8f, 16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f))

                // Floppy Border Collie ears bouncing
                val earBounce = (sin((time * 12f).toDouble()) * 2f).toFloat()
                drawScope.drawOval(color = Color(0xFF18181B), topLeft = Offset(x - 17f, y - 26f + wobble + earBounce), size = Size(9f, 15f))
                drawScope.drawOval(color = Color(0xFF18181B), topLeft = Offset(x + 8f, y - 26f + wobble + earBounce), size = Size(9f, 15f))

                // Dark almond eyes, cute black nose & pink panting tongue
                drawScope.drawCircle(color = Color(0xFF3E2723), radius = 2.5f, center = Offset(x - 5f, y - 16f + wobble))
                drawScope.drawCircle(color = Color(0xFF3E2723), radius = 2.5f, center = Offset(x + 5f, y - 16f + wobble))
                drawScope.drawCircle(color = Color(0xFF09090B), radius = 2.8f, center = Offset(x, y - 10f + wobble))
                drawScope.drawOval(color = Color(0xFFFB7185), topLeft = Offset(x - 2.5f, y - 7f + wobble), size = Size(5f, 8f))
            }

            CharacterId.BART -> {
                // Bart: Red Border Collie (rich copper/red mahogany coat, amber eyes)
                val redCoat = Color(0xFF9A3412)
                val tailWag = (sin((time * 16f).toDouble()) * 18f).toFloat()

                // Wagging tail
                drawScope.drawLine(color = redCoat, start = Offset(x + 12f, y + 4f + wobble), end = Offset(x + 24f + tailWag, y - 4f + wobble), strokeWidth = 5f, cap = StrokeCap.Round)
                drawScope.drawCircle(color = Color.White, radius = 3.5f, center = Offset(x + 24f + tailWag, y - 4f + wobble))

                drawScope.drawOval(color = redCoat, topLeft = Offset(x - 16f, y - 8f + wobble), size = Size(32f, 24f))
                drawScope.drawOval(color = Color.White, topLeft = Offset(x - 9f, y - 5f + wobble), size = Size(18f, 20f))

                drawScope.drawCircle(color = redCoat, radius = 13.5f, center = Offset(x, y - 16f + wobble))
                drawScope.drawRoundRect(color = Color.White, topLeft = Offset(x - 4f, y - 21f + wobble), size = Size(8f, 16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f))

                drawScope.drawOval(color = redCoat, topLeft = Offset(x - 17f, y - 26f + wobble), size = Size(9f, 15f))
                drawScope.drawOval(color = redCoat, topLeft = Offset(x + 8f, y - 26f + wobble), size = Size(9f, 15f))

                // Bright golden amber eyes
                drawScope.drawCircle(color = Color(0xFFD97706), radius = 2.8f, center = Offset(x - 5f, y - 16f + wobble))
                drawScope.drawCircle(color = Color(0xFFD97706), radius = 2.8f, center = Offset(x + 5f, y - 16f + wobble))
                drawScope.drawCircle(color = Color(0xFF451A03), radius = 2.8f, center = Offset(x, y - 10f + wobble))
            }
        }
    }

    private fun drawEnemy(
        drawScope: DrawScope,
        enemy: EnemyInstance,
        x: Float,
        y: Float,
        time: Float,
        isFrozen: Boolean
    ) {
        val wobble = if (isFrozen) 0f else (sin((time * 8f + enemy.id).toDouble()) * 2.5).toFloat()
        val hitColor = if (enemy.flashHitTimer > 0f) Color.White else Color(enemy.type.colorHex)

        if (isFrozen) {
            drawScope.drawCircle(
                color = Color(0x8838BDF8),
                radius = enemy.radius + 7f,
                center = Offset(x, y)
            )
            drawScope.drawCircle(
                color = Color(0xCCBAE6FD),
                radius = enemy.radius + 4f,
                center = Offset(x, y),
                style = Stroke(width = 2f)
            )
        }

        when (enemy.type) {
            EnemyType.STREPTOCOCCUS -> {
                // Streptococcus mutans: multi-segmented glossy green chain
                val r = enemy.radius * 0.55f
                drawScope.drawCircle(color = hitColor, radius = r, center = Offset(x - r * 0.8f, y + wobble))
                drawScope.drawCircle(color = hitColor, radius = r, center = Offset(x + r * 0.8f, y + wobble))
                drawScope.drawCircle(color = hitColor, radius = r * 1.15f, center = Offset(x, y - r * 0.6f + wobble))
                // Specular 3D highlight
                drawScope.drawCircle(color = Color(0x66FFFFFF), radius = r * 0.35f, center = Offset(x - 3f, y - r * 0.9f + wobble))

                // Angry eyebrows & fangs
                drawScope.drawLine(color = Color(0xFF14532D), start = Offset(x - 7f, y - 9f + wobble), end = Offset(x - 2f, y - 6f + wobble), strokeWidth = 2.5f)
                drawScope.drawLine(color = Color(0xFF14532D), start = Offset(x + 7f, y - 9f + wobble), end = Offset(x + 2f, y - 6f + wobble), strokeWidth = 2.5f)
                drawScope.drawCircle(color = Color.White, radius = 3.5f, center = Offset(x - 4f, y - 5f + wobble))
                drawScope.drawCircle(color = Color.White, radius = 3.5f, center = Offset(x + 4f, y - 5f + wobble))
                drawScope.drawCircle(color = Color.Black, radius = 2f, center = Offset(x - 4f, y - 5f + wobble))
                drawScope.drawCircle(color = Color.Black, radius = 2f, center = Offset(x + 4f, y - 5f + wobble))
                // Tiny fangs
                drawScope.drawRect(color = Color.White, topLeft = Offset(x - 3f, y + wobble), size = Size(2f, 3f))
                drawScope.drawRect(color = Color.White, topLeft = Offset(x + 1f, y + wobble), size = Size(2f, 3f))
            }

            EnemyType.PLACA_AMARELA -> {
                // Plaque blob: translucent drippy gooey amoeba
                drawScope.drawRoundRect(
                    color = hitColor,
                    topLeft = Offset(x - enemy.radius, y - enemy.radius + wobble),
                    size = Size(enemy.radius * 2f, enemy.radius * 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(enemy.radius * 0.7f, enemy.radius * 0.7f)
                )
                // Dripping bottom blobs
                drawScope.drawCircle(color = hitColor, radius = enemy.radius * 0.35f, center = Offset(x - 6f, y + enemy.radius * 0.8f + wobble))
                drawScope.drawCircle(color = hitColor, radius = enemy.radius * 0.25f, center = Offset(x + 8f, y + enemy.radius * 0.7f + wobble))

                // Googly cartoon eyes
                drawScope.drawCircle(color = Color.White, radius = 5.5f, center = Offset(x - 5f, y - 4f + wobble))
                drawScope.drawCircle(color = Color.White, radius = 4.5f, center = Offset(x + 5f, y - 2f + wobble))
                drawScope.drawCircle(color = Color.Black, radius = 2.5f, center = Offset(x - 4.5f, y - 4f + wobble))
                drawScope.drawCircle(color = Color.Black, radius = 2.0f, center = Offset(x + 5.5f, y - 2f + wobble))
            }

            EnemyType.ESPIROQUETA -> {
                // Spirochete: Fast wiggly corkscrew bug
                drawScope.drawOval(
                    color = hitColor,
                    topLeft = Offset(x - enemy.radius * 0.6f, y - enemy.radius * 1.4f + wobble),
                    size = Size(enemy.radius * 1.2f, enemy.radius * 2.8f)
                )
                // Spiral coil rings
                drawScope.drawLine(color = Color(0xFFC2410C), start = Offset(x - 6f, y - 8f + wobble), end = Offset(x + 6f, y - 4f + wobble), strokeWidth = 2.5f)
                drawScope.drawLine(color = Color(0xFFC2410C), start = Offset(x - 6f, y + 2f + wobble), end = Offset(x + 6f, y + 6f + wobble), strokeWidth = 2.5f)
                drawScope.drawCircle(color = Color.White, radius = 3.5f, center = Offset(x, y - 10f + wobble))
                drawScope.drawCircle(color = Color.Black, radius = 2f, center = Offset(x, y - 10f + wobble))
            }

            EnemyType.ACIDO_CORROSIVO -> {
                // Purple corrosive droplet
                drawScope.drawCircle(color = hitColor, radius = enemy.radius, center = Offset(x, y + wobble))
                drawScope.drawCircle(color = Color(0xFFC084FC), radius = enemy.radius * 0.5f, center = Offset(x - enemy.radius * 0.3f, y - enemy.radius * 0.3f + wobble))
                drawScope.drawCircle(color = Color.White, radius = 2.5f, center = Offset(x - enemy.radius * 0.4f, y - enemy.radius * 0.4f + wobble))
            }

            EnemyType.TARTARO_MINION -> {
                // Hard rock calculus / tartar golem
                drawScope.drawRoundRect(
                    color = hitColor,
                    topLeft = Offset(x - enemy.radius, y - enemy.radius + wobble),
                    size = Size(enemy.radius * 2f, enemy.radius * 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                // Craggy rock cracks
                drawScope.drawLine(color = Color(0xFF44403C), start = Offset(x - 10f, y - 10f + wobble), end = Offset(x + 2f, y + 2f + wobble), strokeWidth = 2f)
                drawScope.drawCircle(color = Color(0xFFEF4444), radius = 3.5f, center = Offset(x - 6f, y - 4f + wobble))
                drawScope.drawCircle(color = Color(0xFFEF4444), radius = 3.5f, center = Offset(x + 6f, y - 4f + wobble))
            }

            // === PRE-BOSS ELITES ===
            EnemyType.ELITE_SUPER_BACTERIA -> {
                // Elite Mutant Super-Bacteria: pulsating toxic green sphere with emerald chitin shell and neon spikes
                val pulse = (sin((time * 8f).toDouble()) * 3f).toFloat()
                drawScope.drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x7710B981), Color(0x33059669), Color.Transparent),
                        center = Offset(x, y),
                        radius = (enemy.radius + pulse) * 1.35f
                    ),
                    radius = (enemy.radius + pulse) * 1.35f,
                    center = Offset(x, y)
                )
                // Core body
                drawScope.drawCircle(color = hitColor, radius = enemy.radius, center = Offset(x, y + wobble))
                drawScope.drawCircle(color = Color(0xFF34D399), radius = enemy.radius * 0.7f, center = Offset(x - 4f, y - 4f + wobble))

                // Toxic Spikes radiating outward
                for (i in 0 until 8) {
                    val ang = (i * 45f + time * 30f) % 360f
                    val rad = Math.toRadians(ang.toDouble()).toFloat()
                    val sx = x + cos(rad) * (enemy.radius + 6f + pulse)
                    val sy = y + sin(rad) * (enemy.radius + 6f + pulse) + wobble
                    drawScope.drawCircle(color = Color(0xFFFDE047), radius = 4f, center = Offset(sx, sy))
                }

                // Menacing triple eyes
                drawScope.drawCircle(color = Color(0xFFFEF08A), radius = 5f, center = Offset(x - 10f, y - 6f + wobble))
                drawScope.drawCircle(color = Color(0xFFFEF08A), radius = 6f, center = Offset(x, y - 8f + wobble))
                drawScope.drawCircle(color = Color(0xFFFEF08A), radius = 5f, center = Offset(x + 10f, y - 6f + wobble))
                drawScope.drawCircle(color = Color(0xFFB91C1C), radius = 2.5f, center = Offset(x - 10f, y - 6f + wobble))
                drawScope.drawCircle(color = Color(0xFFB91C1C), radius = 3f, center = Offset(x, y - 8f + wobble))
                drawScope.drawCircle(color = Color(0xFFB91C1C), radius = 2.5f, center = Offset(x + 10f, y - 6f + wobble))

                // Sharp fangs
                drawScope.drawLine(color = Color.White, start = Offset(x - 8f, y + 4f + wobble), end = Offset(x - 4f, y + 12f + wobble), strokeWidth = 3f)
                drawScope.drawLine(color = Color.White, start = Offset(x + 8f, y + 4f + wobble), end = Offset(x + 4f, y + 12f + wobble), strokeWidth = 3f)
            }

            EnemyType.ELITE_BIOFILME_TITAN -> {
                // Elite Armored Biofilme Titan: heavy bronze and amber stone plates with magma cracks
                drawScope.drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x66D97706), Color(0x22B45309), Color.Transparent),
                        center = Offset(x, y),
                        radius = enemy.radius * 1.3f
                    ),
                    radius = enemy.radius * 1.3f,
                    center = Offset(x, y)
                )
                // Armored hexagonal body
                drawScope.drawRoundRect(
                    color = hitColor,
                    topLeft = Offset(x - enemy.radius, y - enemy.radius + wobble),
                    size = Size(enemy.radius * 2f, enemy.radius * 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFFF59E0B),
                    topLeft = Offset(x - enemy.radius * 0.7f, y - enemy.radius * 0.7f + wobble),
                    size = Size(enemy.radius * 1.4f, enemy.radius * 1.4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                    style = Stroke(width = 3.5f)
                )
                // Magma fissures
                drawScope.drawLine(color = Color(0xFFEF4444), start = Offset(x - 14f, y - 10f + wobble), end = Offset(x + 4f, y + 10f + wobble), strokeWidth = 3.5f)
                drawScope.drawLine(color = Color(0xFFFCD34D), start = Offset(x - 14f, y - 10f + wobble), end = Offset(x + 4f, y + 10f + wobble), strokeWidth = 1.5f)

                // Glowing fiery eyes
                drawScope.drawCircle(color = Color(0xFFDC2626), radius = 6f, center = Offset(x - 12f, y - 6f + wobble))
                drawScope.drawCircle(color = Color(0xFFDC2626), radius = 6f, center = Offset(x + 12f, y - 6f + wobble))
                drawScope.drawCircle(color = Color(0xFFFEF08A), radius = 2.5f, center = Offset(x - 12f, y - 6f + wobble))
                drawScope.drawCircle(color = Color(0xFFFEF08A), radius = 2.5f, center = Offset(x + 12f, y - 6f + wobble))
            }

            // === BOSSES ===
            EnemyType.BOSS_CARIE_GIGANTE -> {
                // Giant Cracked Molar Cavity with Demonic Aura
                drawScope.drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF7E22CE), Color(0x441E1B4B), Color.Transparent),
                        center = Offset(x, y),
                        radius = enemy.radius * 1.4f
                    ),
                    radius = enemy.radius * 1.4f,
                    center = Offset(x, y)
                )

                drawScope.drawCircle(color = hitColor, radius = enemy.radius, center = Offset(x, y))
                // Glowing purple fissures & cracks
                drawScope.drawLine(color = Color(0xFFA855F7), start = Offset(x - 25f, y - 25f), end = Offset(x + 8f, y + 10f), strokeWidth = 5f)
                drawScope.drawLine(color = Color(0xFFA855F7), start = Offset(x + 8f, y + 10f), end = Offset(x + 28f, y - 12f), strokeWidth = 4f)
                drawScope.drawLine(color = Color(0xFFE9D5FF), start = Offset(x - 25f, y - 25f), end = Offset(x + 8f, y + 10f), strokeWidth = 2f)

                // Burning crimson glowing eyes
                drawScope.drawCircle(color = Color(0xFFEF4444), radius = 9f, center = Offset(x - 16f, y - 8f))
                drawScope.drawCircle(color = Color(0xFFFDE047), radius = 4f, center = Offset(x - 16f, y - 8f))
                drawScope.drawCircle(color = Color(0xFFEF4444), radius = 9f, center = Offset(x + 16f, y - 8f))
                drawScope.drawCircle(color = Color(0xFFFDE047), radius = 4f, center = Offset(x + 16f, y - 8f))

                // Sharp obsidian fangs
                drawScope.drawRect(color = Color.White, topLeft = Offset(x - 10f, y + 14f), size = Size(6f, 12f))
                drawScope.drawRect(color = Color.White, topLeft = Offset(x + 4f, y + 14f), size = Size(6f, 12f))
            }

            EnemyType.BOSS_GENGIVITE -> {
                // Inflamed pulsing red queen with tentacle spikes
                val pulse = (sin((time * 6f).toDouble()) * 5f).toFloat()
                drawScope.drawCircle(color = hitColor, radius = enemy.radius + pulse, center = Offset(x, y))
                drawScope.drawCircle(color = Color(0xFF991B1B), radius = (enemy.radius + pulse) * 0.75f, center = Offset(x, y))
                // Multiple angry eyes
                drawScope.drawCircle(color = Color(0xFFFEF08A), radius = 9f, center = Offset(x - 16f, y - 12f))
                drawScope.drawCircle(color = Color.Black, radius = 4f, center = Offset(x - 16f, y - 12f))
                drawScope.drawCircle(color = Color(0xFFFEF08A), radius = 9f, center = Offset(x + 16f, y - 12f))
                drawScope.drawCircle(color = Color.Black, radius = 4f, center = Offset(x + 16f, y - 12f))
            }

            EnemyType.BOSS_ABCESSO -> {
                // Pulp Abscess Boss
                drawScope.drawCircle(color = hitColor, radius = enemy.radius, center = Offset(x, y))
                drawScope.drawCircle(color = Color(0xFFFACC15), radius = 12f, center = Offset(x - 14f, y - 14f))
                drawScope.drawCircle(color = Color(0xFFFACC15), radius = 10f, center = Offset(x + 18f, y + 8f))
            }

            EnemyType.BOSS_BIOFILME_APARELHO -> {
                // Braces biofilm monster with metal wire frame
                drawScope.drawRoundRect(
                    color = hitColor,
                    topLeft = Offset(x - enemy.radius, y - enemy.radius),
                    size = Size(enemy.radius * 2f, enemy.radius * 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                )
                drawScope.drawLine(color = Color(0xFFE2E8F0), start = Offset(x - 34f, y), end = Offset(x + 34f, y), strokeWidth = 6f)
                drawScope.drawLine(color = Color(0xFFE2E8F0), start = Offset(x, y - 34f), end = Offset(x, y + 34f), strokeWidth = 6f)
            }

            EnemyType.BOSS_TARTARO_TITANICO -> {
                // The ultimate stone titan
                drawScope.drawRoundRect(
                    color = hitColor,
                    topLeft = Offset(x - enemy.radius, y - enemy.radius),
                    size = Size(enemy.radius * 2f, enemy.radius * 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )
                drawScope.drawCircle(color = Color(0xFFF97316), radius = 10f, center = Offset(x - 20f, y - 14f))
                drawScope.drawCircle(color = Color(0xFFF97316), radius = 10f, center = Offset(x + 20f, y - 14f))
            }
        }

        // Boss Mini Health Bar
        if (enemy.isBoss) {
            val barW = enemy.radius * 2.2f
            val barH = 8f
            val hpFrac = (enemy.hp / enemy.maxHp).coerceIn(0f, 1f)
            drawScope.drawRect(
                color = Color(0x99000000),
                topLeft = Offset(x - barW / 2f, y - enemy.radius - 18f),
                size = Size(barW, barH)
            )
            drawScope.drawRect(
                color = Color(0xFFEF4444),
                topLeft = Offset(x - barW / 2f, y - enemy.radius - 18f),
                size = Size(barW * hpFrac, barH)
            )
        }
    }

    private fun drawProjectile(
        drawScope: DrawScope,
        p: ProjectileInstance,
        x: Float,
        y: Float,
        time: Float
    ) {
        when {
            p.isLaser -> {
                // Laser beam: Titanium Floss or UV curing light
                val laserColor = if (p.isEvolved) Color(0xFF00F0FF) else Color(0xFF38BDF8)
                drawScope.drawLine(
                    color = laserColor.copy(alpha = 0.5f),
                    start = Offset(x - p.vx * 0.18f, y - p.vy * 0.18f),
                    end = Offset(x + p.vx * 0.18f, y + p.vy * 0.18f),
                    strokeWidth = p.radius * 1.6f,
                    cap = StrokeCap.Round
                )
                drawScope.drawLine(
                    color = laserColor,
                    start = Offset(x - p.vx * 0.15f, y - p.vy * 0.15f),
                    end = Offset(x + p.vx * 0.15f, y + p.vy * 0.15f),
                    strokeWidth = p.radius,
                    cap = StrokeCap.Round
                )
                drawScope.drawLine(
                    color = Color.White,
                    start = Offset(x - p.vx * 0.15f, y - p.vy * 0.15f),
                    end = Offset(x + p.vx * 0.15f, y + p.vy * 0.15f),
                    strokeWidth = p.radius * 0.45f,
                    cap = StrokeCap.Round
                )
            }

            p.weaponType == WeaponType.ESCOVA_DENTE -> {
                // High-polish Toothbrush with ergonomic handle, bristles, and mint foam trail
                drawScope.rotate(degrees = p.rotation, pivot = Offset(x, y)) {
                    // Ergonomic handle
                    drawRoundRect(
                        color = Color(0xFF0284C7),
                        topLeft = Offset(x - 6f, y - 20f),
                        size = Size(12f, 40f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                    // Rubber grip pads
                    drawRect(color = Color(0xFF0369A1), topLeft = Offset(x - 4f, y + 2f), size = Size(8f, 12f))

                    // Bristles head
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(x - 7f, y - 22f),
                        size = Size(14f, 16f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                    )
                    // Aqua mint gel paste stripe
                    drawRect(
                        color = Color(0xFF06B6D4),
                        topLeft = Offset(x - 2.5f, y - 22f),
                        size = Size(5f, 16f)
                    )
                }
                // Foaming bubbles flying off toothbrush
                val bubbleT = (time * 8f) % 1f
                drawScope.drawCircle(color = Color(0xCCFFFFFF), radius = 3.5f, center = Offset(x - 12f * bubbleT, y + 10f * bubbleT))
                drawScope.drawCircle(color = Color(0x9938BDF8), radius = 2.5f, center = Offset(x + 10f * bubbleT, y - 8f * bubbleT))
            }

            p.weaponType == WeaponType.FIO_DENTAL -> {
                // Whipping Dental Floss line with tension glow and sharp cutting edges
                drawScope.drawLine(
                    color = Color(0x6638BDF8),
                    start = Offset(x - p.vx * 0.12f, y - p.vy * 0.12f),
                    end = Offset(x + p.vx * 0.12f, y + p.vy * 0.12f),
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
                drawScope.drawLine(
                    color = Color(0xFF38BDF8),
                    start = Offset(x - p.vx * 0.09f, y - p.vy * 0.09f),
                    end = Offset(x + p.vx * 0.09f, y + p.vy * 0.09f),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
                drawScope.drawLine(
                    color = Color.White,
                    start = Offset(x - p.vx * 0.07f, y - p.vy * 0.07f),
                    end = Offset(x + p.vx * 0.07f, y + p.vy * 0.07f),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
                // Floss tension glint
                drawScope.drawCircle(color = Color.White, radius = 4f, center = Offset(x, y))
                drawScope.drawCircle(color = Color(0xFF38BDF8), radius = 7f, center = Offset(x, y), style = Stroke(width = 1.5f))
            }

            p.weaponType == WeaponType.JATO_BICARBONATO -> {
                // High-velocity Water spray droplet with bubbly effervescence and splash tail
                drawScope.drawCircle(color = Color(0x4406B6D4), radius = p.radius * 1.4f, center = Offset(x, y))
                drawScope.drawCircle(color = Color(0xFF06B6D4), radius = p.radius, center = Offset(x, y))
                drawScope.drawCircle(color = Color(0xFFBAE6FD), radius = p.radius * 0.65f, center = Offset(x - 2f, y - 2f))
                drawScope.drawCircle(color = Color.White, radius = p.radius * 0.35f, center = Offset(x - 3f, y - 3f))
                // Splash droplets trailing behind
                drawScope.drawCircle(color = Color(0xAA67E8F9), radius = p.radius * 0.4f, center = Offset(x - p.vx * 0.04f, y - p.vy * 0.04f))
                drawScope.drawCircle(color = Color(0x77BAE6FD), radius = p.radius * 0.25f, center = Offset(x - p.vx * 0.08f, y - p.vy * 0.08f))
            }

            p.weaponType == WeaponType.BROCA_ODONTO -> {
                // Diamond carbide drill bur with rotating sparks
                drawScope.rotate(degrees = (time * 1200f) % 360f, pivot = Offset(x, y)) {
                    drawScope.drawCircle(color = Color(0xFF94A3B8), radius = p.radius, center = Offset(x, y))
                    drawScope.drawCircle(color = Color(0xFFCBD5E1), radius = p.radius * 0.75f, center = Offset(x, y))
                    drawScope.drawCircle(color = Color(0xFF0284C7), radius = p.radius * 0.45f, center = Offset(x, y))
                    // 4 cutting flutes
                    drawLine(color = Color.White, start = Offset(x - p.radius, y), end = Offset(x + p.radius, y), strokeWidth = 2f)
                    drawLine(color = Color.White, start = Offset(x, y - p.radius), end = Offset(x, y + p.radius), strokeWidth = 2f)
                }
                // Golden friction sparks
                val sparkAngle = (time * 900f) % 360f
                val sRad = Math.toRadians(sparkAngle.toDouble())
                val sx = x + (cos(sRad) * (p.radius + 5f)).toFloat()
                val sy = y + (sin(sRad) * (p.radius + 5f)).toFloat()
                drawScope.drawCircle(color = Color(0xFFFDE047), radius = 2.5f, center = Offset(sx, sy))
            }

            p.weaponType == WeaponType.PASTA_EXPLOSIVA -> {
                // Toothpaste bomb with red & blue stripes and sparkling fuse
                drawScope.drawCircle(color = Color(0xFF2563EB), radius = p.radius, center = Offset(x, y))
                drawScope.drawCircle(color = Color.White, radius = p.radius * 0.7f, center = Offset(x, y))
                drawScope.drawCircle(color = Color(0xFFEF4444), radius = p.radius * 0.4f, center = Offset(x, y))
                drawScope.drawCircle(color = Color.White, radius = 2.5f, center = Offset(x - 3f, y - 3f))
                // Fuse spark
                drawScope.drawCircle(color = Color(0xFFF59E0B), radius = 3.5f, center = Offset(x + p.radius * 0.7f, y - p.radius * 0.7f))
            }

            else -> {
                drawScope.drawCircle(color = Color(0xFF38BDF8), radius = p.radius, center = Offset(x, y))
            }
        }
    }

    private fun drawLootItem(
        drawScope: DrawScope,
        loot: LootInstance,
        x: Float,
        y: Float,
        time: Float
    ) {
        val bobbing = (sin((time * 6f + loot.id).toDouble()) * 3.5).toFloat()

        when (loot.type) {
            LootType.FLUOR_XP -> {
                // Glowing blue fluoride teardrop
                drawScope.drawCircle(color = Color(0x4438BDF8), radius = 10f, center = Offset(x, y + bobbing))
                drawScope.drawCircle(color = Color(0xFF38BDF8), radius = 7f, center = Offset(x, y + bobbing))
                drawScope.drawCircle(color = Color.White, radius = 3f, center = Offset(x - 2f, y - 2f + bobbing))
            }

            LootType.CRISTAL_XILITOL -> {
                // Green high-value crystal
                drawScope.drawCircle(color = Color(0x4422C55E), radius = 13f, center = Offset(x, y + bobbing))
                drawScope.drawCircle(color = Color(0xFF22C55E), radius = 9f, center = Offset(x, y + bobbing))
                drawScope.drawCircle(color = Color(0xFF86EFAC), radius = 4.5f, center = Offset(x, y + bobbing))
            }

            LootType.MACA_HEAL -> {
                // Shiny green crisp apple (Heal)
                drawScope.drawCircle(color = Color(0xFF16A34A), radius = 11f, center = Offset(x, y + bobbing))
                drawScope.drawCircle(color = Color(0xFF4ADE80), radius = 5f, center = Offset(x - 3f, y - 3f + bobbing))
                drawScope.drawLine(
                    color = Color(0xFF78350F),
                    start = Offset(x, y - 11f + bobbing),
                    end = Offset(x + 3f, y - 17f + bobbing),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }

            LootType.CAPSULA_ANALGESICO -> {
                // Medical capsule pill (High Heal): Red top half, white bottom half, medical cross
                drawScope.drawCircle(
                    color = Color(0x44F43F5E),
                    radius = 16f,
                    center = Offset(x, y + bobbing)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFFF43F5E),
                    topLeft = Offset(x - 7f, y - 14f + bobbing),
                    size = Size(14f, 14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f)
                )
                drawScope.drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(x - 7f, y + bobbing),
                    size = Size(14f, 14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f)
                )
                // Little red medical cross in center
                drawScope.drawLine(color = Color(0xFFE11D48), start = Offset(x - 4f, y + bobbing), end = Offset(x + 4f, y + bobbing), strokeWidth = 2f)
                drawScope.drawLine(color = Color(0xFFE11D48), start = Offset(x, y - 4f + bobbing), end = Offset(x, y + 4f + bobbing), strokeWidth = 2f)
            }

            LootType.PASTILHA_VACUUM -> {
                // Pink plaque disclosing tablet (Loot magnet)
                drawScope.drawCircle(color = Color(0x55EC4899), radius = 13f, center = Offset(x, y + bobbing))
                drawScope.drawCircle(color = Color(0xFFEC4899), radius = 9f, center = Offset(x, y + bobbing))
                drawScope.drawCircle(color = Color.White, radius = 4f, center = Offset(x, y + bobbing))
            }

            LootType.ENXAGUANTE_FREEZE -> {
                // Freeze potion bottle
                drawScope.drawRoundRect(
                    color = Color(0xFF06B6D4),
                    topLeft = Offset(x - 7f, y - 10f + bobbing),
                    size = Size(14f, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                drawScope.drawCircle(color = Color.White, radius = 3f, center = Offset(x, y + bobbing))
            }

            LootType.BAU_PRATA -> {
                // Silver Dental Treasure Chest
                drawScope.drawCircle(color = Color(0x44CBD5E1), radius = 20f, center = Offset(x, y + bobbing))
                drawScope.drawRoundRect(
                    color = Color(0xFF94A3B8),
                    topLeft = Offset(x - 15f, y - 10f + bobbing),
                    size = Size(30f, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
                )
                drawScope.drawRect(
                    color = Color(0xFFE2E8F0),
                    topLeft = Offset(x - 15f, y - 2f + bobbing),
                    size = Size(30f, 4f)
                )
                drawScope.drawCircle(color = Color(0xFF38BDF8), radius = 3f, center = Offset(x, y + bobbing))
            }

            LootType.BAU_DOURADO -> {
                // Ornate Golden Hygiene Chest with sparkles
                drawScope.drawCircle(color = Color(0x44F59E0B), radius = 22f, center = Offset(x, y + bobbing))
                drawScope.drawRoundRect(
                    color = Color(0xFFF59E0B),
                    topLeft = Offset(x - 16f, y - 11f + bobbing),
                    size = Size(32f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
                )
                drawScope.drawRect(
                    color = Color(0xFFFDE68A),
                    topLeft = Offset(x - 16f, y - 2f + bobbing),
                    size = Size(32f, 4.5f)
                )
                drawScope.drawCircle(color = Color.White, radius = 3.5f, center = Offset(x, y + bobbing))
            }

            LootType.BAU_DIAMANTE -> {
                // Diamond Cyan Chest with sparkling facets
                drawScope.drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x6606B6D4), Color(0x2238BDF8), Color.Transparent),
                        center = Offset(x, y + bobbing),
                        radius = 26f
                    ),
                    radius = 26f,
                    center = Offset(x, y + bobbing)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFF0284C7),
                    topLeft = Offset(x - 17f, y - 12f + bobbing),
                    size = Size(34f, 24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                drawScope.drawRect(
                    color = Color(0xFFA5F3FC),
                    topLeft = Offset(x - 17f, y - 2f + bobbing),
                    size = Size(34f, 5f)
                )
                drawScope.drawCircle(color = Color.White, radius = 4f, center = Offset(x, y + bobbing))
            }

            LootType.BAU_MEGA_XP -> {
                // Legendary Purple Tome / Chest of Pure Level Experience
                drawScope.drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x88A855F7), Color(0x337E22CE), Color.Transparent),
                        center = Offset(x, y + bobbing),
                        radius = 28f
                    ),
                    radius = 28f,
                    center = Offset(x, y + bobbing)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFF7C3AED),
                    topLeft = Offset(x - 18f, y - 13f + bobbing),
                    size = Size(36f, 26f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                // Golden spine and dental crest
                drawScope.drawRect(
                    color = Color(0xFFFDE047),
                    topLeft = Offset(x - 18f, y - 2f + bobbing),
                    size = Size(36f, 5f)
                )
                drawScope.drawCircle(color = Color(0xFFFDE047), radius = 5f, center = Offset(x, y + bobbing))
                drawScope.drawCircle(color = Color.White, radius = 2.5f, center = Offset(x, y + bobbing))
            }

            LootType.DENTE_OURO -> {
                // Golden tooth coin
                drawScope.drawCircle(color = Color(0xFFFBBF24), radius = 8f, center = Offset(x, y + bobbing))
                drawScope.drawCircle(color = Color(0xFFFEF08A), radius = 4f, center = Offset(x - 1.5f, y - 1.5f + bobbing))
            }
        }
    }
}
