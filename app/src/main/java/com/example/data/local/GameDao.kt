package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 50")
    fun getAllHighScores(): Flow<List<ScoreEntity>>

    @Query("SELECT * FROM high_scores WHERE stageId = :stageId ORDER BY score DESC LIMIT 20")
    fun getHighScoresForStage(stageId: String): Flow<List<ScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreEntity): Long
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id LIMIT 1")
    suspend fun getAchievement(id: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(achievements: List<AchievementEntity>)
}

@Dao
interface ClinicUpgradeDao {
    @Query("SELECT * FROM clinic_upgrades")
    fun getAllUpgrades(): Flow<List<ClinicUpgradeEntity>>

    @Query("SELECT * FROM clinic_upgrades WHERE id = :id LIMIT 1")
    suspend fun getUpgrade(id: String): ClinicUpgradeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setUpgradeLevel(upgrade: ClinicUpgradeEntity)
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM unlocked_characters")
    fun getAllUnlockedCharacters(): Flow<List<UnlockedCharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun unlockCharacter(unlocked: UnlockedCharacterEntity)
}

@Dao
interface PlayerMetaDao {
    @Query("SELECT * FROM player_meta WHERE id = 1 LIMIT 1")
    fun getPlayerMeta(): Flow<PlayerMetaEntity?>

    @Query("SELECT * FROM player_meta WHERE id = 1 LIMIT 1")
    suspend fun getPlayerMetaSync(): PlayerMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePlayerMeta(meta: PlayerMetaEntity)
}

@Dao
interface DiscoveredItemDao {
    @Query("SELECT * FROM discovered_items")
    fun getAllDiscoveredItems(): Flow<List<DiscoveredItemEntity>>

    @Query("SELECT * FROM discovered_items WHERE itemId = :id LIMIT 1")
    suspend fun getDiscoveredItem(id: String): DiscoveredItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markItemDiscovered(item: DiscoveredItemEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markInitialItemsDiscovered(items: List<DiscoveredItemEntity>)
}
