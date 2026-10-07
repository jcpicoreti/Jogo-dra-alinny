package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ScoreEntity::class,
        AchievementEntity::class,
        ClinicUpgradeEntity::class,
        UnlockedCharacterEntity::class,
        PlayerMetaEntity::class,
        DiscoveredItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
    abstract fun achievementDao(): AchievementDao
    abstract fun clinicUpgradeDao(): ClinicUpgradeDao
    abstract fun characterDao(): CharacterDao
    abstract fun playerMetaDao(): PlayerMetaDao
    abstract fun discoveredItemDao(): DiscoveredItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dra_alinny_game_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
