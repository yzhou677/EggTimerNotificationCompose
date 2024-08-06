package com.example.android.eggtimernotificationcompose.di

interface Clock {
    fun elapsedRealtime(): Long
}

class SystemClockImpl : Clock {
    override fun elapsedRealtime(): Long {
        return android.os.SystemClock.elapsedRealtime()
    }
}
