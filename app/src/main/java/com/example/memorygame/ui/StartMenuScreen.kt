package com.example.memorygame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.media.MediaPlayer
import android.content.Intent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import com.example.memorygame.data.GamePreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.compose.ui.platform.LocalContext


@Composable
fun StartMenuScreen(
    navController: NavController,
    mediaPlayer: MediaPlayer,
    gamePreferences: GamePreferences
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var numberOfPairs by remember { mutableStateOf(8) }
    
    // Load saved preferences
    LaunchedEffect(Unit) {
        numberOfPairs = gamePreferences.numberOfPairs.first()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Memory Game",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 64.dp)
        )
        
        // Play Button
        Button(
            onClick = {
                navController.navigate("game/$numberOfPairs")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Play",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Settings Button
        Button(
            onClick = {
                showSettingsDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // My Records Button
        Button(
            onClick = {
                navController.navigate("records")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text(
                text = "My Records",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    
    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            gamePreferences = gamePreferences,
            currentNumberOfPairs = numberOfPairs,
            onDismiss = { showSettingsDialog = false },
            onNumberOfPairsChanged = { pairs ->
                numberOfPairs = pairs
                scope.launch {
                    gamePreferences.setNumberOfPairs(pairs)
                }
            },
            mediaPlayer = mediaPlayer
        )
    }
}




@Composable
fun SettingsDialog(
    gamePreferences: GamePreferences,
    currentNumberOfPairs: Int,
    onDismiss: () -> Unit,
    onNumberOfPairsChanged: (Int) -> Unit,
    mediaPlayer: MediaPlayer
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Инициализация состояний
    var soundEnabled by remember { mutableStateOf<Boolean?>(null) }
    var musicEnabled by remember { mutableStateOf<Boolean?>(null) }
    var selectedTheme by remember { mutableStateOf("System") }
    var selectedPairs by remember { mutableStateOf(currentNumberOfPairs) }

    val themes = listOf("System", "Light", "Dark")
    val pairOptions = listOf(2, 8, 18) // EASY, MEDIUM, HARD

    val telegramUsername = "Mosiliy" // <-- замените на свой ник

    // Загрузка текущих настроек из GamePreferences
    LaunchedEffect(Unit) {
        soundEnabled = gamePreferences.soundEnabled.first()
        musicEnabled = gamePreferences.musicEnabled.first()
        selectedTheme = gamePreferences.theme.first()
    }



    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Выбор количества пар
                Text("Difficulty:", style = MaterialTheme.typography.titleMedium)
                val maxVisibleItems = 3
                val itemHeight = 56.dp
                val maxHeight = (maxVisibleItems * itemHeight.value).dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeight)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        pairOptions.forEach { pairs ->
                            item {
                                val difficulty = when (pairs) {
                                    2 -> "Easy (2x2)"
                                    8 -> "Medium (4x4)"
                                    18 -> "Hard (6x6)"
                                    else -> "$pairs pairs"
                                }
                                FilterChip(
                                    selected = selectedPairs == pairs,
                                    onClick = {
                                        selectedPairs = pairs
                                        onNumberOfPairsChanged(pairs)
                                    },
                                    label = { Text(difficulty) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Переключатель звука
                if (soundEnabled != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sound Effects")
                        Switch(
                            checked = soundEnabled!!,
                            onCheckedChange = { enabled ->
                                soundEnabled = enabled
                                scope.launch { gamePreferences.setSoundEnabled(enabled) }
                            }
                        )
                    }
                }

                // Переключатель музыки
                if (musicEnabled != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Background Music")
                        Switch(
                            checked = musicEnabled!!,
                            onCheckedChange = { enabled ->
                                musicEnabled = enabled
                                scope.launch { gamePreferences.setMusicEnabled(enabled) }
                            }
                        )
                    }
                }

                // Кнопка обратной связи через Telegram
                Button(
                    onClick = {
                        val telegramUrl = "https://t.me/$telegramUsername"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Feedback / Contact me")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}




