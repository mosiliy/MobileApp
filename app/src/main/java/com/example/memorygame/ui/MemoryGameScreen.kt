package com.example.memorygame.ui

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import android.media.MediaPlayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.memorygame.data.GamePreferences
import com.example.memorygame.model.Card
import com.example.memorygame.viewmodel.MemoryGameViewModel
import kotlinx.coroutines.launch

import com.example.memorygame.util.SoundPlayer
import com.example.memorygame.R


// Difficulty options
enum class Difficulty(val gridSize: Int, val numberOfPairs: Int) {
    EASY(2, 2),
    MEDIUM(4, 8),
    HARD(6, 18)
}

@Composable
fun MemoryGameScreen(
    viewModel: MemoryGameViewModel,
    navController: NavController,
    mediaPlayer: MediaPlayer,
    gamePreferences: GamePreferences? = null
) {
    val cards by viewModel.cards.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val soundEnabled by gamePreferences?.soundEnabled?.collectAsState(initial = true) ?: remember { mutableStateOf(true) }
    val musicEnabled by gamePreferences?.musicEnabled?.collectAsState(initial = true) ?: remember { mutableStateOf(true) }
    val soundPlayer = remember { SoundPlayer(context) }

    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var showGameOverDialog by remember { mutableStateOf(false) }
    var currentDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var hasShownGameOverToast by remember { mutableStateOf(false) }
    var navigateHome by remember { mutableStateOf(false) }
    var lastMatchedCount by remember { mutableStateOf(0) }



    // Determine difficulty
    val currentPairs = cards.size / 2
    LaunchedEffect(currentPairs) {
        currentDifficulty = when (currentPairs) {
            2 -> Difficulty.EASY
            8 -> Difficulty.MEDIUM
            18 -> Difficulty.HARD
            else -> Difficulty.MEDIUM
        }
    }

    // Lifecycle observer to pause when app goes to background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) viewModel.handleAppBackgrounded()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val gridColumns = remember(currentDifficulty) { GridCells.Fixed(currentDifficulty.gridSize) }
    val formattedTime = remember(elapsedTime) {
        val minutes = elapsedTime / 60
        val seconds = elapsedTime % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        // Top bar с таймером
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 3.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding() // фон поднимается до статус-бара
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Memory Game",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.togglePause() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            PauseIcon(
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                // Таймер на том же фоне, под Row
                Text(
                    text = "Time: $formattedTime",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }




        val firstSelectedIndex by viewModel.firstSelectedIndex.collectAsState()
        LazyVerticalGrid(
            columns = gridColumns,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(cards.size) { index ->
                val card = cards[index]
                CardView(
                    card = card,
                    isSelected = firstSelectedIndex == index,
                    onClick = {
                        if (soundEnabled) soundPlayer.play(R.raw.card_flip)
                        viewModel.onCardClicked(index)
                    }
                )
            }
        }
    }

    // Difficulty selection dialog
    if (showDifficultyDialog) {
        DifficultySelectionDialog(
            currentDifficulty = currentDifficulty,
            onDifficultySelected = { difficulty ->
                currentDifficulty = difficulty
                viewModel.resetGame(difficulty.numberOfPairs)
                showDifficultyDialog = false
            },
            onDismiss = { showDifficultyDialog = false }
        )
    }

    // Pause dialog
    LaunchedEffect(isPaused) {
        showPauseDialog = isPaused
    }

    // Sounds
    LaunchedEffect(Unit) {
        soundPlayer.load(R.raw.card_flip) // убедись, что файл есть в res/raw/
        soundPlayer.load(R.raw.card_match)
        soundPlayer.load(R.raw.card_mismatch)
    }



    if (showPauseDialog && !navigateHome) {
        PauseDialog(
            onResume = {
                viewModel.togglePause()
                showPauseDialog = false
            },
            onRestart = {
                viewModel.restartGame()
                showPauseDialog = false
            },
            onHome = {
                showPauseDialog = false
                navigateHome = true
            },
            onDismiss = {
                viewModel.togglePause()
                showPauseDialog = false
            }
        )
    }




    LaunchedEffect(navigateHome) {
        if (navigateHome) {
            navController.popBackStack()
            navigateHome = false
        }
    }


    LaunchedEffect(cards) {
        val matchedCount = cards.count { it.isMatched }
        if (matchedCount > lastMatchedCount) {
            if (soundEnabled) {
                soundPlayer.play(R.raw.card_match)
            }
        }
        lastMatchedCount = matchedCount
    }

    LaunchedEffect(Unit) {
        viewModel.cardMatchEvent.collect { matched ->
            if (soundEnabled) {
                if (matched) soundPlayer.play(R.raw.card_match)
                else soundPlayer.play(R.raw.card_mismatch)
            }
        }
    }




    // Game over check
    LaunchedEffect(cards, elapsedTime) {
        if (viewModel.checkGameOver() && !hasShownGameOverToast) {
            Toast.makeText(context, "Game Over!", Toast.LENGTH_SHORT).show()
            hasShownGameOverToast = true
            gamePreferences?.let { prefs ->
                val difficultyName = when (currentDifficulty) {
                    Difficulty.EASY -> "EASY"
                    Difficulty.MEDIUM -> "MEDIUM"
                    Difficulty.HARD -> "HARD"
                }
                scope.launch { prefs.updateBestTime(difficultyName, elapsedTime) }
            }
            showGameOverDialog = true
        } else if (!viewModel.checkGameOver()) {
            hasShownGameOverToast = false
        }
    }

    if (showGameOverDialog && viewModel.checkGameOver()) {
        GameOverDialog(
            elapsedTime = elapsedTime,
            onRestart = {
                viewModel.restartGame()
                showGameOverDialog = false
                hasShownGameOverToast = false
            },
            onDismiss = {
                showGameOverDialog = false
                navController.popBackStack()
            }
        )
    }
}

@Composable
fun DifficultySelectionDialog(
    currentDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Difficulty") },
        text = {
            Column {
                Difficulty.values().forEach { difficulty ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDifficultySelected(difficulty) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = difficulty == currentDifficulty,
                            onClick = { onDifficultySelected(difficulty) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${difficulty.gridSize}x${difficulty.gridSize} (${difficulty.numberOfPairs} pairs)",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun GameOverDialog(
    elapsedTime: Long,
    onRestart: () -> Unit,
    onDismiss: () -> Unit
) {
    val minutes = elapsedTime / 60
    val seconds = elapsedTime % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Game Over!") },
        text = {
            Column {
                Text("Congratulations! You completed the game.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Time: $formattedTime")
            }
        },
        confirmButton = {
            TextButton(onClick = onRestart) { Text("Play Again") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}



@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Game Paused") },
        text = { Text("The game is paused. Choose an option to continue.") },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onResume) { Text("Resume") }
                TextButton(onClick = onRestart) { Text("Restart") }
                TextButton(onClick = onHome) { Text("Home") }
            }
        },
        dismissButton = { /* можно оставить пустым */ }
    )
}

@Composable
fun PauseIcon(modifier: Modifier = Modifier, tint: Color = Color.Unspecified) {
    Row(
        modifier = modifier.size(24.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(tint)
        )
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(tint)
        )
    }
}

@Composable
fun CardView(
    card: Card,
    isSelected: Boolean,
    soundEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val rotationAnim by animateFloatAsState(
        targetValue = if (card.isFaceUp || card.isMatched) 180f else 0f,
        animationSpec = tween(400),
        label = "cardFlip"
    )

    val density = LocalDensity.current.density
    val cardAlpha = if (card.isMatched) 0.7f else 1f
    val isFrontVisible = rotationAnim > 90f

    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(60.dp)
            .graphicsLayer {
                rotationY = rotationAnim
                cameraDistance = 8f * density
                alpha = cardAlpha
            }
            .clickable(enabled = !card.isMatched) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (!isFrontVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray, RoundedCornerShape(8.dp)),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = 180f
                    }
                    .background(
                        if (card.isMatched) Color.Green else Color.Cyan,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.imageId.toString(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

