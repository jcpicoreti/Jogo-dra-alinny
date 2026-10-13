package com.example.data.repository

import android.content.Context
import com.example.data.local.AchievementEntity
import com.example.data.local.AppDatabase
import com.example.data.local.ClinicUpgradeEntity
import com.example.data.local.DiscoveredItemEntity
import com.example.data.local.PlayerMetaEntity
import com.example.data.local.ScoreEntity
import com.example.data.local.UnlockedCharacterEntity
import com.example.data.model.CharacterId
import com.example.data.model.ClinicUpgradeType
import com.example.data.model.StageId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val maxProgress: Int,
    val iconName: String = "trophy"
)

val ALL_ACHIEVEMENTS = listOf(
    AchievementDef("first_germ", "Primeira Consulta", "Elimine sua primeira bactéria bucal.", 1),
    AchievementDef("kills_100", "Boca Limpa", "Elimine 100 germes no total.", 100),
    AchievementDef("kills_500", "Sorriso Radiante", "Elimine 500 germes no total.", 500),
    AchievementDef("kills_1000", "Especialista em Higiene", "Elimine 1.000 germes e desbloqueie Lorenzo!", 1000),
    AchievementDef("kills_5000", "Exterminador de Bactérias", "Elimine 5.000 germes no total.", 5000),
    AchievementDef("boss_carie", "Adeus Cárie", "Derrote a Cárie Gigante e desbloqueie Raynara!", 1),
    AchievementDef("boss_gengivite", "Gengivas Saudáveis", "Derrote o chefe Gengivite Furiosa.", 1),
    AchievementDef("boss_abcesso", "Mestre do Canal", "Derrote o Abcesso Periapical no Canal Radicular.", 1),
    AchievementDef("boss_tartaro", "Destruidor de Tártaro", "Derrote o Tártaro Titânico e desbloqueie Julio!", 1),
    AchievementDef("stage_aparelho", "Sorriso de Metal", "Sobreviva ao labirinto dos braquetes.", 1),
    AchievementDef("evolve_brush", "Escova Ultrassônica", "Evolua a Escova de Dente para a Escova Ultrassônica!", 1),
    AchievementDef("evolve_floss", "Laser de Fio de Titânio", "Evolua o Fio Dental para Fio de Titânio Laser!", 1),
    AchievementDef("dual_evolution", "Dupla Evolução Bucal", "Evolua a escova e o fio dental na mesma partida e desbloqueie Bart!", 1),
    AchievementDef("survive_5min", "Primeiro Plantão", "Sobreviva por 5 minutos em uma única partida.", 1),
    AchievementDef("survive_10min", "Sobrevivente do Plantão", "Sobreviva por mais de 10 minutos em uma partida.", 1),
    AchievementDef("reach_level_10", "Doutorando", "Alcance o nível 10 de higiene em uma partida.", 1),
    AchievementDef("reach_level_20", "Mestre em Odontologia", "Alcance o nível 20 de higiene em uma partida.", 1),
    AchievementDef("collect_loot_100", "Gotas de Flúor", "Colete 100 itens de higiene bucal.", 100),
    AchievementDef("collect_loot_500", "Amigo dos Bichos", "Colete 500 itens de higiene bucal e desbloqueie Rick!", 500),
    AchievementDef("clinic_first_upgrade", "Clínica Renovada", "Compre sua primeira melhoria permanente na clínica.", 1),
    AchievementDef("chest_open_5", "Tesouros de Higiene", "Abra 5 estojos de higiene bucal dourados.", 5),
    AchievementDef("infinite_survival", "Plantão Imortal", "Sobreviva 12 minutos no Modo Infinito.", 1)
)

class GameRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val scoreDao = database.scoreDao()
    private val achievementDao = database.achievementDao()
    private val upgradeDao = database.clinicUpgradeDao()
    private val characterDao = database.characterDao()
    private val playerMetaDao = database.playerMetaDao()
    private val discoveredItemDao = database.discoveredItemDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            // Ensure default character Dra Alinny is unlocked
            characterDao.unlockCharacter(UnlockedCharacterEntity(CharacterId.DRA_ALINNY.name, true))
            // Mark starting weapon discovered
            markItemDiscovered(CharacterId.DRA_ALINNY.startingWeapon.name)
            
            // Ensure player meta exists
            val currentMeta = playerMetaDao.getPlayerMetaSync()
            if (currentMeta == null) {
                playerMetaDao.updatePlayerMeta(
                    PlayerMetaEntity(
                        id = 1,
                        dentalCoins = 150, // Welcome gift of coins!
                        totalGermsEliminated = 0,
                        totalTimePlayedSeconds = 0,
                        totalLootCollected = 0,
                        bossesDefeated = 0
                    )
                )
            }
        }
    }

    // High Scores
    fun getHighScores(): Flow<List<ScoreEntity>> = scoreDao.getAllHighScores()
    fun getHighScoresForStage(stageId: String): Flow<List<ScoreEntity>> = scoreDao.getHighScoresForStage(stageId)
    suspend fun saveHighScore(score: ScoreEntity): Long = scoreDao.insertScore(score)

    // Player Meta & Coins
    fun getPlayerMeta(): Flow<PlayerMetaEntity?> = playerMetaDao.getPlayerMeta()
    
    suspend fun addCoins(amount: Int) {
        val meta = playerMetaDao.getPlayerMetaSync() ?: PlayerMetaEntity()
        playerMetaDao.updatePlayerMeta(meta.copy(dentalCoins = meta.dentalCoins + amount))
    }

    suspend fun recordMatchStats(
        germsKilled: Int,
        timeSeconds: Int,
        lootCollected: Int,
        bossesKilled: Int,
        coinsEarned: Int,
        isVictory: Boolean = false,
        stage: StageId = StageId.STAGE_1_ESMALTE
    ): List<String> {
        val newlyUnlocked = mutableListOf<String>()
        val meta = playerMetaDao.getPlayerMetaSync() ?: PlayerMetaEntity()
        val nextUnlockedStage = if (isVictory && stage.stageNumber >= meta.highestStageUnlocked && stage.stageNumber < 6) {
            stage.stageNumber + 1
        } else {
            meta.highestStageUnlocked
        }

        val newMeta = meta.copy(
            dentalCoins = meta.dentalCoins + coinsEarned,
            totalGermsEliminated = meta.totalGermsEliminated + germsKilled,
            totalTimePlayedSeconds = meta.totalTimePlayedSeconds + timeSeconds,
            totalLootCollected = meta.totalLootCollected + lootCollected,
            bossesDefeated = meta.bossesDefeated + bossesKilled,
            highestStageUnlocked = nextUnlockedStage
        )
        playerMetaDao.updatePlayerMeta(newMeta)

        // Check milestones
        if (updateAchievementProgress("kills_100", newMeta.totalGermsEliminated)) newlyUnlocked.add("kills_100")
        if (updateAchievementProgress("kills_500", newMeta.totalGermsEliminated)) newlyUnlocked.add("kills_500")
        if (updateAchievementProgress("kills_1000", newMeta.totalGermsEliminated)) newlyUnlocked.add("kills_1000")
        if (updateAchievementProgress("kills_5000", newMeta.totalGermsEliminated)) newlyUnlocked.add("kills_5000")
        if (updateAchievementProgress("collect_loot_100", newMeta.totalLootCollected)) newlyUnlocked.add("collect_loot_100")
        if (updateAchievementProgress("collect_loot_500", newMeta.totalLootCollected)) newlyUnlocked.add("collect_loot_500")

        if (isVictory && stage == StageId.STAGE_4_APARELHO) {
            if (unlockAchievement("stage_aparelho")) newlyUnlocked.add("stage_aparelho")
        }
        if (stage == StageId.STAGE_INFINITE && timeSeconds >= 720) {
            if (unlockAchievement("infinite_survival")) newlyUnlocked.add("infinite_survival")
        }

        // Character Unlocks check
        if (newMeta.totalGermsEliminated >= 1000) {
            unlockCharacter(CharacterId.LORENZO)
        }
        if (newMeta.totalLootCollected >= 500) {
            unlockCharacter(CharacterId.RICK)
        }
        return newlyUnlocked
    }

    // Achievements
    fun getAllAchievements(): Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()

    suspend fun unlockAchievement(achievementId: String): Boolean {
        val existing = achievementDao.getAchievement(achievementId)
        if (existing?.isUnlocked == true) return false
        val def = ALL_ACHIEVEMENTS.find { it.id == achievementId }
        val maxProg = def?.maxProgress ?: 1
        achievementDao.insertOrUpdate(
            AchievementEntity(
                id = achievementId,
                isUnlocked = true,
                currentProgress = maxProg,
                unlockedTimestamp = System.currentTimeMillis()
            )
        )
        
        // Character Unlocks tied to specific achievements
        if (achievementId == "boss_carie") unlockCharacter(CharacterId.RAYNARA)
        if (achievementId == "boss_tartaro") unlockCharacter(CharacterId.JULIO)
        if (achievementId == "dual_evolution") unlockCharacter(CharacterId.BART)
        if (achievementId == "kills_1000") unlockCharacter(CharacterId.LORENZO)
        if (achievementId == "collect_loot_500") unlockCharacter(CharacterId.RICK)
        
        return true
    }

    suspend fun updateAchievementProgress(achievementId: String, currentProgress: Int): Boolean {
        val def = ALL_ACHIEVEMENTS.find { it.id == achievementId } ?: return false
        val existing = achievementDao.getAchievement(achievementId)
        if (existing?.isUnlocked == true) return false

        val newProg = currentProgress.coerceAtMost(def.maxProgress)
        val isNowUnlocked = newProg >= def.maxProgress
        
        if (isNowUnlocked) {
            return unlockAchievement(achievementId)
        } else {
            achievementDao.insertOrUpdate(
                AchievementEntity(
                    id = achievementId,
                    isUnlocked = false,
                    currentProgress = newProg,
                    unlockedTimestamp = null
                )
            )
            return false
        }
    }

    // Character Unlocks
    fun getUnlockedCharacters(): Flow<List<UnlockedCharacterEntity>> = characterDao.getAllUnlockedCharacters()

    suspend fun unlockCharacter(characterId: CharacterId) {
        characterDao.unlockCharacter(UnlockedCharacterEntity(characterId.name, true))
    }

    // Clinic Upgrades
    fun getClinicUpgrades(): Flow<List<ClinicUpgradeEntity>> = upgradeDao.getAllUpgrades()

    suspend fun purchaseUpgrade(upgradeType: ClinicUpgradeType): Boolean {
        val meta = playerMetaDao.getPlayerMetaSync() ?: return false
        val currentUpgrade = upgradeDao.getUpgrade(upgradeType.id)
        val currentLevel = currentUpgrade?.level ?: 0
        if (currentLevel >= upgradeType.maxLevel) return false

        val cost = calculateUpgradeCost(upgradeType, currentLevel)
        if (meta.dentalCoins < cost) return false

        playerMetaDao.updatePlayerMeta(meta.copy(dentalCoins = meta.dentalCoins - cost))
        upgradeDao.setUpgradeLevel(ClinicUpgradeEntity(upgradeType.id, currentLevel + 1))
        unlockAchievement("clinic_first_upgrade")
        return true
    }

    fun calculateUpgradeCost(upgradeType: ClinicUpgradeType, currentLevel: Int): Int {
        var cost = upgradeType.baseCost.toFloat()
        repeat(currentLevel) {
            cost *= upgradeType.costMultiplier
        }
        return cost.toInt()
    }

    // Discovered Library Items (Armas, Equipamentos e Fusões)
    fun getAllDiscoveredItems(): Flow<List<DiscoveredItemEntity>> = discoveredItemDao.getAllDiscoveredItems()

    suspend fun markItemDiscovered(itemId: String) {
        val existing = discoveredItemDao.getDiscoveredItem(itemId)
        if (existing == null) {
            discoveredItemDao.markItemDiscovered(
                DiscoveredItemEntity(
                    itemId = itemId,
                    isDiscovered = true,
                    timesUsed = 1,
                    discoveredTimestamp = System.currentTimeMillis()
                )
            )
        } else {
            discoveredItemDao.markItemDiscovered(
                existing.copy(timesUsed = existing.timesUsed + 1)
            )
        }
    }
}
