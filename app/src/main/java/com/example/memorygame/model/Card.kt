/**
 * Represents a single card in the memory game.
 *
 * @param id        Unique identifier for this card instance (each physical card is unique).
 * @param imageId   Identifier for the card's image; matching pairs share the same imageId.
 * @param isFaceUp  Whether the card is currently face up.
 * @param isMatched Whether the card has been successfully matched and removed from play.
 */

package com.example.memorygame.model
data class Card(
    val id: Int,
    val imageId: Int,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)