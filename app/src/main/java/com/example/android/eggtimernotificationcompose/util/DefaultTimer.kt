package com.example.android.eggtimernotificationcompose.util

import android.os.CountDownTimer

interface Timer {
    fun start()
    fun cancel()

    interface Factory {
        fun create(millisInFuture: Long, countDownInterval: Long, onTick: (Long) -> Unit, onFinish: () -> Unit): Timer
    }
}

class DefaultTimer(
    millisInFuture: Long,
    countDownInterval: Long,
    private val onTickCallback: (Long) -> Unit,
    private val onFinishCallback: () -> Unit
) : Timer {

    private val countDownTimer = object : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
            onTickCallback(millisUntilFinished)
        }

        override fun onFinish() {
            onFinishCallback()
        }
    }

    override fun start() {
        countDownTimer.start()
    }

    override fun cancel() {
        countDownTimer.cancel()
    }
}