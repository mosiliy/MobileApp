package com.example.memorygame.util

import android.content.Context
import android.media.SoundPool
import androidx.annotation.RawRes

class SoundPlayer(private val context: Context) { // <- контекст как параметр
    private val soundPool = SoundPool.Builder().setMaxStreams(1).build()
    private val soundMap = mutableMapOf<Int, Int>()

    fun load(@RawRes resId: Int) {
        val soundId = soundPool.load(context, resId, 1)
        soundMap[resId] = soundId
    }

    fun play(@RawRes resId: Int) {
        soundMap[resId]?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}
