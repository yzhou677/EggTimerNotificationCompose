package com.example.android.eggtimernotificationcompose

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.eggtimernotificationcompose.data.AppDatabase
import com.example.android.eggtimernotificationcompose.data.TimerDao
import com.example.android.eggtimernotificationcompose.data.TimerRepository
import com.example.android.eggtimernotificationcompose.model.TimerEntity
import com.example.android.eggtimernotificationcompose.model.TimerStatus
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimerRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TimerDao
    private lateinit var repository: TimerRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = db.timerDao()
        repository = TimerRepository(dao)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insert_and_read_timer() = runBlocking {
        val timer = TimerEntity(
            id = "test-id",
            triggerAtMillis = 1000L,
            status = TimerStatus.SCHEDULED
        )

        repository.save(timer)

        val result = repository.getAll()

        assertEquals(1, result.size)
        assertEquals("test-id", result[0].id)
    }

    @Test
    fun update_timer_status() = runBlocking {
        val timer = TimerEntity(
            id = "test-id",
            triggerAtMillis = 1000L,
            status = TimerStatus.SCHEDULED
        )

        repository.save(timer)

        repository.update(timer.copy(status = TimerStatus.FIRED))

        val result = repository.getAll()

        assertEquals(TimerStatus.FIRED, result[0].status)
    }
}