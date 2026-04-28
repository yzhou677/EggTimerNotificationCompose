package com.example.android.eggtimernotificationcompose.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timers")
data class TimerEntity(
    @PrimaryKey val id: String,
    val triggerAtMillis: Long,
    val status: TimerStatus
)

enum class TimerStatus {
    SCHEDULED,
    FIRED,
    CANCELLED
}