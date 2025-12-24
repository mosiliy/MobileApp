package com.example.memorygame.ui

import android.media.MediaPlayer
import androidx.compose.runtime.*
import com.example.memorygame.data.GamePreferences
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MusicController(
    gamePreferences: GamePreferences,
    mediaPlayer: MediaPlayer
) {
    LaunchedEffect(Unit) {
        gamePreferences.musicEnabled.collectLatest { enabled ->
            if (enabled) {
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
            } else {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.pause()
        }
    }
}
