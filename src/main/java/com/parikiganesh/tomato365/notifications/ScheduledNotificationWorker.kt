package com.parikiganesh.tomato365.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.parikiganesh.tomato365.MainActivity
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.utils.Constants

class ScheduledNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val notificationType = inputData.getString("type") ?: "morning"
            showScheduledNotification(notificationType)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending scheduled notification", e)
            Result.retry()
        }
    }

    private fun showScheduledNotification(type: String) {
        createChannelIfNeeded()
        
        val farmerName = inputData.getString("farmerName") ?: "Farmer"

        val (title, body) = when (type) {
            "noon" -> {
                "Hello $farmerName! 🌤️" to "Check today's tomato prices in your preferred markets"
            }
            "evening" -> {
                "Evening Update, $farmerName! 🌆" to "See the latest tomato prices before closing time"
            }
            else -> {
                "Tomato Prices" to "Check the latest price updates"
            }
        }

        val destinationIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis().toInt(),
            destinationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, Constants.NOTIFICATION_CHANNEL_PRICE_UPDATES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Skipping notification because POST_NOTIFICATIONS is not granted")
            return
        }

        NotificationManagerCompat.from(applicationContext).notify(
            type.hashCode(),
            notification
        )
        Log.d(TAG, "Scheduled notification sent: $type")
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(Constants.NOTIFICATION_CHANNEL_PRICE_UPDATES) != null) return
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_PRICE_UPDATES,
            applicationContext.getString(R.string.notification_channel_price_updates),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = applicationContext.getString(R.string.notification_channel_price_updates_description)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "ScheduledNotificationWorker"
    }
}
