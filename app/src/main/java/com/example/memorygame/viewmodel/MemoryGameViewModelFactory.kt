package com.example.memorygame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MemoryGameViewModelFactory(private val numberOfPairs: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoryGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MemoryGameViewModel(numberOfPairs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
