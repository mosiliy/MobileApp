/**
 * Core game logic for a simple "Find the Pair" memory game.
 *
 * This class is UI-agnostic and contains no Android/Compose dependencies.
 */

package com.example.memorygame.model

class MemoryGame(
    private val numberOfPairs: Int
) {

    // Internal mutable list; external code gets read-only access via the `cards` property.
    private val _cards: MutableList<Card>

    /**
     * Public, read-only view of the current cards.
     */
    val cards: List<Card>
        get() = _cards

    // Index of the one and only face-up, non-matched card waiting for a potential pair.
    // If null, no card is currently selected (or we just resolved a pair).
    private var indexOfSingleSelectedCard: Int? = null

    init {
        require(numberOfPairs > 0) { "numberOfPairs must be greater than 0" }

        // Create pairs of cards: for each imageId, create two Card instances.
        val generatedCards = mutableListOf<Card>()
        var currentId = 0
        for (imageId in 0 until numberOfPairs) {
            // First card of the pair
            generatedCards.add(
                Card(
                    id = currentId++,
                    imageId = imageId
                )
            )
            // Second card of the pair
            generatedCards.add(
                Card(
                    id = currentId++,
                    imageId = imageId
                )
            )
        }

        // Shuffle the cards to randomize their positions.
        generatedCards.shuffle()
        _cards = generatedCards
    }

    /**
     * Handles selecting a card at the given [index].
     *
     * Behavior:
     * - Ignores selection if the card is already face up or already matched.
     * - If no other card is currently selected, flips this card face up.
     * - If another card is already face up:
     *      - Flips this card face up.
     *      - Checks whether the two face-up cards form a pair.
     *      - If they match, both cards are marked as matched.
     *      - If they do not match, both cards remain face up; the caller is
     *        responsible for flipping them back down later (e.g. after a UI delay)
     *        via [resetUnmatchedCards].
     */
    fun selectCard(index: Int) {
        // Basic bounds check to avoid runtime crashes.
        if (index !in _cards.indices) return

        val selectedCard = _cards[index]

        // Ignore attempts to select a card that is already face up or already matched.
        if (selectedCard.isFaceUp || selectedCard.isMatched) {
            return
        }

        when (val previouslySelectedIndex = indexOfSingleSelectedCard) {
            null -> {
                // No other card is currently selected.
                // Flip this card face up and remember its index.
                _cards[index] = selectedCard.copy(isFaceUp = true)
                indexOfSingleSelectedCard = index
            }

            else -> {
                // There is exactly one previously selected (face-up, not matched) card.
                val previousCard = _cards[previouslySelectedIndex]

                // Flip the newly selected card face up.
                _cards[index] = selectedCard.copy(isFaceUp = true)

                // Check for a match based on imageId.
                if (previousCard.imageId == selectedCard.imageId) {
                    // Mark both cards as matched and keep them face up.
                    _cards[previouslySelectedIndex] =
                        previousCard.copy(isMatched = true, isFaceUp = true)
                    _cards[index] =
                        _cards[index].copy(isMatched = true, isFaceUp = true)
                }

                // After resolving the pair (match or not), clear the selection.
                indexOfSingleSelectedCard = null
            }
        }
    }

    /**
     * Flips back down all currently face-up cards that are **not** matched.
     *
     * This is intended to be called by higher-level code (e.g., a ViewModel)
     * after a UI-visible delay so that the player can briefly see the
     * non-matching pair before they are turned face down again.
     *
     * Matched cards remain face up.
     */
    fun resetUnmatchedCards() {
        for (i in _cards.indices) {
            val card = _cards[i]
            if (card.isFaceUp && !card.isMatched) {
                _cards[i] = card.copy(isFaceUp = false)
            }
        }
    }

    /**
     * Utility function to check whether the game is finished.
     *
     * @return true if all cards are matched.
     */
    fun isGameOver(): Boolean {
        return _cards.all { it.isMatched }
    }
}