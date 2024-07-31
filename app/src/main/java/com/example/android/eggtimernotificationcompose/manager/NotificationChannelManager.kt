package com.example.android.eggtimernotificationcompose.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import com.example.android.eggtimernotificationcompose.R

object NotificationChannelManager {
    /**
     * Creates and registers Notification channel, which is required
     *
     * @param context, activity context.
     * @param channelId, Notification channel id.
     * @param channelName, Notification channel name.
     */
    fun createNotificationChannel(context: Context, channelId: String, channelName: String) {
        // create the NotificationChannel
        val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setShowBadge(false)
            enableLights(true)
            lightColor = android.graphics.Color.RED
            enableVibration(true)
            description = context.getString(R.string.breakfast_notification_channel_description)
            setSound(ringtoneUri, null)
            vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            enableVibration(true)
        }

        // register the channel with the system
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(notificationChannel)
    }
}