package com.example

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.CharacterSelectScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.MatchEndScreen
import com.example.ui.screens.RankingScreen
import com.example.ui.screens.StageSelectScreen
import com.example.ui.screens.UpgradesScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.Screen

import androidx.compose.foundation.layout.Box
import com.example.ui.components.NotificationPopupOverlay

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (viewModel.gamepadHandler.handleMotionEvent(event)) {
            return true
        }
        return super.dispatchGenericMotionEvent(event)
    }

    @android.annotation.SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (viewModel.gamepadHandler.handleKeyEvent(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}

@Composable
fun AppNavigation(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activePopup by viewModel.activePopup.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val screen = currentScreen) {
            is Screen.Splash -> SplashScreen(viewModel = viewModel)
            is Screen.Menu -> MainMenuScreen(viewModel = viewModel)
            is Screen.CharacterSelect -> CharacterSelectScreen(viewModel = viewModel)
            is Screen.StageSelect -> StageSelectScreen(viewModel = viewModel)
            is Screen.Gameplay -> GameScreen(viewModel = viewModel)
            is Screen.GameOver -> MatchEndScreen(
                isVictory = false,
                kills = screen.kills,
                timeSec = screen.timeSec,
                coins = screen.coins,
                level = screen.level,
                viewModel = viewModel
            )
            is Screen.Victory -> MatchEndScreen(
                isVictory = true,
                kills = screen.kills,
                timeSec = screen.timeSec,
                coins = screen.coins,
                level = screen.level,
                viewModel = viewModel
            )
            is Screen.Rankings -> RankingScreen(viewModel = viewModel)
            is Screen.Achievements -> AchievementsScreen(viewModel = viewModel)
            is Screen.Upgrades -> UpgradesScreen(viewModel = viewModel)
            is Screen.Library -> LibraryScreen(viewModel = viewModel)
        }

        activePopup?.let { popup ->
            NotificationPopupOverlay(
                popup = popup,
                onDismiss = { viewModel.dismissPopup() }
            )
        }
    }
}
