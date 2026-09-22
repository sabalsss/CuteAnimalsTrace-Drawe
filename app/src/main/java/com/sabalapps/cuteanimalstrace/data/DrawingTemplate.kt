package com.sabalapps.cuteanimalstrace.data

/** Separate UI artwork and tracing ink; both paths are relative to Android assets. */
data class DrawingTemplate(
    val id: String,
    val name: String,
    val category: TemplateCategory,
    val traceImagePath: String,
    val difficulty: Difficulty,
    val featured: Boolean,
    val description: String,
    val previewImagePath: String = traceImagePath,
)

enum class Difficulty(val label: String) {
    Easy("Easy"), Medium("Medium"), Detailed("Detailed"),
}

enum class TemplateCategory(val label: String) {
    Cats("Cats"), Dogs("Dogs"), Bunnies("Bunnies"), Pandas("Pandas"),
    Foxes("Foxes"), Bears("Bears"), Kawaii("Kawaii Animals"), BabyAnimals("Baby Animals"),
}
