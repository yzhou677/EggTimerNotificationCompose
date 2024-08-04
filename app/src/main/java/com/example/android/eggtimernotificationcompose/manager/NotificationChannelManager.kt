package com.example.android.eggtimernotificationcompose.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import com.example.android.eggtimernotificationcompose.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationChannelManager @Inject constructor(
    private val context: Context,
    private val ringtoneUri: Uri,
    private val notificationManager: NotificationManager
){
    /**
     * Creates and registers Notification channel, which is required
     *
     * @param channelId, Notification channel id.
     * @param channelName, Notification channel name.
     */
    fun createNotificationChannel(channelId: String, channelName: String) {
        // create the NotificationChannel
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
        notificationManager.createNotificationChannel(notificationChannel)
    }
}