package com.example.android.eggtimernotificationcompose.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.android.eggtimernotificationcompose.data.TimerRepository
import com.example.android.eggtimernotificationcompose.engine.TimerEngine
import com.example.android.eggtimernotificationcompose.model.TimerStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SnoozeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var timerEngine: TimerEngine

    @Inject
    lateinit var repository: TimerRepository

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getStringExtra("TIMER_ID") ?: return

        val newTriggerAt = System.currentTimeMillis() + 60_000

        // Reschedule via TimerEngine
        timerEngine.schedule(timerId, newTriggerAt)

        // Update DB
        CoroutineScope(Dispatchers.IO).launch {
            val timers = repository.getAll()
            val timer = timers.find { it.id == timerId }

            timer?.let {
                repository.update(
                    it.copy(
                        triggerAtMillis = newTriggerAt,
                        status = TimerStatus.SCHEDULED
                    )
                )
            }
        }

        // Clear current notification
        notificationManager.cancelAll()
    }
}