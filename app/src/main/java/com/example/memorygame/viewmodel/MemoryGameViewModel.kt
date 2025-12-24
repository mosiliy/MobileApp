package com.example.memorygame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorygame.model.Card
import com.example.memorygame.model.MemoryGame
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MemoryGameViewModel(
    numberOfPairs: Int
) : ViewModel() {

    private var game = MemoryGame(numberOfPairs)
    private val _cards = MutableStateFlow(game.cards)
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    // Timer state
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _cardMatchEvent = MutableSharedFlow<Boolean>() // true = match, false = mismatch
    val cardMatchEvent: SharedFlow<Boolean> = _cardMatchEvent.asSharedFlow()


    val gridSize: Int
        get() = when (cards.value.size / 2) {
            2 -> 2 // EASY
            8 -> 4 // MEDIUM
            18 -> 6 // HARD
            else -> 4
        }

    val difficultyName: String
        get() = when (cards.value.size / 2) {
            2 -> "EASY"
            8 -> "MEDIUM"
            18 -> "HARD"
            else -> "MEDIUM"
        }


    // Pause state


    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused
    
    // Selected card index state
    private val _firstSelectedIndex = MutableStateFlow<Int?>(null)
    val firstSelectedIndex: StateFlow<Int?> = _firstSelectedIndex.asStateFlow()
    
    private var timerJob: Job? = null

    // === ЭТИ ПЕРЕМЕННЫЕ ОБЯЗАТЕЛЬНЫ ===
    private var isProcessingTurn: Boolean = false

    init {
        // Start the timer when ViewModel is created
        startTimer()
    }

    override fun onCleared() {
        super.onCleared()
        // Stop timer when ViewModel is cleared
        stopTimer()
    }

    /**
     * Resets the game with a new number of pairs.
     * Clears all previous game state and creates a new MemoryGame instance.
     */
    fun resetGame(numberOfPairs: Int) {
        // Cancel any ongoing turn processing
        isProcessingTurn = false
        _firstSelectedIndex.value = null
        
        // Stop and reset timer
        stopTimer()
        _elapsedTime.value = 0L
        _isPaused.value = false
        
        // Create a new game instance with the new number of pairs
        val newGame = MemoryGame(numberOfPairs)
        // Update the internal game reference
        game = newGame
        // Immediately update the UI with the new cards
        _cards.value = newGame.cards
        
        // Start the timer for the new game
        startTimer()
    }
    
    /**
     * Resets the current game with the same number of pairs.
     * Useful for restarting the game without changing difficulty.
     */
    fun restartGame() {
        val currentNumberOfPairs = game.cards.size / 2
        resetGame(currentNumberOfPairs)
    }
    
    /**
     * Starts the timer that increments elapsedTime every second.
     * Timer only runs when the game is not paused and not finished.
     */
    fun startTimer() {
        stopTimer() // на всякий случай

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)

                if (game.isGameOver()) {
                    break
                }

                if (!_isPaused.value) {
                    _elapsedTime.value += 1L
                }
            }
        }
    }


    /**
     * Stops the timer.
     */
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
    
    /**
     * Toggles the pause state of the game.
     */
    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }

    fun setPaused(paused: Boolean) {
        _isPaused.value = paused
    }
    
    /**
     * Pauses the timer.
     */
    fun pauseTimer() {
        _isPaused.value = true
    }
    
    /**
     * Resumes the timer.
     */
    fun resumeTimer() {
        _isPaused.value = false
    }
    
    /**
     * Handles when the app goes to the background.
     * Automatically pauses the game and stops the timer.
     */
    fun handleAppBackgrounded() {
        // Only pause if not already paused and game is not finished
        if (!_isPaused.value && !game.isGameOver()) {
            _isPaused.value = true
        }
    }
    
    /**
     * Checks if the game is over (all cards are matched).
     * @return true if all cards are matched, false otherwise.
     */
    fun checkGameOver(): Boolean {
        return game.isGameOver()
    }

    fun onCardClicked(index: Int) {
        if (_isPaused.value) return
        if (isProcessingTurn) return

        val currentCards = _cards.value
        if (index !in currentCards.indices) return
        val clickedCard = currentCards[index]
        if (clickedCard.isMatched || clickedCard.isFaceUp) return

        val firstIndex = _firstSelectedIndex.value
        if (firstIndex == null) {
            // Первый клик — просто перевернуть карту
            val updatedCards = currentCards.toMutableList()
            updatedCards[index] = clickedCard.copy(isFaceUp = true)
            _cards.value = updatedCards
            _firstSelectedIndex.value = index
            return
        }

        if (index == firstIndex) return

        // Второй клик — перевернуть карту и начать проверку
        isProcessingTurn = true
        val updatedCards = currentCards.toMutableList()
        updatedCards[index] = clickedCard.copy(isFaceUp = true)
        _cards.value = updatedCards

        viewModelScope.launch {
            delay(1000L)

            // Логика игры обновляет состояние карт
            game.selectCard(firstIndex)
            game.selectCard(index)
            game.resetUnmatchedCards()
            _cards.value = game.cards
            _firstSelectedIndex.value = null
            isProcessingTurn = false

            // Проверяем совпадение
            val matched = game.cards[firstIndex].isMatched || game.cards[index].isMatched
            _cardMatchEvent.emit(matched)
        }
    }

}

