package com.sabalapps.cuteanimalstrace.data

/**
 * Decides when the rate / share invitation may appear. Pure logic so the schedule stays
 * readable and testable: milestones at 3, 8 and 15 successful traces, then every ten after that.
 */
object RateSharePrompt {
    const val CooldownMillis = 7L * 24 * 60 * 60 * 1000

    private val EarlyMilestones = intArrayOf(3, 8, 15)
    private const val RepeatEvery = 10
    private const val RepeatFrom = 15

    fun isMilestone(traceCount: Int): Boolean = when {
        traceCount in EarlyMilestones -> true
        traceCount > RepeatFrom -> (traceCount - RepeatFrom) % RepeatEvery == 0
        else -> false
    }

    /**
     * Rating removes the prompt for good; dismissing only skips this milestone, so the next
     * one still arrives once the seven-day cooldown has passed.
     */
    fun shouldShow(preferences: UserPreferences, now: Long): Boolean {
        if (preferences.hasRequestedReview) return false
        if (!isMilestone(preferences.successfulTraceCount)) return false
        val since = now - preferences.lastRateSharePromptTime
        // A negative gap means the device clock moved backwards; treat it as eligible again.
        return preferences.lastRateSharePromptTime == 0L || since < 0 || since >= CooldownMillis
    }
}
