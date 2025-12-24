package com.example.memorygame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.memorygame.data.GamePreferences
import kotlinx.coroutines.flow.first
import androidx.compose.foundation.layout.statusBarsPadding
import android.media.MediaPlayer


@Composable
fun MyRecordsScreen(
    navController: NavController,
    mediaPlayer: MediaPlayer,
    gamePreferences: GamePreferences
) {
    var bestTimeEasy by remember { mutableStateOf<Long?>(null) }
    var bestTimeMedium by remember { mutableStateOf<Long?>(null) }
    var bestTimeHard by remember { mutableStateOf<Long?>(null) }
    
    // Load records
    LaunchedEffect(Unit) {
        bestTimeEasy = gamePreferences.bestTimeEasy.first()
        bestTimeMedium = gamePreferences.bestTimeMedium.first()
        bestTimeHard = gamePreferences.bestTimeHard.first()
    }
    
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding() // 🔥 ВАЖНО
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "My Records",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Best Times",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
            )
            
            // Easy Difficulty Record
            RecordCard(
                difficulty = "Easy (2x2)",
                time = bestTimeEasy
            )
            
            // Medium Difficulty Record
            RecordCard(
                difficulty = "Medium (4x4)",
                time = bestTimeMedium
            )
            
            // Hard Difficulty Record
            RecordCard(
                difficulty = "Hard (6x6)",
                time = bestTimeHard
            )
        }
    }
}

@Composable
fun RecordCard(
    difficulty: String,
    time: Long?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = difficulty,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (time != null) {
                val minutes = time / 60
                val seconds = time % 60
                val formattedTime = String.format("%02d:%02d", minutes, seconds)
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "No record yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

