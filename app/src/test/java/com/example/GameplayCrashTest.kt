package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.SoundManager
import com.example.data.local.AppDatabase
import com.example.data.local.ClinicUpgradeEntity
import com.example.data.model.CharacterId
import com.example.data.model.ClinicUpgradeType
import com.example.data.model.LootInstance
import com.example.data.model.LootType
import com.example.data.model.StageId
import com.example.data.repository.GameRepository
import com.example.game.GameEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GameplayCrashTest {

    @Test
    fun testSoundManagerDoesNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val soundManager = SoundManager(context)
        soundManager.playBrush()
        soundManager.playFloss()
        soundManager.playLaser()
        soundManager.playDrill()
        soundManager.playSplash()
        soundManager.playWaterJet()
        soundManager.playHit()
        soundManager.playGemPickup(0)
        soundManager.playGemPickup(5)
        soundManager.playLevelUp()
        soundManager.playBossRoar()
        soundManager.playChestOpen()
        soundManager.playGameOver()
    }

    @Test
    fun testGameEngineSimulationLoop() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val soundManager = SoundManager(context)
        val upgrades = listOf(
            ClinicUpgradeEntity(ClinicUpgradeType.VITALIDADE_ESMALTE.id, 2),
            ClinicUpgradeEntity(ClinicUpgradeType.POTENCIA_CLINICA.id, 1)
        )

        val engine = GameEngine(
            characterId = CharacterId.DRA_ALINNY,
            stageId = StageId.STAGE_1_ESMALTE,
            soundManager = soundManager,
            clinicUpgrades = upgrades
        )

        // Run 300 ticks of game updates
        for (i in 0 until 300) {
            engine.setMoveDirection(1.0f, 0.5f)
            engine.update(0.016f)

            // Concurrently take snapshots
            val snapshot = engine.getRenderSnapshot()
            assertNotNull(snapshot)
        }

        // Test Level Up choice generation
        val choices = engine.generateLevelUpChoices()
        assertTrue(choices.isNotEmpty())
        engine.selectLevelUpChoice(choices.first())

        // Test Golden chest
        synchronized(engine.stateLock) {
            engine.lootItems.add(
                LootInstance(id = 9999, x = engine.gameState.value.playerX, y = engine.gameState.value.playerY, type = LootType.BAU_DOURADO)
            )
        }
        engine.update(0.016f)
        engine.dismissChest()

        // Test All characters and stages initialization
        for (char in CharacterId.values()) {
            for (stg in StageId.values()) {
                val eng = GameEngine(char, stg, soundManager, emptyList())
                eng.update(0.016f)
            }
        }
    }

    @Test
    fun testRepositoryAndDatabase() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        assertNotNull(db)

        val repo = GameRepository(context)
        val meta = repo.getPlayerMeta().first()
        // verify no crash
        repo.recordMatchStats(10, 60, 5, 1, 50)
    }

    @Test
    fun testMainActivityLaunches() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
        assertNotNull(controller.get())
    }
}
