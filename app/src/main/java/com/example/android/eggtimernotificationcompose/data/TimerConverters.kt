package com.example.android.eggtimernotificationcompose.data

import androidx.room.TypeConverter
import com.example.android.eggtimernotificationcompose.model.TimerStatus

class TimerConverters {

    @TypeConverter
    fun fromStatus(status: TimerStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(value: String): TimerStatus {
        return TimerStatus.valueOf(value)
    }
}