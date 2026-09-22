package com.sabalapps.cuteanimalstrace.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.sabalapps.cuteanimalstrace.R

/**
 * Tiny chime for adding a favorite. SoundPool keeps it preloaded so it plays without delay, and
 * the sonification usage lets it follow the system media volume without taking audio focus.
 */
internal class FavoriteSound(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build())
        .build()
    private val soundId = pool.load(context, R.raw.favorite_added, 1)

    fun play() {
        pool.play(soundId, 0.6f, 0.6f, 1, 0, 1f)
    }

    fun release() = pool.release()
}

@Composable
internal fun rememberFavoriteSound(): FavoriteSound {
    val context = LocalContext.current
    val sound = remember(context) { FavoriteSound(context.applicationContext) }
    DisposableEffect(sound) { onDispose { sound.release() } }
    return sound
}
