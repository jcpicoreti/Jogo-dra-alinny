package com.example.game

import com.example.audio.SoundManager
import com.example.data.local.ClinicUpgradeEntity
import com.example.data.model.ActivePassive
import com.example.data.model.ActiveWeapon
import com.example.data.model.CharacterId
import com.example.data.model.ClinicUpgradeType
import com.example.data.model.DamageNumber
import com.example.data.model.EnemyInstance
import com.example.data.model.EnemyType
import com.example.data.model.EvolutionType
import com.example.data.model.GameState
import com.example.data.model.LevelUpOption
import com.example.data.model.LootInstance
import com.example.data.model.LootType
import com.example.data.model.PassiveType
import com.example.data.model.ProjectileInstance
import com.example.data.model.StageId
import com.example.data.model.VisualParticle
import com.example.data.model.WeaponType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class RenderSnapshot(
    val enemies: List<EnemyInstance> = emptyList(),
    val projectiles: List<ProjectileInstance> = emptyList(),
    val lootItems: List<LootInstance> = emptyList(),
    val damageNumbers: List<DamageNumber> = emptyList(),
    val particles: List<VisualParticle> = emptyList()
)

class GameEngine(
    val characterId: CharacterId,
    val stageId: StageId,
    private val soundManager: SoundManager,
    clinicUpgrades: List<ClinicUpgradeEntity>,
    val highestStageUnlocked: Int = 1
) {
    val stateLock = Any()

    private val _gameState = MutableStateFlow(GameState(characterId = characterId, stageId = stageId))
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Upgrades modifiers
    private val hpBonus: Float
    private val damageMultiplier: Float
    private val speedMultiplier: Float
    private val magnetBonus: Float
    private val cooldownReduction: Float
    private val goldMultiplier: Float

    init {
        var hp = 0f
        var dmg = 1f
        var spd = 1f
        var mag = 0f
        var cd = 1f
        var gold = 1f

        clinicUpgrades.forEach { u ->
            when (u.id) {
                ClinicUpgradeType.VITALIDADE_ESMALTE.id -> hp += u.level * 20f
                ClinicUpgradeType.POTENCIA_CLINICA.id -> dmg += u.level * 0.08f
                ClinicUpgradeType.MOBILIDADE_AGIL.id -> spd += u.level * 0.06f
                ClinicUpgradeType.IMA_HIGIENICO.id -> mag += u.level * 25f
                ClinicUpgradeType.RECARGA_RAPIDA.id -> cd *= (1f - u.level * 0.05f)
                ClinicUpgradeType.SORRISO_DE_OURO.id -> gold += u.level * 0.25f
            }
        }
        hpBonus = hp
        damageMultiplier = dmg * characterId.baseDamageMultiplier
        speedMultiplier = spd * characterId.baseSpeed
        magnetBonus = mag
        cooldownReduction = cd
        goldMultiplier = gold

        val startMaxHp = characterId.baseHp + hpBonus
        val startingWeapon = ActiveWeapon(type = characterId.startingWeapon, level = 1)

        _gameState.value = _gameState.value.copy(
            playerHp = startMaxHp,
            playerMaxHp = startMaxHp,
            weapons = listOf(startingWeapon),
            xpForNextLevel = calculateXpRequirement(1)
        )
    }

    // Entities in the battlefield
    val enemies = mutableListOf<EnemyInstance>()
    val projectiles = mutableListOf<ProjectileInstance>()
    val lootItems = mutableListOf<LootInstance>()
    val damageNumbers = mutableListOf<DamageNumber>()
    val particles = mutableListOf<VisualParticle>()

    private val idGen = AtomicLong(1)
    private var moveDirX = 0f
    private var moveDirY = 0f

    // Internal timers
    private var spawnTimer = 0f
    private var lastBrushAngle = 0f
    private var lastSonicPulseTimer = 0f
    private var bossSpawnedPhase1 = false
    private var bossSpawnedPhase2 = false
    private var infiniteBossTimer = 0f
    private var freezeTimer = 0f
    private var naturalRegenTimer = 0f
    private var barkTimer = 0f
    private var chestOpenCounter = 0
    private var shakeIntensity = 0f

    var onMatchComplete: ((kills: Int, timeSec: Int, lootCount: Int, bossesCount: Int, coinsEarned: Int) -> Unit)? = null
    var onAchievementTriggered: ((String) -> Unit)? = null
    var onItemDiscovered: ((String) -> Unit)? = null
    var onFusionTriggered: ((EvolutionType) -> Unit)? = null
    private var eliteSpawnedPreBoss = false

    fun setMoveDirection(x: Float, y: Float) {
        val len = sqrt(x * x + y * y)
        if (len > 0.05f) {
            moveDirX = (x / len).coerceIn(-1f, 1f)
            moveDirY = (y / len).coerceIn(-1f, 1f)
        } else {
            moveDirX = 0f
            moveDirY = 0f
        }
    }

    fun togglePause() {
        synchronized(stateLock) {
            val current = _gameState.value
            _gameState.value = current.copy(isPaused = !current.isPaused)
        }
    }

    fun getRenderSnapshot(): RenderSnapshot {
        synchronized(stateLock) {
            return RenderSnapshot(
                enemies = enemies.toList(),
                projectiles = projectiles.toList(),
                lootItems = lootItems.toList(),
                damageNumbers = damageNumbers.toList(),
                particles = particles.toList()
            )
        }
    }

    fun update(deltaSeconds: Float) {
        val state = _gameState.value
        if (state.isGameOver || state.isVictory || state.isPaused || state.isLevelingUp || state.isOpeningChest) {
            return
        }

        val dt = deltaSeconds.coerceIn(0.001f, 0.1f)
        var newGameTime = state.gameTimeSeconds + dt

        // Check Victory for 5-minute stages
        if (stageId != StageId.STAGE_INFINITE && newGameTime >= stageId.durationSeconds) {
            triggerVictory()
            return
        }

        // Screen Shake decay
        shakeIntensity = (shakeIntensity - dt * 25f).coerceAtLeast(0f)
        val shakeX = if (shakeIntensity > 0f) (Random.nextFloat() * 2f - 1f) * shakeIntensity else 0f
        val shakeY = if (shakeIntensity > 0f) (Random.nextFloat() * 2f - 1f) * shakeIntensity else 0f

        // Freeze countdown
        if (freezeTimer > 0f) {
            freezeTimer -= dt
            if (freezeTimer <= 0f) {
                freezeTimer = 0f
            }
        }

        // Passive bonuses calculation
        val passiveDmgBonus = 1f + (getPassiveLevel(PassiveType.PASTA_FLUOR) * 0.15f)
        val passiveCritBonus = getPassiveLevel(PassiveType.ESPELHO_PRECISAO) * 0.08f
        val passiveAreaBonus = 1f + (getPassiveLevel(PassiveType.FIO_EXTRA_FORTE) * 0.15f)
        val passiveCooldownBonus = (1f - getPassiveLevel(PassiveType.ENXAGUANTE_MENTA) * 0.10f).coerceAtLeast(0.4f)
        val passiveArmorReduction = (1f - getPassiveLevel(PassiveType.MASCARA_CIRURGICA) * 0.12f).coerceAtLeast(0.3f)
        val passiveSpeedBonus = 1f + (getPassiveLevel(PassiveType.LUVAS_LATEX) * 0.10f)
        val passiveMagnetBonus = getPassiveLevel(PassiveType.PASTILHA_REVELADORA) * 45f
        val regenLevel = getPassiveLevel(PassiveType.VITAMINA_C)

        synchronized(stateLock) {
            // Move Player
            val baseMoveSpeed = 160f * speedMultiplier * passiveSpeedBonus
            var newPlayerX = state.playerX + moveDirX * baseMoveSpeed * dt
            var newPlayerY = state.playerY + moveDirY * baseMoveSpeed * dt

            val worldLimit = WORLD_LIMIT
            newPlayerX = newPlayerX.coerceIn(-worldLimit, worldLimit)
            newPlayerY = newPlayerY.coerceIn(-worldLimit, worldLimit)

            // Health regen
            if (regenLevel > 0) {
                naturalRegenTimer += dt
                if (naturalRegenTimer >= 1.0f) {
                    naturalRegenTimer = 0f
                    healPlayer(regenLevel * 1.5f)
                }
            }

            // Bart's special bark
            if (characterId == CharacterId.BART) {
                barkTimer += dt
                if (barkTimer >= 4.0f) {
                    barkTimer = 0f
                    triggerSonicBark()
                }
            }

            // Update Weapons firing
            lastBrushAngle = (lastBrushAngle + 240f * dt) % 360f
            state.weapons.forEach { weapon ->
                updateWeapon(weapon, newPlayerX, newPlayerY, dt, passiveDmgBonus, passiveCritBonus, passiveAreaBonus, passiveCooldownBonus)
            }

            // Spawn Enemies
            updateEnemySpawning(dt, newGameTime, newPlayerX, newPlayerY)

            // Update Enemies
            val isFrozen = freezeTimer > 0f
            var currentBoss: EnemyInstance? = null

            for (enemy in enemies) {
                if (enemy.isDead) continue
                if (enemy.isBoss) currentBoss = enemy

                if (enemy.flashHitTimer > 0f) {
                    enemy.flashHitTimer -= dt
                }
                if (enemy.attackCooldown > 0f) {
                    enemy.attackCooldown -= dt
                }

                if (!isFrozen) {
                    val dx = newPlayerX - enemy.x
                    val dy = newPlayerY - enemy.y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist > 1f) {
                        enemy.x += (dx / dist) * enemy.speed * 60f * dt
                        enemy.y += (dy / dist) * enemy.speed * 60f * dt
                    }

                    if (dist < (enemy.radius + 18f)) {
                        if (enemy.attackCooldown <= 0f) {
                            enemy.attackCooldown = 0.8f
                            var dmg = enemy.damage * passiveArmorReduction
                            if (characterId == CharacterId.JULIO) dmg *= 0.80f
                            damagePlayer(dmg)
                        }
                    }
                }
            }

            // Update Projectiles
            for (p in projectiles) {
                if (p.isDead) continue
                p.durationRemaining -= dt
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.rotation += 360f * dt

                for (enemy in enemies) {
                    if (enemy.isDead) continue
                    val dx = enemy.x - p.x
                    val dy = enemy.y - p.y
                    val distSq = dx * dx + dy * dy
                    val hitDist = enemy.radius + p.radius
                    if (distSq <= hitDist * hitDist) {
                        val isCrit = Random.nextFloat() < (characterId.baseCritChance + passiveCritBonus)
                        val finalDmg = p.damage * (if (isCrit) 2.0f else 1.0f)
                        applyDamageToEnemy(enemy, finalDmg, isCrit)
                        spawnHitParticles(enemy.x, enemy.y, 4, 0xFF38BDF8)

                        p.pierceLeft--
                        if (p.pierceLeft <= 0) {
                            p.isDead = true
                            break
                        }
                    }
                }

                if (p.durationRemaining <= 0f || p.pierceLeft <= 0) {
                    p.isDead = true
                }
            }

            // Update Loot & Attraction
            val magnetRadius = (characterId.baseMagnetRadius + magnetBonus + passiveMagnetBonus)
            for (loot in lootItems) {
                if (loot.isDead) continue
                val dx = newPlayerX - loot.x
                val dy = newPlayerY - loot.y
                val dist = sqrt(dx * dx + dy * dy)

                if (dist < magnetRadius || loot.isAttracted) {
                    loot.isAttracted = true
                    val pullSpeed = 450f + (magnetRadius - dist).coerceAtLeast(0f) * 2f
                    loot.x += (dx / dist) * pullSpeed * dt
                    loot.y += (dy / dist) * pullSpeed * dt

                    if (dist < 28f) {
                        collectLoot(loot)
                        loot.isDead = true
                    }
                }
            }

            // Update Damage Numbers
            for (d in damageNumbers) {
                d.y -= 40f * dt
                d.life -= dt * 1.5f
            }

            // Update Particles
            for (pt in particles) {
                pt.x += pt.vx * dt
                pt.y += pt.vy * dt
                pt.life -= dt
            }

            // Safe removal of all dead/expired items
            enemies.removeAll { it.isDead }
            projectiles.removeAll { it.isDead }
            lootItems.removeAll { it.isDead }
            damageNumbers.removeAll { it.life <= 0f }
            particles.removeAll { it.life <= 0f }

            // Check Milestone Achievements
            if (newGameTime >= 300f) onAchievementTriggered?.invoke("survive_5min")
            if (newGameTime >= 600f) onAchievementTriggered?.invoke("survive_10min")

            // Update Boss HUD status
            val bossFraction = currentBoss?.let { it.hp / it.maxHp } ?: 0f
            val bossName = currentBoss?.type?.displayName

            _gameState.value = _gameState.value.copy(
                playerX = newPlayerX,
                playerY = newPlayerY,
                gameTimeSeconds = newGameTime,
                activeBossName = bossName,
                activeBossHpFraction = bossFraction,
                isFreezeActive = isFrozen,
                screenShakeX = shakeX,
                screenShakeY = shakeY
            )
        }
    }

    private fun updateWeapon(
        weapon: ActiveWeapon,
        playerX: Float,
        playerY: Float,
        dt: Float,
        dmgBonus: Float,
        critBonus: Float,
        areaBonus: Float,
        cooldownBonus: Float
    ) {
        val now = System.currentTimeMillis()
        val cdMs = if (weapon.isEvolved) {
            (weapon.evolvedType?.cooldownSeconds ?: 1.0f) * cooldownBonus * cooldownReduction * 1000f
        } else {
            weapon.type.cooldownSeconds * cooldownBonus * cooldownReduction * 1000f
        }

        if (weapon.type == WeaponType.ENXAGUANTE_AURA) {
            if (now - weapon.lastFiredTime >= cdMs) {
                weapon.lastFiredTime = now
                val auraRadius = weapon.type.rangeRadius * areaBonus * (1f + weapon.level * 0.15f)
                val auraDmg = weapon.type.baseDamage * dmgBonus * damageMultiplier * (1f + weapon.level * 0.2f)

                for (e in enemies) {
                    if (e.isDead) continue
                    val dx = e.x - playerX
                    val dy = e.y - playerY
                    if (dx * dx + dy * dy <= auraRadius * auraRadius) {
                        applyDamageToEnemy(e, auraDmg, false)
                    }
                }
                soundManager.playSplash()
            }
            return
        }

        if (weapon.isEvolved && weapon.evolvedType == EvolutionType.ESCOVA_ULTRASSONICA) {
            lastSonicPulseTimer += dt
            if (lastSonicPulseTimer >= 0.25f) {
                lastSonicPulseTimer = 0f
                val radius = EvolutionType.ESCOVA_ULTRASSONICA.rangeRadius * areaBonus
                val dmg = EvolutionType.ESCOVA_ULTRASSONICA.damage * dmgBonus * damageMultiplier

                for (e in enemies) {
                    if (e.isDead) continue
                    val dx = e.x - playerX
                    val dy = e.y - playerY
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist <= radius) {
                        applyDamageToEnemy(e, dmg, true)
                        if (dist > 1f && !e.isBoss) {
                            e.x += (dx / dist) * 45f
                            e.y += (dy / dist) * 45f
                        }
                    }
                }
                soundManager.playDrill()
                spawnHitParticles(playerX, playerY, 8, 0xFF67E8F9)
            }
            return
        }

        if (weapon.isEvolved && weapon.evolvedType == EvolutionType.FIO_LASER_TITANIO) {
            if (now - weapon.lastFiredTime >= cdMs) {
                weapon.lastFiredTime = now
                soundManager.playLaser()
                val angles = listOf(0f, 45f, 90f, 135f)
                val dmg = EvolutionType.FIO_LASER_TITANIO.damage * dmgBonus * damageMultiplier
                angles.forEach { ang ->
                    val rad = Math.toRadians(ang.toDouble()).toFloat()
                    val vx = cos(rad) * 600f
                    val vy = sin(rad) * 600f
                    projectiles.add(
                        ProjectileInstance(
                            id = idGen.incrementAndGet(),
                            x = playerX,
                            y = playerY,
                            vx = vx,
                            vy = vy,
                            damage = dmg,
                            durationRemaining = 1.2f,
                            pierceLeft = 999,
                            weaponType = weapon.type,
                            isEvolved = true,
                            isLaser = true,
                            radius = 24f * areaBonus
                        )
                    )
                }
            }
            return
        }

        if (now - weapon.lastFiredTime >= cdMs) {
            weapon.lastFiredTime = now
            val baseDmg = weapon.type.baseDamage * dmgBonus * damageMultiplier * (1f + (weapon.level - 1) * 0.25f)
            val range = weapon.type.rangeRadius * areaBonus

            when (weapon.type) {
                WeaponType.ESCOVA_DENTE -> {
                    soundManager.playBrush()
                    val count = 2 + weapon.level
                    for (i in 0 until count) {
                        val angle = lastBrushAngle + (360f / count) * i
                        val rad = Math.toRadians(angle.toDouble()).toFloat()
                        val bx = playerX + cos(rad) * range
                        val by = playerY + sin(rad) * range
                        projectiles.add(
                            ProjectileInstance(
                                id = idGen.incrementAndGet(),
                                x = bx,
                                y = by,
                                vx = cos(rad) * 120f,
                                vy = sin(rad) * 120f,
                                damage = baseDmg,
                                durationRemaining = 0.45f,
                                pierceLeft = 4,
                                weaponType = weapon.type,
                                radius = 22f * areaBonus
                            )
                        )
                    }
                }
                WeaponType.FIO_DENTAL -> {
                    soundManager.playFloss()
                    val nearest = findNearestEnemies(playerX, playerY, count = weapon.level)
                    if (nearest.isNotEmpty()) {
                        nearest.forEach { target ->
                            val dx = target.x - playerX
                            val dy = target.y - playerY
                            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                            val speed = 550f
                            projectiles.add(
                                ProjectileInstance(
                                    id = idGen.incrementAndGet(),
                                    x = playerX,
                                    y = playerY,
                                    vx = (dx / dist) * speed,
                                    vy = (dy / dist) * speed,
                                    damage = baseDmg,
                                    durationRemaining = 0.8f,
                                    pierceLeft = 3 + weapon.level,
                                    weaponType = weapon.type,
                                    radius = 16f * areaBonus
                                )
                            )
                        }
                    } else {
                        projectiles.add(
                            ProjectileInstance(
                                id = idGen.incrementAndGet(),
                                x = playerX,
                                y = playerY,
                                vx = 500f,
                                vy = 0f,
                                damage = baseDmg,
                                durationRemaining = 0.8f,
                                pierceLeft = 3,
                                weaponType = weapon.type,
                                radius = 16f
                            )
                        )
                    }
                }
                WeaponType.JATO_BICARBONATO -> {
                    soundManager.playWaterJet()
                    val target = findNearestEnemies(playerX, playerY, 1).firstOrNull()
                    val (vx, vy) = if (target != null) {
                        val dx = target.x - playerX
                        val dy = target.y - playerY
                        val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                        (dx / dist * 600f) to (dy / dist * 600f)
                    } else {
                        600f to 0f
                    }
                    projectiles.add(
                        ProjectileInstance(
                            id = idGen.incrementAndGet(),
                            x = playerX,
                            y = playerY,
                            vx = vx + Random.nextFloat() * 40f - 20f,
                            vy = vy + Random.nextFloat() * 40f - 20f,
                            damage = baseDmg,
                            durationRemaining = 0.7f,
                            pierceLeft = 2,
                            weaponType = weapon.type,
                            radius = 12f
                        )
                    )
                }
                WeaponType.BROCA_ODONTO -> {
                    soundManager.playDrill()
                    val numBurs = 1 + weapon.level
                    for (i in 0 until numBurs) {
                        val angle = (360f / numBurs) * i + (now % 3600) / 10f
                        val rad = Math.toRadians(angle.toDouble()).toFloat()
                        val bx = playerX + cos(rad) * range
                        val by = playerY + sin(rad) * range
                        projectiles.add(
                            ProjectileInstance(
                                id = idGen.incrementAndGet(),
                                x = bx,
                                y = by,
                                vx = -sin(rad) * 200f,
                                vy = cos(rad) * 200f,
                                damage = baseDmg,
                                durationRemaining = 0.5f,
                                pierceLeft = 5,
                                weaponType = weapon.type,
                                radius = 18f
                            )
                        )
                    }
                }
                WeaponType.LUZ_FOTOPOLIMERIZADORA -> {
                    soundManager.playLaser()
                    val target = findNearestEnemies(playerX, playerY, 1).firstOrNull()
                    val angle = if (target != null) {
                        atan2(target.y - playerY, target.x - playerX)
                    } else 0f
                    val vx = cos(angle) * 800f
                    val vy = sin(angle) * 800f
                    projectiles.add(
                        ProjectileInstance(
                            id = idGen.incrementAndGet(),
                            x = playerX,
                            y = playerY,
                            vx = vx,
                            vy = vy,
                            damage = baseDmg * 1.8f,
                            durationRemaining = 0.6f,
                            pierceLeft = 50,
                            weaponType = weapon.type,
                            isLaser = true,
                            radius = 28f * areaBonus
                        )
                    )
                }
                WeaponType.PASTA_EXPLOSIVA -> {
                    soundManager.playSplash()
                    val targets = findNearestEnemies(playerX, playerY, 2)
                    targets.forEach { target ->
                        val dx = target.x - playerX
                        val dy = target.y - playerY
                        val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                        projectiles.add(
                            ProjectileInstance(
                                id = idGen.incrementAndGet(),
                                x = playerX,
                                y = playerY,
                                vx = (dx / dist) * 380f,
                                vy = (dy / dist) * 380f,
                                damage = baseDmg,
                                durationRemaining = 0.9f,
                                pierceLeft = 1,
                                weaponType = weapon.type,
                                radius = 24f
                            )
                        )
                    }
                }
                WeaponType.ESPELHO_CLINICO -> {
                    val count = 3 + weapon.level
                    for (i in 0 until count) {
                        val angle = Random.nextFloat() * 360f
                        val rad = Math.toRadians(angle.toDouble()).toFloat()
                        projectiles.add(
                            ProjectileInstance(
                                id = idGen.incrementAndGet(),
                                x = playerX,
                                y = playerY,
                                vx = cos(rad) * 450f,
                                vy = sin(rad) * 450f,
                                damage = baseDmg,
                                durationRemaining = 0.6f,
                                pierceLeft = 3,
                                weaponType = weapon.type,
                                radius = 14f
                            )
                        )
                    }
                }
                WeaponType.SERINGA_ANESTESICA -> {
                    soundManager.playLaser()
                    val target = findNearestEnemies(playerX, playerY, 1).firstOrNull()
                    val (vx, vy) = if (target != null) {
                        val dx = target.x - playerX
                        val dy = target.y - playerY
                        val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                        (dx / dist * 650f) to (dy / dist * 650f)
                    } else {
                        500f to 0f
                    }
                    projectiles.add(
                        ProjectileInstance(
                            id = idGen.incrementAndGet(),
                            x = playerX,
                            y = playerY,
                            vx = vx,
                            vy = vy,
                            damage = baseDmg * 1.3f,
                            durationRemaining = 0.75f,
                            pierceLeft = 2 + weapon.level,
                            weaponType = weapon.type,
                            radius = 16f
                        )
                    )
                }
                WeaponType.SUGADOR_SALIVA -> {
                    soundManager.playSplash()
                    val vortexRadius = range * 1.2f
                    for (e in enemies) {
                        if (e.isDead) continue
                        val dx = playerX - e.x
                        val dy = playerY - e.y
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist <= vortexRadius && dist > 10f) {
                            if (!e.isBoss) {
                                e.x += (dx / dist) * 20f
                                e.y += (dy / dist) * 20f
                            }
                            applyDamageToEnemy(e, baseDmg * 0.4f, false)
                        }
                    }
                }
                WeaponType.CURETA_GRACEY -> {
                    soundManager.playFloss()
                    val count = 2 + weapon.level
                    for (i in 0 until count) {
                        val ang = (now % 360) + (360f / count) * i
                        val rad = Math.toRadians(ang.toDouble()).toFloat()
                        projectiles.add(
                            ProjectileInstance(
                                id = idGen.incrementAndGet(),
                                x = playerX + cos(rad) * 40f,
                                y = playerY + sin(rad) * 40f,
                                vx = cos(rad) * 320f,
                                vy = sin(rad) * 320f,
                                damage = baseDmg * 1.2f,
                                durationRemaining = 0.5f,
                                pierceLeft = 4,
                                weaponType = weapon.type,
                                radius = 20f
                            )
                        )
                    }
                }
                WeaponType.SPRAY_CLOREXIDINA -> {
                    soundManager.playWaterJet()
                    val count = 4 + weapon.level * 2
                    val baseAngle = (now % 360).toFloat()
                    for (i in 0 until count) {
                        val spread = (Random.nextFloat() * 80f) - 40f
                        val rad = Math.toRadians((baseAngle + spread).toDouble()).toFloat()
                        projectiles.add(
                            ProjectileInstance(
                                id = idGen.incrementAndGet(),
                                x = playerX,
                                y = playerY,
                                vx = cos(rad) * (250f + Random.nextFloat() * 150f),
                                vy = sin(rad) * (250f + Random.nextFloat() * 150f),
                                damage = baseDmg * 0.7f,
                                durationRemaining = 0.55f,
                                pierceLeft = 2,
                                weaponType = weapon.type,
                                radius = 14f
                            )
                        )
                    }
                }
                WeaponType.FITA_EXPANSIVA -> {
                    soundManager.playBrush()
                    val ringCount = 8 + weapon.level * 2
                    for (i in 0 until ringCount) {
                        val rad = Math.toRadians(((360f / ringCount) * i).toDouble()).toFloat()
                        projectiles.add(
                            ProjectileInstance(
                                id = idGen.incrementAndGet(),
                                x = playerX + cos(rad) * 20f,
                                y = playerY + sin(rad) * 20f,
                                vx = cos(rad) * 280f,
                                vy = sin(rad) * 280f,
                                damage = baseDmg,
                                durationRemaining = 0.65f,
                                pierceLeft = 3,
                                weaponType = weapon.type,
                                radius = 18f
                            )
                        )
                    }
                }
                WeaponType.ULTRASSOM_ODONTO -> {
                    soundManager.playDrill()
                    val targets = findNearestEnemies(playerX, playerY, 3)
                    targets.forEach { target ->
                        val dx = target.x - playerX
                        val dy = target.y - playerY
                        val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                        projectiles.add(
                            ProjectileInstance(
                                id = idGen.incrementAndGet(),
                                x = playerX,
                                y = playerY,
                                vx = (dx / dist) * 700f,
                                vy = (dy / dist) * 700f,
                                damage = baseDmg * 1.5f,
                                durationRemaining = 0.7f,
                                pierceLeft = 8,
                                weaponType = weapon.type,
                                isLaser = true,
                                radius = 26f
                            )
                        )
                    }
                }
                WeaponType.PINCA_CIRURGICA -> {
                    soundManager.playDrill()
                    val highestHpEnemy = enemies.filter { !it.isDead }.maxByOrNull { it.hp }
                    if (highestHpEnemy != null) {
                        applyDamageToEnemy(highestHpEnemy, baseDmg * 2.5f, true)
                        spawnHitParticles(highestHpEnemy.x, highestHpEnemy.y, 14, 0xFFE11D48)
                    }
                }
                else -> {}
            }
        }
    }

    private fun spawnPreBossElites(playerX: Float, playerY: Float) {
        val elite1 = EnemyType.ELITE_SUPER_BACTERIA
        val elite2 = EnemyType.ELITE_BIOFILME_TITAN
        val hpScale = 1.35f

        enemies.add(
            EnemyInstance(
                id = idGen.incrementAndGet(),
                x = playerX - 350f,
                y = playerY - 150f,
                hp = elite1.baseHp * hpScale,
                maxHp = elite1.baseHp * hpScale,
                type = elite1,
                speed = elite1.baseSpeed,
                damage = elite1.baseDamage,
                radius = elite1.radius
            )
        )
        enemies.add(
            EnemyInstance(
                id = idGen.incrementAndGet(),
                x = playerX + 350f,
                y = playerY + 150f,
                hp = elite2.baseHp * hpScale,
                maxHp = elite2.baseHp * hpScale,
                type = elite2,
                speed = elite2.baseSpeed,
                damage = elite2.baseDamage,
                radius = elite2.radius
            )
        )
        soundManager.playLaser()
    }

    private fun updateEnemySpawning(dt: Float, gameTime: Float, playerX: Float, playerY: Float) {
        spawnTimer += dt
        val baseSpawnInterval = (1.5f - (gameTime / 300f).coerceAtMost(1.0f)).coerceAtLeast(0.35f)

        if (spawnTimer >= baseSpawnInterval) {
            spawnTimer = 0f
            val count = 2 + (gameTime / 60f).toInt()
            repeat(count) {
                spawnRandomEnemy(gameTime, playerX, playerY)
            }
        }

        if (stageId != StageId.STAGE_INFINITE) {
            if (gameTime >= 150f && !bossSpawnedPhase1) {
                bossSpawnedPhase1 = true
                spawnStageBoss(stageId, playerX, playerY, isFinal = false)
            }
            if (gameTime >= 210f && !eliteSpawnedPreBoss) {
                eliteSpawnedPreBoss = true
                spawnPreBossElites(playerX, playerY)
            }
            if (gameTime >= 270f && !bossSpawnedPhase2) {
                bossSpawnedPhase2 = true
                spawnStageBoss(stageId, playerX, playerY, isFinal = true)
            }
        } else {
            infiniteBossTimer += dt
            if (infiniteBossTimer >= 90f) {
                infiniteBossTimer = 0f
                val bossList = listOf(
                    EnemyType.BOSS_CARIE_GIGANTE,
                    EnemyType.BOSS_GENGIVITE,
                    EnemyType.BOSS_ABCESSO,
                    EnemyType.BOSS_BIOFILME_APARELHO,
                    EnemyType.BOSS_TARTARO_TITANICO
                )
                spawnBossInstance(bossList.random(), playerX, playerY)
            }
        }
    }

    private fun spawnRandomEnemy(gameTime: Float, playerX: Float, playerY: Float) {
        if (enemies.size > 140) return

        val angle = Random.nextFloat() * 2f * PI.toFloat()
        val spawnDist = 420f + Random.nextFloat() * 100f
        val x = playerX + cos(angle) * spawnDist
        val y = playerY + sin(angle) * spawnDist

        val type = when (stageId) {
            StageId.STAGE_1_ESMALTE -> if (Random.nextFloat() < 0.7f) EnemyType.STREPTOCOCCUS else EnemyType.PLACA_AMARELA
            StageId.STAGE_2_GENGIVA -> if (Random.nextFloat() < 0.6f) EnemyType.ESPIROQUETA else EnemyType.ACIDO_CORROSIVO
            StageId.STAGE_3_CANAL -> if (Random.nextFloat() < 0.5f) EnemyType.ACIDO_CORROSIVO else EnemyType.TARTARO_MINION
            StageId.STAGE_4_APARELHO -> if (Random.nextFloat() < 0.5f) EnemyType.PLACA_AMARELA else EnemyType.ESPIROQUETA
            StageId.STAGE_5_TARTARO -> if (Random.nextFloat() < 0.4f) EnemyType.TARTARO_MINION else EnemyType.ACIDO_CORROSIVO
            StageId.STAGE_INFINITE -> {
                val types = EnemyType.values().filter { !it.name.startsWith("BOSS_") }
                types.random()
            }
        }

        val hpScale = 1f + (gameTime / 180f)
        val hp = type.baseHp * hpScale
        enemies.add(
            EnemyInstance(
                id = idGen.incrementAndGet(),
                x = x,
                y = y,
                hp = hp,
                maxHp = hp,
                type = type,
                speed = type.baseSpeed,
                damage = type.baseDamage,
                radius = type.radius
            )
        )
    }

    private fun spawnStageBoss(stage: StageId, playerX: Float, playerY: Float, isFinal: Boolean) {
        val bossType = when (stage) {
            StageId.STAGE_1_ESMALTE -> EnemyType.BOSS_CARIE_GIGANTE
            StageId.STAGE_2_GENGIVA -> EnemyType.BOSS_GENGIVITE
            StageId.STAGE_3_CANAL -> EnemyType.BOSS_ABCESSO
            StageId.STAGE_4_APARELHO -> EnemyType.BOSS_BIOFILME_APARELHO
            StageId.STAGE_5_TARTARO -> EnemyType.BOSS_TARTARO_TITANICO
            else -> EnemyType.BOSS_CARIE_GIGANTE
        }
        spawnBossInstance(bossType, playerX, playerY)
    }

    private fun spawnBossInstance(bossType: EnemyType, playerX: Float, playerY: Float) {
        val angle = Random.nextFloat() * 2f * PI.toFloat()
        val x = playerX + cos(angle) * 350f
        val y = playerY + sin(angle) * 350f

        soundManager.playBossRoar()
        shakeIntensity = 18f // Screen shake on boss spawn!

        enemies.add(
            EnemyInstance(
                id = idGen.incrementAndGet(),
                x = x,
                y = y,
                hp = bossType.baseHp,
                maxHp = bossType.baseHp,
                type = bossType,
                speed = bossType.baseSpeed,
                damage = bossType.baseDamage,
                radius = bossType.radius,
                isBoss = true
            )
        )
    }

    private fun applyDamageToEnemy(enemy: EnemyInstance, damage: Float, isCrit: Boolean) {
        if (enemy.isDead) return
        enemy.hp -= damage
        enemy.flashHitTimer = 0.12f

        damageNumbers.add(
            DamageNumber(
                id = idGen.incrementAndGet(),
                x = enemy.x + Random.nextFloat() * 16f - 8f,
                y = enemy.y - enemy.radius,
                text = "${damage.toInt()}",
                isCrit = isCrit,
                color = if (isCrit) 0xFFFBBF24 else 0xFFFFFFFF
            )
        )

        if (enemy.hp <= 0f) {
            eliminateEnemy(enemy)
        }
    }

    private fun eliminateEnemy(enemy: EnemyInstance) {
        if (enemy.isDead) return
        enemy.isDead = true

        val state = _gameState.value
        val newKills = state.kills + 1
        _gameState.value = state.copy(kills = newKills)

        soundManager.playHit()
        spawnHitParticles(enemy.x, enemy.y, 6, enemy.type.colorHex)

        if (enemy.isBoss) {
            shakeIntensity = 14f
            lootItems.add(
                LootInstance(
                    id = idGen.incrementAndGet(),
                    x = enemy.x,
                    y = enemy.y,
                    type = LootType.BAU_DOURADO
                )
            )
            repeat(5) {
                lootItems.add(
                    LootInstance(
                        id = idGen.incrementAndGet(),
                        x = enemy.x + Random.nextFloat() * 40f - 20f,
                        y = enemy.y + Random.nextFloat() * 40f - 20f,
                        type = LootType.DENTE_OURO
                    )
                )
            }

            when (enemy.type) {
                EnemyType.BOSS_CARIE_GIGANTE -> onAchievementTriggered?.invoke("boss_carie")
                EnemyType.BOSS_GENGIVITE -> onAchievementTriggered?.invoke("boss_gengivite")
                EnemyType.BOSS_ABCESSO -> onAchievementTriggered?.invoke("boss_abcesso")
                EnemyType.BOSS_TARTARO_TITANICO -> onAchievementTriggered?.invoke("boss_tartaro")
                else -> {}
            }

            // Defeating final boss in standard stages completes the stage
            if (stageId != StageId.STAGE_INFINITE && bossSpawnedPhase2) {
                triggerVictory()
                return
            }
        } else {
            val dropType = if (enemy.type.isElite) {
                LootType.BAU_MEGA_XP
            } else {
                val dropRoll = Random.nextFloat()
                when {
                    dropRoll < 0.025f -> LootType.CAPSULA_ANALGESICO
                    dropRoll < 0.055f -> LootType.MACA_HEAL
                    dropRoll < 0.075f -> LootType.BAU_PRATA
                    dropRoll < 0.090f -> LootType.PASTILHA_VACUUM
                    dropRoll < 0.105f -> LootType.ENXAGUANTE_FREEZE
                    dropRoll < 0.120f -> LootType.BAU_DOURADO
                    dropRoll < 0.200f -> LootType.DENTE_OURO
                    dropRoll < 0.450f -> enemy.type.xpDrop
                    else -> LootType.FLUOR_XP
                }
            }

            lootItems.add(
                LootInstance(
                    id = idGen.incrementAndGet(),
                    x = enemy.x,
                    y = enemy.y,
                    type = dropType
                )
            )
        }

        if (newKills == 1) onAchievementTriggered?.invoke("first_germ")
    }

    private fun collectLoot(loot: LootInstance) {
        val state = _gameState.value
        when (loot.type) {
            LootType.FLUOR_XP, LootType.CRISTAL_XILITOL -> {
                soundManager.playGemPickup()
                val xpBonus = if (characterId == CharacterId.DRA_ALINNY) 1.15f else 1.0f
                addXp((loot.type.xpValue * xpBonus).toInt())
            }
            LootType.MACA_HEAL, LootType.CAPSULA_ANALGESICO -> {
                soundManager.playSplash()
                healPlayer(loot.type.healAmount)
                spawnHitParticles(state.playerX, state.playerY, 10, loot.type.color)
            }
            LootType.PASTILHA_VACUUM -> {
                soundManager.playLaser()
                lootItems.forEach { it.isAttracted = true }
            }
            LootType.ENXAGUANTE_FREEZE -> {
                soundManager.playSplash()
                freezeTimer = 4.5f
            }
            LootType.BAU_PRATA, LootType.BAU_DOURADO, LootType.BAU_DIAMANTE -> {
                openChest(loot.type)
            }
            LootType.BAU_MEGA_XP -> {
                soundManager.playLevelUp()
                val neededXp = (state.xpForNextLevel - state.currentXp).coerceAtLeast(40)
                addXp(neededXp)
                openChest(loot.type)
            }
            LootType.DENTE_OURO -> {
                soundManager.playGemPickup(3)
                val coins = (15 * goldMultiplier).toInt()
                _gameState.value = state.copy(dentalCoins = state.dentalCoins + coins)
            }
        }
    }

    private fun addXp(amount: Int) {
        val state = _gameState.value
        var newXp = state.currentXp + amount
        var newLevel = state.level
        var requiredXp = state.xpForNextLevel

        if (newXp >= requiredXp) {
            newXp -= requiredXp
            newLevel++
            requiredXp = calculateXpRequirement(newLevel)
            soundManager.playLevelUp()
            triggerLevelUp(newLevel, newXp, requiredXp)
        } else {
            _gameState.value = state.copy(currentXp = newXp)
        }
    }

    private fun calculateXpRequirement(lvl: Int): Int {
        return (35 + lvl * 25 + (lvl * lvl * 4))
    }

    private fun triggerLevelUp(newLevel: Int, currentXp: Int, requiredXp: Int) {
        val choices = generateLevelUpChoices()
        _gameState.value = _gameState.value.copy(
            level = newLevel,
            currentXp = currentXp,
            xpForNextLevel = requiredXp,
            isLevelingUp = true,
            levelUpChoices = choices
        )
        if (newLevel >= 10) onAchievementTriggered?.invoke("reach_level_10")
        if (newLevel >= 20) onAchievementTriggered?.invoke("reach_level_20")
    }

    private fun openChest(rarity: LootType = LootType.BAU_DOURADO) {
        soundManager.playChestOpen()
        chestOpenCounter++
        val baseCoins = when (rarity) {
            LootType.BAU_PRATA -> 35
            LootType.BAU_DOURADO -> 75
            LootType.BAU_DIAMANTE -> 180
            LootType.BAU_MEGA_XP -> 120
            else -> 50
        }
        val coins = (baseCoins * goldMultiplier).toInt()
        if (rarity.healAmount > 0f) {
            healPlayer(rarity.healAmount)
        }

        val readyEvolution = findAvailableEvolution()
        val option = readyEvolution ?: generateLevelUpChoices().firstOrNull()

        _gameState.value = _gameState.value.copy(
            isOpeningChest = true,
            chestReward = option,
            chestCoins = coins,
            dentalCoins = _gameState.value.dentalCoins + coins
        )
        onAchievementTriggered?.invoke("chest_open_5")
    }

    fun dismissChest() {
        val state = _gameState.value
        state.chestReward?.let { applyLevelUpChoice(it) }
        _gameState.value = state.copy(isOpeningChest = false, chestReward = null)
    }

    private fun findAvailableEvolution(): LevelUpOption.EvolveWeapon? {
        val state = _gameState.value
        for (w in state.weapons) {
            if (!w.isEvolved && w.level >= w.type.maxLevel && w.type.evolvedWeapon != null && w.type.evolutionItem != null) {
                val hasPassive = state.passives.any { it.type == w.type.evolutionItem }
                if (hasPassive) {
                    return LevelUpOption.EvolveWeapon(w, w.type.evolvedWeapon)
                }
            }
        }
        return null
    }

    fun generateLevelUpChoices(): List<LevelUpOption> {
        val state = _gameState.value
        val options = mutableListOf<LevelUpOption>()

        val evolution = findAvailableEvolution()
        if (evolution != null) {
            options.add(evolution)
        }

        state.weapons.filter { !it.isEvolved && it.level < it.type.maxLevel }.forEach {
            options.add(LevelUpOption.UpgradeWeapon(it))
        }

        // Limit total weapons + passives to maximum 8
        val totalSlots = state.weapons.size + state.passives.size
        val canAddMore = totalSlots < 8

        if (canAddMore && state.weapons.size < 4) {
            val existingTypes = state.weapons.map { it.type }.toSet()
            WeaponType.values()
                .filter { it !in existingTypes && it.unlockedAtStage <= highestStageUnlocked }
                .shuffled()
                .take(2)
                .forEach {
                    options.add(LevelUpOption.NewWeapon(it))
                }
        }

        state.passives.filter { it.level < it.type.maxLevel }.forEach {
            options.add(LevelUpOption.UpgradePassive(it))
        }

        if (canAddMore && state.passives.size < 4) {
            val existingPassives = state.passives.map { it.type }.toSet()
            PassiveType.values()
                .filter { it !in existingPassives && it.unlockedAtStage <= highestStageUnlocked }
                .shuffled()
                .take(2)
                .forEach {
                    options.add(LevelUpOption.NewPassive(it))
                }
        }

        if (options.isEmpty()) {
            return listOf(LevelUpOption.HealthBonus(), LevelUpOption.CoinBonus())
        }

        return if (evolution != null) {
            val rest = options.filter { it != evolution }.shuffled().take(2)
            listOf(evolution) + rest
        } else {
            options.shuffled().take(3)
        }
    }

    fun selectLevelUpChoice(option: LevelUpOption) {
        applyLevelUpChoice(option)
        _gameState.value = _gameState.value.copy(
            isLevelingUp = false,
            levelUpChoices = emptyList()
        )
    }

    private fun applyLevelUpChoice(option: LevelUpOption) {
        val state = _gameState.value
        val currentWeapons = state.weapons.toMutableList()
        val currentPassives = state.passives.toMutableList()

        when (option) {
            is LevelUpOption.HealthBonus -> {
                healPlayer(option.healAmount)
            }
            is LevelUpOption.CoinBonus -> {
                _gameState.value = state.copy(dentalCoins = state.dentalCoins + option.coinAmount)
            }
            is LevelUpOption.NewWeapon -> {
                currentWeapons.add(ActiveWeapon(type = option.weaponType, level = 1))
                onItemDiscovered?.invoke(option.weaponType.name)
            }
            is LevelUpOption.UpgradeWeapon -> {
                val idx = currentWeapons.indexOfFirst { it.type == option.weapon.type }
                if (idx != -1) {
                    val w = currentWeapons[idx]
                    currentWeapons[idx] = w.copy(level = w.level + 1)
                }
                onItemDiscovered?.invoke(option.weapon.type.name)
            }
            is LevelUpOption.NewPassive -> {
                currentPassives.add(ActivePassive(type = option.passiveType, level = 1))
                onItemDiscovered?.invoke(option.passiveType.name)
            }
            is LevelUpOption.UpgradePassive -> {
                val idx = currentPassives.indexOfFirst { it.type == option.passive.type }
                if (idx != -1) {
                    val p = currentPassives[idx]
                    currentPassives[idx] = p.copy(level = p.level + 1)
                }
                onItemDiscovered?.invoke(option.passive.type.name)
            }
            is LevelUpOption.EvolveWeapon -> {
                val idx = currentWeapons.indexOfFirst { it.type == option.weapon.type }
                if (idx != -1) {
                    currentWeapons[idx] = currentWeapons[idx].copy(
                        isEvolved = true,
                        evolvedType = option.evolution
                    )
                }
                onItemDiscovered?.invoke(option.evolution.name)
                onFusionTriggered?.invoke(option.evolution)
                if (option.evolution == EvolutionType.ESCOVA_ULTRASSONICA) {
                    onAchievementTriggered?.invoke("evolve_brush")
                }
                if (option.evolution == EvolutionType.FIO_LASER_TITANIO) {
                    onAchievementTriggered?.invoke("evolve_floss")
                }
                val hasBrushEvo = currentWeapons.any { it.evolvedType == EvolutionType.ESCOVA_ULTRASSONICA }
                val hasFlossEvo = currentWeapons.any { it.evolvedType == EvolutionType.FIO_LASER_TITANIO }
                if (hasBrushEvo && hasFlossEvo) {
                    onAchievementTriggered?.invoke("dual_evolution")
                }
            }
        }
        _gameState.value = state.copy(weapons = currentWeapons, passives = currentPassives)
    }

    private fun damagePlayer(amount: Float) {
        val state = _gameState.value
        val newHp = (state.playerHp - amount).coerceAtLeast(0f)
        shakeIntensity = 8f
        _gameState.value = state.copy(playerHp = newHp)
        soundManager.playHit()

        if (newHp <= 0f) {
            triggerGameOver()
        }
    }

    private fun healPlayer(amount: Float) {
        val state = _gameState.value
        val newHp = (state.playerHp + amount).coerceAtMost(state.playerMaxHp)
        _gameState.value = state.copy(playerHp = newHp)
        damageNumbers.add(
            DamageNumber(
                id = idGen.incrementAndGet(),
                x = state.playerX,
                y = state.playerY - 20f,
                text = "+${amount.toInt()}",
                isCrit = false,
                color = 0xFF22C55E
            )
        )
    }

    private fun triggerSonicBark() {
        soundManager.playBossRoar()
        shakeIntensity = 10f
        val state = _gameState.value
        val barkRadius = 260f
        val barkDmg = 50f * damageMultiplier
        for (e in enemies) {
            if (e.isDead) continue
            val dx = e.x - state.playerX
            val dy = e.y - state.playerY
            val dist = sqrt(dx * dx + dy * dy)
            if (dist <= barkRadius) {
                applyDamageToEnemy(e, barkDmg, true)
                if (dist > 1f && !e.isBoss) {
                    e.x += (dx / dist) * 70f
                    e.y += (dy / dist) * 70f
                }
            }
        }
        spawnHitParticles(state.playerX, state.playerY, 12, 0xFFF97316)
    }

    private fun triggerGameOver() {
        soundManager.playGameOver()
        val state = _gameState.value
        _gameState.value = state.copy(isGameOver = true)
        onMatchComplete?.invoke(
            state.kills,
            state.gameTimeSeconds.toInt(),
            lootItems.size,
            0,
            state.dentalCoins
        )
    }

    private fun triggerVictory() {
        soundManager.playLevelUp()
        val state = _gameState.value
        _gameState.value = state.copy(isVictory = true)
        onMatchComplete?.invoke(
            state.kills,
            state.gameTimeSeconds.toInt(),
            lootItems.size,
            1,
            state.dentalCoins + 100
        )
    }

    private fun spawnHitParticles(x: Float, y: Float, count: Int, colorHex: Long) {
        repeat(count) {
            val ang = Random.nextFloat() * 2f * PI.toFloat()
            val spd = 60f + Random.nextFloat() * 80f
            particles.add(
                VisualParticle(
                    id = idGen.incrementAndGet(),
                    x = x,
                    y = y,
                    vx = cos(ang) * spd,
                    vy = sin(ang) * spd,
                    color = colorHex,
                    radius = 3f + Random.nextFloat() * 4f,
                    life = 0.35f,
                    maxLife = 0.35f
                )
            )
        }
    }

    private fun findNearestEnemies(px: Float, py: Float, count: Int): List<EnemyInstance> {
        return enemies.filter { !it.isDead }.sortedBy { e ->
            val dx = e.x - px
            val dy = e.y - py
            dx * dx + dy * dy
        }.take(count)
    }

    private fun getPassiveLevel(type: PassiveType): Int {
        return _gameState.value.passives.find { it.type == type }?.level ?: 0
    }

    companion object {
        const val WORLD_LIMIT = 3200f
    }
}

