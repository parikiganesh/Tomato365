package com.parikiganesh.tomato365.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.parikiganesh.tomato365.MainActivity
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.utils.Constants

class FcmService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        showPriceUpdateNotification(message)
    }

    private fun showPriceUpdateNotification(message: RemoteMessage) {
        createChannelIfNeeded()

        val marketName = message.data["marketName"].orEmpty()
        val title = message.notification?.title
            ?: message.data["title"]
            ?: if (marketName.isNotBlank()) {
                getString(R.string.notification_title_with_market, marketName)
            } else {
                getString(R.string.notification_title_fallback)
            }
        val body = message.notification?.body
            ?: message.data["body"]
            ?: getString(R.string.notification_body_fallback)

        val destinationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.EXTRA_NOTIFICATION_MARKET_NAME, marketName)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            1001,
            destinationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_PRICE_UPDATES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Skipping notification because POST_NOTIFICATIONS is not granted")
            return
        }

        NotificationManagerCompat.from(this).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(Constants.NOTIFICATION_CHANNEL_PRICE_UPDATES) != null) return
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_PRICE_UPDATES,
            getString(R.string.notification_channel_price_updates),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notification_channel_price_updates_description)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "FcmService"
    }
}
