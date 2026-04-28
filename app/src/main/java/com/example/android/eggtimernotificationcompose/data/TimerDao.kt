package com.example.android.eggtimernotificationcompose.data

import androidx.room.*
import com.example.android.eggtimernotificationcompose.model.TimerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao {

    @Query("SELECT * FROM timers")
    suspend fun getAll(): List<TimerEntity>

    @Query("SELECT * FROM timers")
    fun observeTimers(): Flow<List<TimerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timer: TimerEntity)

    @Query("DELETE FROM timers WHERE id = :id")
    suspend fun delete(id: String)
}