package com.sabalapps.cuteanimalstrace.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

enum class Appearance { System, Light, Dark }

data class UserPreferences(
    val favorites: Set<String> = emptySet(),
    val recent: List<String> = emptyList(),
    val appearance: Appearance = Appearance.System,
    val dynamicColor: Boolean = false,
    val keepAwake: Boolean = true,
    val showTips: Boolean = true,
    val defaultOpacity: Float = 0.65f,
    val successfulTraceCount: Int = 0,
    val lastRateSharePromptTime: Long = 0L,
    val hasRequestedReview: Boolean = false,
    val hasShared: Boolean = false,
    val promptDismissCount: Int = 0,
    val dailyReminder: Boolean = true,
    val onboardingSeen: Boolean = false,
)

// A single application-scoped instance; IDs refer to the bundled catalog, never copied templates.
val Context.userPreferencesStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val store: DataStore<Preferences>) {
    private object Keys {
        val favorites = stringSetPreferencesKey("favorites")
        val recent = stringPreferencesKey("recent")
        val appearance = stringPreferencesKey("appearance")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val keepAwake = booleanPreferencesKey("keep_awake")
        val showTips = booleanPreferencesKey("show_tips")
        val opacity = floatPreferencesKey("default_opacity")
        val traceCount = intPreferencesKey("successful_trace_count")
        val lastPrompt = longPreferencesKey("last_rate_share_prompt")
        val requestedReview = booleanPreferencesKey("has_requested_review")
        val shared = booleanPreferencesKey("has_shared")
        val dismissCount = intPreferencesKey("prompt_dismiss_count")
        val dailyReminder = booleanPreferencesKey("daily_reminder")
        val onboardingSeen = booleanPreferencesKey("onboarding_seen")
    }

    val data = store.data.map { values ->
        UserPreferences(
            favorites = values[Keys.favorites].orEmpty(),
            recent = decodeRecent(values[Keys.recent]),
            appearance = Appearance.entries.find { it.name == values[Keys.appearance] } ?: Appearance.System,
            dynamicColor = values[Keys.dynamicColor] ?: false,
            keepAwake = values[Keys.keepAwake] ?: true,
            showTips = values[Keys.showTips] ?: true,
            defaultOpacity = (values[Keys.opacity] ?: 0.65f).let {
                if (it.isFinite()) it.coerceIn(0.1f, 1f) else 0.65f
            },
            successfulTraceCount = (values[Keys.traceCount] ?: 0).coerceAtLeast(0),
            lastRateSharePromptTime = values[Keys.lastPrompt] ?: 0L,
            hasRequestedReview = values[Keys.requestedReview] ?: false,
            hasShared = values[Keys.shared] ?: false,
            promptDismissCount = (values[Keys.dismissCount] ?: 0).coerceAtLeast(0),
            dailyReminder = values[Keys.dailyReminder] ?: true,
            onboardingSeen = values[Keys.onboardingSeen] ?: false,
        )
    }

    suspend fun toggleFavorite(id: String) {
        store.edit { values ->
            val saved = values[Keys.favorites].orEmpty()
            values[Keys.favorites] = if (id in saved) saved - id else saved + id
        }
    }

    suspend fun recordViewed(id: String) {
        require(id.isNotBlank() && '\n' !in id)
        store.edit { values ->
            values[Keys.recent] = (listOf(id) + decodeRecent(values[Keys.recent]))
                .distinct().take(MAX_RECENT).joinToString("\n")
        }
    }

    suspend fun clearRecent() { store.edit { it.remove(Keys.recent) } }
    suspend fun setAppearance(value: Appearance) { store.edit { it[Keys.appearance] = value.name } }
    suspend fun setDynamicColor(value: Boolean) { store.edit { it[Keys.dynamicColor] = value } }
    suspend fun setKeepAwake(value: Boolean) { store.edit { it[Keys.keepAwake] = value } }
    suspend fun setShowTips(value: Boolean) { store.edit { it[Keys.showTips] = value } }
    suspend fun setDefaultOpacity(value: Float) {
        if (value.isFinite()) store.edit { it[Keys.opacity] = value.coerceIn(0.1f, 1f) }
    }

    /** Counted once per trace session that actually reaches a live camera overlay. */
    suspend fun recordSuccessfulTrace() {
        store.edit { values ->
            values[Keys.traceCount] = (values[Keys.traceCount] ?: 0).coerceAtLeast(0) + 1
        }
    }

    suspend fun setDailyReminder(value: Boolean) { store.edit { it[Keys.dailyReminder] = value } }
    suspend fun setOnboardingSeen() { store.edit { it[Keys.onboardingSeen] = true } }

    suspend fun recordPromptShown(now: Long) { store.edit { it[Keys.lastPrompt] = now } }
    suspend fun recordReviewRequested() { store.edit { it[Keys.requestedReview] = true } }
    suspend fun recordShared() { store.edit { it[Keys.shared] = true } }
    suspend fun recordPromptDismissed() {
        store.edit { values ->
            values[Keys.dismissCount] = (values[Keys.dismissCount] ?: 0).coerceAtLeast(0) + 1
        }
    }

    companion object {
        const val MAX_RECENT = 20
        private fun decodeRecent(value: String?) =
            value.orEmpty().lineSequence().filter { it.isNotBlank() }.distinct().take(MAX_RECENT).toList()
    }
}
