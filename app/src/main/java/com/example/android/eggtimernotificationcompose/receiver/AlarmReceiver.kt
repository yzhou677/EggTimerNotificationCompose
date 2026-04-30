package com.example.android.eggtimernotificationcompose.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.android.eggtimernotificationcompose.data.TimerRepository
import com.example.android.eggtimernotificationcompose.model.TimerStatus
import com.example.android.eggtimernotificationcompose.util.sendNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var repository: TimerRepository

    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getStringExtra("TIMER_ID") ?: return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val timers = repository.getAll()
                val timer = timers.find { it.id == timerId }

                val label = timer?.label ?: "Timer"

                notificationManager.sendNotification(
                    "$label finished",
                    context,
                    timerId
                )

                timer?.let {
                    repository.update(it.copy(status = TimerStatus.FIRED))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}