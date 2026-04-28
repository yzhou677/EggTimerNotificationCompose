package com.example.android.eggtimernotificationcompose.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.android.eggtimernotificationcompose.util.sendNotification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver: BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getStringExtra("TIMER_ID") ?: return

        notificationManager.sendNotification(
            "Timer $timerId finished",
            context
        )
    }

}