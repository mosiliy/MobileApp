package com.example.memorygame

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memorygame.data.GamePreferences
import com.example.memorygame.ui.MemoryGameScreen
import com.example.memorygame.ui.MyRecordsScreen
import com.example.memorygame.ui.StartMenuScreen
import com.example.memorygame.ui.MusicController
import com.example.memorygame.ui.theme.MemoryGameTheme
import com.example.memorygame.viewmodel.MemoryGameViewModel
import com.example.memorygame.viewmodel.MemoryGameViewModelFactory
import android.media.MediaPlayer



class MainActivity : ComponentActivity() {

    private lateinit var gamePreferences: GamePreferences
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gamePreferences = GamePreferences(this)

        mediaPlayer = MediaPlayer.create(this, R.raw.background).apply {
            isLooping = true
            setVolume(0.5f, 0.5f) // регулировка громкости
        }

        setContent {
            MemoryGameTheme {
                MusicController(
                    gamePreferences = gamePreferences,
                    mediaPlayer = mediaPlayer
                )
                val navController = rememberNavController()
                MemoryGameApp(
                    gamePreferences = gamePreferences,
                    mediaPlayer = mediaPlayer
                )
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release() // освобождаем ресурсы
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            val musicEnabled = gamePreferences.musicEnabled.first()
            if (musicEnabled && !mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }



}



@Composable
fun MemoryGameApp(
    gamePreferences: GamePreferences,
    mediaPlayer: MediaPlayer
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "start"
    ) {
        composable("start") {
            StartMenuScreen(
                navController = navController,
                gamePreferences = gamePreferences,
                mediaPlayer = mediaPlayer
            )
        }

        composable(
            route = "game/{numberOfPairs}",
            arguments = listOf(navArgument("numberOfPairs") { type = NavType.IntType })
        ) { backStackEntry ->
            val numberOfPairs = backStackEntry.arguments?.getInt("numberOfPairs") ?: 8
            val viewModel: MemoryGameViewModel = viewModel(
                factory = MemoryGameViewModelFactory(numberOfPairs)
            )
            MemoryGameScreen(
                viewModel = viewModel,
                navController = navController,
                gamePreferences = gamePreferences,
                mediaPlayer = mediaPlayer
            )
        }

        composable("records") {
            MyRecordsScreen(
                navController = navController,
                gamePreferences = gamePreferences,
                mediaPlayer = mediaPlayer
            )
        }
    }
}
