package com.example.android.eggtimernotificationcompose.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.android.eggtimernotificationcompose.model.TimerEntity

@Database(
    entities = [TimerEntity::class],
    version = 2
)
@TypeConverters(TimerConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timerDao(): TimerDao
}