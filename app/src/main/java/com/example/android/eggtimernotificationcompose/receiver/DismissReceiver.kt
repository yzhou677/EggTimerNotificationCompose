package com.example.android.eggtimernotificationcompose.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DismissReceiver: BroadcastReceiver() {
    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        // Dismiss the notification
        val notificationId = intent.getIntExtra("notification_id", -1)
        notificationManager.cancel(notificationId)
    }
}

/**
 * Creates a PendingIntent for dismissing a notification.
 *
 * @param context, the application context.
 * @param notificationId, the ID of the notification to dismiss.
 */
fun getDismissIntent(context: Context, notificationId: Int): PendingIntent {
    val intent = Intent(context, DismissReceiver::class.java).apply {
        putExtra("notification_id", notificationId)
    }
    return PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}