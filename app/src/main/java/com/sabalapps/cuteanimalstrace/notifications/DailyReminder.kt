package com.sabalapps.cuteanimalstrace.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sabalapps.cuteanimalstrace.MainActivity
import com.sabalapps.cuteanimalstrace.R
import com.sabalapps.cuteanimalstrace.data.UserPreferencesRepository
import com.sabalapps.cuteanimalstrace.data.userPreferencesStore
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

/**
 * A single local, offline daily nudge back into the app. Nothing is fetched and nothing is sent:
 * WorkManager wakes a worker once a day and posts one notification from bundled strings.
 */
object DailyReminder {
    const val ChannelId = "daily_reminder"
    private const val WorkName = "daily_reminder"
    private const val NotificationId = 2001

    /** Late morning: past the school run, well clear of quiet hours in either direction. */
    private val ReminderTime: LocalTime = LocalTime.of(10, 0)

    private val Messages = intArrayOf(
        R.string.reminder_message_1,
        R.string.reminder_message_2,
        R.string.reminder_message_3,
        R.string.reminder_message_4,
        R.string.reminder_message_5,
    )

    /**
     * Keeps any reminder that is already pending, so simply opening the app never pushes the
     * next one further away.
     */
    fun schedule(context: Context) {
        runCatching {
            ensureChannel(context)
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay().toMillis(), TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WorkName, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }

    fun cancel(context: Context) {
        runCatching {
            WorkManager.getInstance(context).cancelUniqueWork(WorkName)
            NotificationManagerCompat.from(context).cancel(NotificationId)
        }
    }

    internal fun initialDelay(now: LocalDateTime = LocalDateTime.now()): Duration {
        val today = now.toLocalDate().atTime(ReminderTime)
        val next = if (today.isAfter(now)) today else today.plusDays(1)
        return Duration.between(now, next)
    }

    private fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            ChannelId,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.reminder_channel_description) }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    /** Rotates through the bundled lines by day, so the nudge does not read the same every morning. */
    internal fun messageFor(date: LocalDate): Int = Messages[date.toEpochDay().mod(Messages.size)]

    fun notifyNow(context: Context) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        ensureChannel(context)
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, ChannelId)
            .setSmallIcon(R.drawable.ic_paw)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(messageFor(LocalDate.now())))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(context.getString(messageFor(LocalDate.now()))))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        runCatching {
            NotificationManagerCompat.from(context).notify(NotificationId, notification)
        }
    }
}

/** Re-checks the user's choice before posting, in case the toggle flipped while work was queued. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val enabled = runCatching {
            UserPreferencesRepository(applicationContext.userPreferencesStore).data.first().dailyReminder
        }.getOrDefault(false)
        if (enabled) DailyReminder.notifyNow(applicationContext)
        return Result.success()
    }
}
