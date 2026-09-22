package com.sabalapps.cuteanimalstrace.ui

import com.sabalapps.cuteanimalstrace.data.*
import org.junit.Assert.*
import org.junit.Test

class FilterDrawingsTest {
    private val drawings = listOf(
        DrawingTemplate("kitten", "Curious kitten", TemplateCategory.Cats, "templates/cats/test.webp", Difficulty.Easy, true, "Test cat"),
        DrawingTemplate("tabby", "Whiskered tabby", TemplateCategory.Cats, "templates/cats/test.webp", Difficulty.Detailed, false, "Test tabby"),
        DrawingTemplate("baby_bunny", "Baby bunny", TemplateCategory.BabyAnimals, "templates/baby_animals/test.webp", Difficulty.Easy, false, "Test bunny"),
        DrawingTemplate("baby_bear", "Baby bear", TemplateCategory.BabyAnimals, "templates/baby_animals/test.webp", Difficulty.Medium, false, "Test bear"),
    )

    @Test fun blankQuery_returnsWholeCatalogInOrder() {
        assertEquals(drawings, filterDrawings(drawings, "   ", null, null))
    }

    @Test fun search_matchesNamesAndCategoriesIgnoringCaseAndOuterSpaces() {
        assertEquals(listOf("kitten"), filterDrawings(drawings, "  CURIOUS  ", null, null).map { it.id })
        assertEquals(listOf("kitten", "tabby"), filterDrawings(drawings, "cAtS", null, null).map { it.id })
        assertEquals(listOf("baby_bunny", "baby_bear"), filterDrawings(drawings, "BABY ANIMALS", null, null).map { it.id })
    }

    @Test fun combinedFilters_intersectInsteadOfUnion() {
        assertEquals(listOf("tabby"), filterDrawings(drawings, "cat", TemplateCategory.Cats, Difficulty.Detailed).map { it.id })
        assertTrue(filterDrawings(drawings, "panda", TemplateCategory.Cats, null).isEmpty())
        assertTrue(filterDrawings(drawings, "", TemplateCategory.Cats, Difficulty.Medium).isEmpty())
    }

    @Test fun eachDifficulty_filtersIndependently() {
        Difficulty.entries.forEach { difficulty ->
            assertEquals(drawings.filter { it.difficulty == difficulty }, filterDrawings(drawings, "", null, difficulty))
        }
    }

    @Test fun unmatchedSearch_andEmptyCatalog_returnNoResults() {
        assertTrue(filterDrawings(drawings, "not-an-animal", null, null).isEmpty())
        assertTrue(filterDrawings(emptyList(), "cat", null, null).isEmpty())
    }
}
