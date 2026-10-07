package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playerName: String,
    val characterId: String,
    val stageId: String,
    val timeSurvivedSeconds: Int,
    val kills: Int,
    val score: Int,
    val levelReached: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val isUnlocked: Boolean = false,
    val currentProgress: Int = 0,
    val unlockedTimestamp: Long? = null
)

@Entity(tableName = "clinic_upgrades")
data class ClinicUpgradeEntity(
    @PrimaryKey
    val id: String,
    val level: Int = 0
)

@Entity(tableName = "unlocked_characters")
data class UnlockedCharacterEntity(
    @PrimaryKey
    val characterId: String,
    val isUnlocked: Boolean = false
)

@Entity(tableName = "player_meta")
data class PlayerMetaEntity(
    @PrimaryKey
    val id: Int = 1,
    val dentalCoins: Int = 0,
    val totalGermsEliminated: Int = 0,
    val totalTimePlayedSeconds: Long = 0,
    val totalLootCollected: Int = 0,
    val bossesDefeated: Int = 0,
    val highestStageUnlocked: Int = 1
)

@Entity(tableName = "discovered_items")
data class DiscoveredItemEntity(
    @PrimaryKey
    val itemId: String,
    val isDiscovered: Boolean = true,
    val timesUsed: Int = 1,
    val discoveredTimestamp: Long = System.currentTimeMillis()
)
