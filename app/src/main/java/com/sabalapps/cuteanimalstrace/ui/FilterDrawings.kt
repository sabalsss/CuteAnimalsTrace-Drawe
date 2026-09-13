package com.sabalapps.cuteanimalstrace.ui

import com.sabalapps.cuteanimalstrace.data.Difficulty
import com.sabalapps.cuteanimalstrace.data.DrawingTemplate
import com.sabalapps.cuteanimalstrace.data.TemplateCategory

/** All active filters intersect; a blank query and null selections mean all drawings. */
internal fun filterDrawings(
    drawings: List<DrawingTemplate>,
    query: String,
    category: TemplateCategory?,
    difficulty: Difficulty?,
): List<DrawingTemplate> {
    val search = query.trim()
    return drawings.filter { drawing ->
        (category == null || drawing.category == category) &&
            (difficulty == null || drawing.difficulty == difficulty) &&
            (search.isEmpty() || drawing.name.contains(search, ignoreCase = true) ||
                drawing.category.label.contains(search, ignoreCase = true))
    }
}
