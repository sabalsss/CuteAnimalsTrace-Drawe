package com.sabalapps.cuteanimalstrace.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UserPreferencesRepositoryTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun favoritesAndAllSettingsSurviveStoreReopening() = runBlocking {
        val file = temporary.newFolder().resolve("preferences.preferences_pb")
        var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        var repository = UserPreferencesRepository(PreferenceDataStoreFactory.create(scope = scope) { file })
        try {
            assertEquals(UserPreferences(), repository.data.first())
            coroutineScope { repeat(10) { launch { repository.toggleFavorite("cat_001") } } }
            assertTrue(repository.data.first().favorites.isEmpty())
            repository.toggleFavorite("cat_001")
            repository.recordViewed("cat_001")
            repository.setAppearance(Appearance.Dark)
            repository.setDynamicColor(true)
            repository.setKeepAwake(false)
            repository.setShowTips(false)
            repository.setDefaultOpacity(0.35f)
            val expected = repository.data.first()
            scope.coroutineContext.job.cancelAndJoin()
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            repository = UserPreferencesRepository(PreferenceDataStoreFactory.create(scope = scope) { file })
            assertEquals(expected, repository.data.first())
            assertEquals(setOf("cat_001"), repository.data.first().favorites)
            repository.toggleFavorite("cat_001")
            assertTrue(repository.data.first().favorites.isEmpty())
        } finally { scope.coroutineContext.job.cancelAndJoin() }
    }

    @Test fun recentIsNewestFirstUniqueBounded_andClearPreservesOtherData() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = temporary.newFolder().resolve("preferences.preferences_pb")
        val repository = UserPreferencesRepository(PreferenceDataStoreFactory.create(scope = scope) { file })
        try {
            repeat(25) { repository.recordViewed("animal_$it") }
            assertEquals((24 downTo 5).map { "animal_$it" }, repository.data.first().recent)
            repository.recordViewed("animal_12")
            val recent = repository.data.first().recent
            assertEquals("animal_12", recent.first())
            assertEquals(20, recent.size)
            assertEquals(recent.size, recent.distinct().size)
            repository.toggleFavorite("cat_001")
            repository.setAppearance(Appearance.Light)
            repository.clearRecent()
            val data = repository.data.first()
            assertTrue(data.recent.isEmpty())
            assertEquals(setOf("cat_001"), data.favorites)
            assertEquals(Appearance.Light, data.appearance)
        } finally { scope.coroutineContext.job.cancelAndJoin() }
    }

    @Test fun defaultOpacityIsBoundedAndRejectsInvalidValues() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = temporary.newFolder().resolve("preferences.preferences_pb")
        val repository = UserPreferencesRepository(PreferenceDataStoreFactory.create(scope = scope) { file })
        try {
            repository.setDefaultOpacity(-1f)
            assertEquals(0.1f, repository.data.first().defaultOpacity, 0f)
            repository.setDefaultOpacity(9f)
            repository.setDefaultOpacity(Float.NaN)
            assertEquals(1f, repository.data.first().defaultOpacity, 0f)
        } finally { scope.coroutineContext.job.cancelAndJoin() }
    }
}
