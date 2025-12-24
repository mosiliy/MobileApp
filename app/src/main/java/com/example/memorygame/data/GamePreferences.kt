package com.example.memorygame.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Теперь DataStore не private, чтобы можно было использовать внутри класса
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_preferences")

class GamePreferences(private val context: Context) {

    companion object {
        // Все ключи остаются private — UI их не видит напрямую
        private val KEY_NUMBER_OF_PAIRS = intPreferencesKey("number_of_pairs")
        private val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val KEY_MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        private val KEY_THEME = stringPreferencesKey("theme")

        private val KEY_BEST_TIME_EASY = longPreferencesKey("best_time_easy")
        private val KEY_BEST_TIME_MEDIUM = longPreferencesKey("best_time_medium")
        private val KEY_BEST_TIME_HARD = longPreferencesKey("best_time_hard")
    }

    // Публичные Flow для использования в UI
    val numberOfPairs: Flow<Int> = context.dataStore.data.map { it[KEY_NUMBER_OF_PAIRS] ?: 8 }
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_SOUND_ENABLED] ?: true }
    val musicEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_MUSIC_ENABLED] ?: true }
    val theme: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "System" }

    // Сеттеры для обновления настроек
    suspend fun setNumberOfPairs(pairs: Int) {
        context.dataStore.edit { it[KEY_NUMBER_OF_PAIRS] = pairs }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SOUND_ENABLED] = enabled }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_MUSIC_ENABLED] = enabled }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    // Работа с рекордами
    suspend fun getBestTime(difficulty: String): Long? {
        val key = when (difficulty) {
            "EASY" -> KEY_BEST_TIME_EASY
            "MEDIUM" -> KEY_BEST_TIME_MEDIUM
            "HARD" -> KEY_BEST_TIME_HARD
            else -> return null
        }
        return context.dataStore.data.first()[key]
    }

    suspend fun updateBestTime(difficulty: String, time: Long) {
        val key = when (difficulty) {
            "EASY" -> KEY_BEST_TIME_EASY
            "MEDIUM" -> KEY_BEST_TIME_MEDIUM
            "HARD" -> KEY_BEST_TIME_HARD
            else -> return
        }
        val currentBest = getBestTime(difficulty)
        if (currentBest == null || time < currentBest) {
            context.dataStore.edit { it[key] = time }
        }
    }

    // Flow для рекордов (для наблюдения в UI)
    val bestTimeEasy: Flow<Long?> = context.dataStore.data.map { it[KEY_BEST_TIME_EASY] }
    val bestTimeMedium: Flow<Long?> = context.dataStore.data.map { it[KEY_BEST_TIME_MEDIUM] }
    val bestTimeHard: Flow<Long?> = context.dataStore.data.map { it[KEY_BEST_TIME_HARD] }
}
