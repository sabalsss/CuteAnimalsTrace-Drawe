package com.sabalapps.cuteanimalstrace.ui

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.sabalapps.cuteanimalstrace.R
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** Byte-bounded cache; only visible items decode, with at most two decodes in flight. */
internal object TemplateBitmapLoader {
    private val dispatcher = Dispatchers.IO.limitedParallelism(2)
    private val cache = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount
    }

    suspend fun load(assets: AssetManager, path: String, targetSize: Int): Bitmap = withContext(dispatcher) {
        val key = "$path@$targetSize"
        cache.get(key)?.let { return@withContext it }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("Invalid image: $path")
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= targetSize) sample *= 2
        ensureActive()
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inScaled = false
        }
        val bitmap = assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IOException("Unable to decode: $path")
        ensureActive()
        cache.put(key, bitmap)
        bitmap
    }
}

private data class AssetImageState(val bitmap: Bitmap? = null, val failed: Boolean = false)

@Composable
internal fun AssetTemplateImage(
    imagePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    targetSize: Int = 512,
    colorFilter: ColorFilter? = null,
) {
    val assets = LocalContext.current.assets
    key(imagePath, targetSize) {
        var attempt by remember { mutableIntStateOf(0) }
        val state by produceState(AssetImageState(), imagePath, targetSize, attempt) {
            value = AssetImageState()
            value = try { AssetImageState(TemplateBitmapLoader.load(assets, imagePath, targetSize)) }
            catch (_: IOException) { AssetImageState(failed = true) }
        }
        val status = stringResource(when {
            state.bitmap != null -> R.string.artwork_loaded
            state.failed -> R.string.artwork_failed
            else -> R.string.artwork_loading
        })
        Box(modifier.semantics { stateDescription = status }, contentAlignment = Alignment.Center) {
            val bitmap = state.bitmap
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), contentDescription, Modifier.matchParentSize(),
                    contentScale = ContentScale.Fit, colorFilter = colorFilter)
            } else if (state.failed) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.artwork_failed))
                    TextButton(onClick = { attempt++ }) { Text(stringResource(R.string.retry_camera)) }
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}
