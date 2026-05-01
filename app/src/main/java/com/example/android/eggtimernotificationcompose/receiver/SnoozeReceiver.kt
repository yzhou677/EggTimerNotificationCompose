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

    companion object {
        const val ACTION_TIMER_RESCHEDULED =
            "com.example.android.eggtimernotificationcompose.ACTION_TIMER_RESCHEDULED"
        const val EXTRA_TIMER_ID = "TIMER_ID"
    }

    @Inject
    lateinit var timerEngine: TimerEngine

    @Inject
    lateinit var repository: TimerRepository

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getStringExtra("TIMER_ID") ?: return

        val newTriggerAt = System.currentTimeMillis() + 60_000
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.withPersistenceLock {
                    timerEngine.schedule(timerId, newTriggerAt)
                    repository.getById(timerId)?.let {
                        repository.update(
                            it.copy(
                                triggerAtMillis = newTriggerAt,
                                status = TimerStatus.SCHEDULED
                            )
                        )
                    }
                }
                context.sendBroadcast(
                    Intent(ACTION_TIMER_RESCHEDULED).apply {
                        setPackage(context.packageName)
                        putExtra(EXTRA_TIMER_ID, timerId)
                    }
                )
                notificationManager.cancelAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}