package com.example.android.eggtimernotificationcompose.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.example.android.eggtimernotificationcompose.MainActivity
import com.example.android.eggtimernotificationcompose.R
import com.example.android.eggtimernotificationcompose.receiver.SnoozeReceiver
import com.example.android.eggtimernotificationcompose.receiver.getDismissIntent

private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0

/**
 * Builds and delivers the notification.
 *
 * @param messageBody, activity context.
 */
fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {
    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    val dismissIntent = getDismissIntent(applicationContext, NOTIFICATION_ID)

    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val eggImage = BitmapFactory.decodeResource(
        applicationContext.resources,
        R.drawable.cooked_egg
    )
    val bigPicStyle = NotificationCompat.BigPictureStyle().bigPicture(eggImage)

    val snoozeIntent = Intent(applicationContext, SnoozeReceiver::class.java)
    val snoozePendingIntent: PendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        REQUEST_CODE,
        snoozeIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.egg_notification_channel_id)
    )
        .setSmallIcon(R.drawable.cooked_egg)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .setStyle(bigPicStyle)
        .setLargeIcon(eggImage)
        .addAction(
            R.drawable.egg_icon,
            applicationContext.getString(R.string.snooze),
            snoozePendingIntent
        )
        .addAction(
            android.R.drawable.ic_delete, // 替换为你的 dismiss 图标
            applicationContext.getString(R.string.dismiss), // 替换为你的 dismiss 文本
            dismissIntent
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))

    notify(NOTIFICATION_ID, builder.build())
}

/**
 * Cancels all notifications.
 */
fun NotificationManager.cancelNotifications() {
    cancelAll()
}

