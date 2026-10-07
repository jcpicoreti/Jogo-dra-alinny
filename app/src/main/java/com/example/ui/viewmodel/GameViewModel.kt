package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.local.ClinicUpgradeEntity
import com.example.data.local.PlayerMetaEntity
import com.example.data.local.ScoreEntity
import com.example.data.local.UnlockedCharacterEntity
import com.example.data.model.CharacterId
import com.example.data.model.ClinicUpgradeType
import com.example.data.model.GameState
import com.example.data.model.LevelUpOption
import com.example.data.model.StageId
import com.example.data.repository.GameRepository
import com.example.game.GameEngine
import com.example.game.GamepadHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Menu : Screen()
    object CharacterSelect : Screen()
    object StageSelect : Screen()
    object Gameplay : Screen()
    data class GameOver(val kills: Int, val timeSec: Int, val coins: Int, val level: Int) : Screen()
    data class Victory(val kills: Int, val timeSec: Int, val coins: Int, val level: Int) : Screen()
    object Rankings : Screen()
    object Achievements : Screen()
    object Upgrades : Screen()
    object Library : Screen()
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val repository = GameRepository(application)
    val soundManager = SoundManager(application)
    val gamepadHandler = GamepadHandler()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedCharacter = MutableStateFlow(CharacterId.DRA_ALINNY)
    val selectedCharacter: StateFlow<CharacterId> = _selectedCharacter.asStateFlow()

    private val _selectedStage = MutableStateFlow(StageId.STAGE_1_ESMALTE)
    val selectedStage: StateFlow<StageId> = _selectedStage.asStateFlow()

    // Persistent data flows
    val highScores = repository.getHighScores()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val achievements = repository.getAllAchievements()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val clinicUpgrades = repository.getClinicUpgrades()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val unlockedCharacters = repository.getUnlockedCharacters()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val playerMeta = repository.getPlayerMeta()
        .stateIn(viewModelScope, SharingStarted.Lazily, PlayerMetaEntity())

    val discoveredItems = repository.getAllDiscoveredItems()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Active Game Engine instance
    var gameEngine: GameEngine? = null
        private set

    private val _activeGameState = MutableStateFlow<GameState?>(null)
    val activeGameState: StateFlow<GameState?> = _activeGameState.asStateFlow()

    private var gameLoopJob: Job? = null

    init {
        // Gamepad bindings
        gamepadHandler.onButtonAPressed = {
            val state = gameEngine?.gameState?.value
            if (state?.isLevelingUp == true && state.levelUpChoices.isNotEmpty()) {
                selectLevelUpChoice(state.levelUpChoices.first())
            } else if (state?.isOpeningChest == true) {
                dismissChestReward()
            }
        }
        gamepadHandler.onButtonBPressed = {
            gameEngine?.togglePause()
        }
        gamepadHandler.onButtonStartPressed = {
            gameEngine?.togglePause()
        }
    }

    fun navigateTo(screen: Screen) {
        if (screen != Screen.Gameplay) {
            stopGameLoop()
        }
        _currentScreen.value = screen
    }

    fun selectCharacter(characterId: CharacterId) {
        _selectedCharacter.value = characterId
    }

    fun isStageUnlocked(stageId: StageId): Boolean {
        val highest = playerMeta.value?.highestStageUnlocked ?: 1
        return stageId.stageNumber <= highest
    }

    fun selectStage(stageId: StageId) {
        if (isStageUnlocked(stageId)) {
            _selectedStage.value = stageId
        }
    }

    fun startMatch(characterId: CharacterId = _selectedCharacter.value, stageId: StageId = _selectedStage.value) {
        _selectedCharacter.value = characterId
        _selectedStage.value = stageId

        val upgradesList = clinicUpgrades.value
        val highestUnlocked = playerMeta.value?.highestStageUnlocked ?: 1
        val engine = GameEngine(characterId, stageId, soundManager, upgradesList, highestUnlocked)
        gameEngine = engine
        _activeGameState.value = engine.gameState.value

        engine.onMatchComplete = { kills, timeSec, lootCount, bossesCount, coinsEarned ->
            val isVictorious = engine.gameState.value.isVictory
            val currentLevel = engine.gameState.value.level
            viewModelScope.launch(Dispatchers.Main) {
                stopGameLoop()
                if (isVictorious) {
                    _currentScreen.value = Screen.Victory(kills, timeSec, coinsEarned, currentLevel)
                } else {
                    _currentScreen.value = Screen.GameOver(kills, timeSec, coinsEarned, currentLevel)
                }
            }
            viewModelScope.launch(Dispatchers.IO) {
                repository.recordMatchStats(
                    germsKilled = kills,
                    timeSeconds = timeSec,
                    lootCollected = lootCount,
                    bossesKilled = bossesCount,
                    coinsEarned = coinsEarned,
                    isVictory = isVictorious,
                    stage = stageId
                )
            }
        }

        engine.onAchievementTriggered = { achId ->
            viewModelScope.launch(Dispatchers.IO) {
                repository.unlockAchievement(achId)
            }
        }

        engine.onItemDiscovered = { itemId ->
            viewModelScope.launch(Dispatchers.IO) {
                repository.markItemDiscovered(itemId)
            }
        }

        // Mark character's starting weapon as discovered
        viewModelScope.launch(Dispatchers.IO) {
            repository.markItemDiscovered(characterId.startingWeapon.name)
        }

        _currentScreen.value = Screen.Gameplay
        startGameLoop(engine)
    }

    private fun startGameLoop(engine: GameEngine) {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastTimeNanos = System.nanoTime()

            while (isActive) {
                val nowNanos = System.nanoTime()
                val deltaSeconds = (nowNanos - lastTimeNanos) / 1_000_000_000f
                lastTimeNanos = nowNanos

                // Poll Gamepad stick & dpad
                if (gamepadHandler.moveX != 0f || gamepadHandler.moveY != 0f) {
                    engine.setMoveDirection(gamepadHandler.moveX, gamepadHandler.moveY)
                }

                engine.update(deltaSeconds)
                _activeGameState.value = engine.gameState.value

                delay(16) // ~60 FPS
            }
        }
    }

    fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    fun setVirtualJoystickDirection(x: Float, y: Float) {
        gameEngine?.setMoveDirection(x, y)
    }

    fun selectLevelUpChoice(option: LevelUpOption) {
        gameEngine?.selectLevelUpChoice(option)
    }

    fun dismissChestReward() {
        gameEngine?.dismissChest()
    }

    fun togglePause() {
        gameEngine?.togglePause()
    }

    fun saveMatchScore(playerName: String, kills: Int, timeSec: Int, level: Int) {
        val score = kills * 10 + timeSec * 5 + level * 50
        val entry = ScoreEntity(
            playerName = playerName.ifBlank { "Dr. Sorriso" },
            characterId = _selectedCharacter.value.name,
            stageId = _selectedStage.value.title,
            timeSurvivedSeconds = timeSec,
            kills = kills,
            score = score,
            levelReached = level
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveHighScore(entry)
        }
    }

    fun purchaseClinicUpgrade(upgradeType: ClinicUpgradeType) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.purchaseUpgrade(upgradeType)
            if (success) {
                soundManager.playChestOpen()
            }
        }
    }

    fun isCharacterUnlocked(charId: CharacterId): Boolean {
        if (charId == CharacterId.DRA_ALINNY) return true
        return unlockedCharacters.value.any { it.characterId == charId.name && it.isUnlocked }
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
        soundManager.release()
    }
}
