package com.parikiganesh.tomato365.notifications

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.parikiganesh.tomato365.data.local.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Schedules periodic notifications at specific times
 * Noon: 12:00 PM
 * Evening: 6:00 PM
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {

    fun scheduleNotifications() {
        CoroutineScope(Dispatchers.Default).launch {
            val userPrefs = userPreferencesDataStore.userPreferences.firstOrNull()
            val farmerName = userPrefs?.farmerName ?: "Farmer"
            
            scheduleNoonNotification(farmerName)
            scheduleEveningNotification(farmerName)
            Log.d(TAG, "Scheduled daily notifications for $farmerName")
        }
    }

    private fun scheduleNoonNotification(farmerName: String) {
        val noonData = workDataOf(
            "type" to "noon",
            "farmerName" to farmerName
        )
        
        val noonWork = PeriodicWorkRequestBuilder<ScheduledNotificationWorker>(
            1, TimeUnit.DAYS
        )
            .setInputData(noonData)
            .setInitialDelay(getDelayToTime(12, 0), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "noon_notification",
            ExistingPeriodicWorkPolicy.KEEP,
            noonWork
        )
        Log.d(TAG, "Noon notification scheduled for 12:00 PM")
    }

    private fun scheduleEveningNotification(farmerName: String) {
        val eveningData = workDataOf(
            "type" to "evening",
            "farmerName" to farmerName
        )
        
        val eveningWork = PeriodicWorkRequestBuilder<ScheduledNotificationWorker>(
            1, TimeUnit.DAYS
        )
            .setInputData(eveningData)
            .setInitialDelay(getDelayToTime(18, 0), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "evening_notification",
            ExistingPeriodicWorkPolicy.KEEP,
            eveningWork
        )
        Log.d(TAG, "Evening notification scheduled for 6:00 PM")
    }

    /**
     * Calculate delay from now to the specified time
     * Returns delay in milliseconds
     */
    private fun getDelayToTime(hourOfDay: Int, minuteOfHour: Int): Long {
        val now = Calendar.getInstance()
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minuteOfHour)
            set(Calendar.SECOND, 0)
        }

        // If the time has already passed today, schedule for tomorrow
        if (scheduledTime.before(now)) {
            scheduledTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delayMillis = scheduledTime.timeInMillis - now.timeInMillis
        Log.d(TAG, "Delay to $hourOfDay:$minuteOfHour = ${delayMillis / 1000 / 60} minutes")
        
        return delayMillis
    }

    fun cancelNotifications() {
        WorkManager.getInstance(context).cancelUniqueWork("morning_notification")
        WorkManager.getInstance(context).cancelUniqueWork("evening_notification")
        Log.d(TAG, "Cancelled all scheduled notifications")
    }

    companion object {
        private const val TAG = "NotificationScheduler"
    }
}
