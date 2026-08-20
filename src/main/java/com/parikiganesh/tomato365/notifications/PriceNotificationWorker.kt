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

class PriceNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val marketName = inputData.getString("marketName") ?: return Result.failure()
            val minPrice = inputData.getString("minPrice") ?: "--"
            val maxPrice = inputData.getString("maxPrice") ?: "--"
            val boxTypeKg = inputData.getInt("boxTypeKg", 0)

            showNotification(marketName, minPrice, maxPrice, boxTypeKg)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending price notification", e)
            Result.retry()
        }
    }

    private fun showNotification(
        marketName: String,
        minPrice: String,
        maxPrice: String,
        boxTypeKg: Int
    ) {
        createChannelIfNeeded()

        val body = if (boxTypeKg > 0) {
            "₹$minPrice - ₹$maxPrice ($boxTypeKg kg)"
        } else {
            "₹$minPrice - ₹$maxPrice"
        }

        val destinationIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.EXTRA_NOTIFICATION_MARKET_NAME, marketName)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis().toInt(),
            destinationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, Constants.NOTIFICATION_CHANNEL_PRICE_UPDATES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(applicationContext.getString(R.string.notification_title_with_market, marketName))
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
            System.currentTimeMillis().toInt(),
            notification
        )
        Log.d(TAG, "Price notification sent for market: $marketName")
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
        private const val TAG = "PriceNotificationWorker"
    }
}
