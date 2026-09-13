package com.sabalapps.cuteanimalstrace.data

import androidx.annotation.DrawableRes

/** Bundled, read-only template metadata. imageRes identifies a local drawable asset. */
data class DrawingTemplate(
    val id: String,
    val name: String,
    val category: TemplateCategory,
    @param:DrawableRes val imageRes: Int,
    val difficulty: Difficulty,
    val featured: Boolean,
    val description: String,
)

enum class Difficulty(val label: String) {
    Easy("Easy"), Medium("Medium"), Detailed("Detailed"),
}

enum class TemplateCategory(val label: String) {
    Cats("Cats"), Dogs("Dogs"), Bunnies("Bunnies"), Pandas("Pandas"),
    Foxes("Foxes"), Bears("Bears"), Kawaii("Kawaii"), BabyAnimals("Baby Animals"),
}
