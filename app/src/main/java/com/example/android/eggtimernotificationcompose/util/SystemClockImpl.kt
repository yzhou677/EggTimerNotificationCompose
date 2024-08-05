package com.example.android.eggtimernotificationcompose.util

interface Clock {
    fun elapsedRealtime(): Long
}

class SystemClockImpl : Clock {
    override fun elapsedRealtime(): Long {
        return android.os.SystemClock.elapsedRealtime()
    }
}
