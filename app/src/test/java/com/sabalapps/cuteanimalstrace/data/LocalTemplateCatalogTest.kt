package com.sabalapps.cuteanimalstrace.data

import org.junit.Assert.*
import org.junit.Test

class LocalTemplateCatalogTest {
    @Test
    fun catalog_hasTwelveCompleteRecordsWithUniqueRouteSafeIds() {
        val templates = LocalTemplateCatalog.templates
        assertEquals(12, templates.size)
        assertEquals(templates.size, templates.map { it.id }.toSet().size)
        templates.forEach {
            assertTrue(it.id.matches(Regex("[a-z0-9_]+")))
            assertTrue(it.name.isNotBlank())
            assertTrue(it.description.isNotBlank())
            assertNotEquals(0, it.imageRes)
        }
    }

    @Test
    fun catalog_coversEveryInitialCategoryAndDifficulty() {
        assertEquals(TemplateCategory.entries.toSet(), LocalTemplateCatalog.templates.map { it.category }.toSet())
        assertEquals(Difficulty.entries.toSet(), LocalTemplateCatalog.templates.map { it.difficulty }.toSet())
    }

    @Test
    fun featuredSelection_isNonemptyAndContainsOnlyFeaturedRecords() {
        assertTrue(LocalTemplateCatalog.featuredTemplates.isNotEmpty())
        assertEquals(LocalTemplateCatalog.templates.filter { it.featured }, LocalTemplateCatalog.featuredTemplates)
        assertTrue(LocalTemplateCatalog.templates.any { !it.featured })
    }

    @Test
    fun lookup_resolvesEveryRecordAndRejectsUnknownIds() {
        LocalTemplateCatalog.templates.forEach { assertEquals(it, LocalTemplateCatalog.findById(it.id)) }
        assertNull(LocalTemplateCatalog.findById("missing"))
        assertNull(LocalTemplateCatalog.findById(null))
    }
}
