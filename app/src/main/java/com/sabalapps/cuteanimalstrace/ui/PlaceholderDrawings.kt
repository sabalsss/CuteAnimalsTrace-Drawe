package com.sabalapps.cuteanimalstrace.ui

import androidx.annotation.StringRes
import com.sabalapps.cuteanimalstrace.R

// UI samples only. IDs are fixed route-safe values; no repository or persistence is needed yet.
data class PlaceholderDrawing(val id: String, @param:StringRes val name: Int)

val placeholderDrawings = listOf(
    PlaceholderDrawing("bunny", R.string.bunny),
    PlaceholderDrawing("kitten", R.string.kitten),
    PlaceholderDrawing("bear", R.string.bear),
    PlaceholderDrawing("fox", R.string.fox),
)
