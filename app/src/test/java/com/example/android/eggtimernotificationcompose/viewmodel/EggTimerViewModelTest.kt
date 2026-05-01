package com.example.android.eggtimernotificationcompose.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.android.eggtimernotificationcompose.R
import com.example.android.eggtimernotificationcompose.data.TimerDao
import com.example.android.eggtimernotificationcompose.data.TimerRepository
import com.example.android.eggtimernotificationcompose.di.CustomTimerPrefs
import com.example.android.eggtimernotificationcompose.di.LastEffectiveTimerSelectionPrefs
import com.example.android.eggtimernotificationcompose.engine.TimerEngine
import com.example.android.eggtimernotificationcompose.model.TimerEntity
import com.example.android.eggtimernotificationcompose.model.TimerStatus
import com.example.android.eggtimernotificationcompose.util.cancelNotifications
import com.google.gson.Gson
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock

/**
 * Focused tests for timer persistence, scheduling order, restore rules, and cancel/countdown safety.
 * Uses an in-memory [TimerDao] so [TimerRepository.withPersistenceLock] matches production behavior.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class EggTimerViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var application: Application

    @Mock
    private lateinit var resources: Resources

    @Mock
    private lateinit var alarmManager: AlarmManager

    @Mock
    @CustomTimerPrefs
    private lateinit var customTimerPrefs: SharedPreferences

    @Mock
    @LastEffectiveTimerSelectionPrefs
    private lateinit var lastEffectiveTimerSelectionPrefs: SharedPreferences

    @Mock
    private lateinit var lastEffectiveTimerSelectionEditor: SharedPreferences.Editor

    private lateinit var gson: Gson

    @Mock
    private lateinit var notificationManager: NotificationManager

    @Mock
    private lateinit var timerEngine: TimerEngine

    private lateinit var inMemoryDao: InMemoryTimerDao
    private lateinit var repository: TimerRepository
    private lateinit var viewModel: EggTimerViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        gson = Gson()

        `when`(application.resources).thenReturn(resources)
        `when`(application.getString(R.string.egg_item_for_testing)).thenReturn("TestEgg")
        `when`(resources.getStringArray(R.array.egg_array)).thenReturn(arrayOf("Soft", "Medium", "Hard"))
        `when`(resources.getIntArray(R.array.minutes_array)).thenReturn(intArrayOf(3, 4, 5))

        `when`(lastEffectiveTimerSelectionPrefs.edit()).thenReturn(lastEffectiveTimerSelectionEditor)
        `when`(lastEffectiveTimerSelectionEditor.putString(anyString(), anyString()))
            .thenReturn(lastEffectiveTimerSelectionEditor)
        `when`(lastEffectiveTimerSelectionEditor.apply()).then { }

        inMemoryDao = InMemoryTimerDao()
        repository = TimerRepository(inMemoryDao)
        viewModel = createViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        clearInvocations(timerEngine, notificationManager)
    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) {
            closeViewModel(viewModel)
        }
    }

    /** [EggTimerViewModel.onCleared] is protected; finish pending countdown jobs so [runTest] does not fail. */
    private fun closeViewModel(vm: EggTimerViewModel) {
        val m = ViewModel::class.java.getDeclaredMethod("onCleared")
        m.isAccessible = true
        m.invoke(vm)
    }

    private fun createViewModel(repo: TimerRepository): EggTimerViewModel =
        EggTimerViewModel(
            application,
            alarmManager,
            customTimerPrefs,
            lastEffectiveTimerSelectionPrefs,
            gson,
            notificationManager,
            timerEngine,
            repo,
            true
        )

    @Test
    fun setTimeSelected_updatesLiveData() {
        val observer: Observer<Int> = mock()
        viewModel.timeSelection.observeForever(observer)

        viewModel.setTimeSelected(2)

        verify(observer).onChanged(2)
    }

    @Test
    fun startTimer_setsAlarmOn_andSchedulesTriggerInFuture() = runTest(testDispatcher) {
        val before = System.currentTimeMillis()
        viewModel.setTimeSelected(1)
        viewModel.startTimer(1)
        advanceUntilIdle()

        assertEquals(true, viewModel.isAlarmOn.value)
        val triggerCaptor = argumentCaptor<Long>()
        verify(timerEngine, times(1)).schedule(anyString(), triggerCaptor.capture())
        val trigger = triggerCaptor.firstValue
        assertTrue(trigger > before)
        assertTrue(trigger <= before + 3 * 60_000 + 2_000)
    }

    @Test
    fun startTimer_secondStart_cancelsFirst_andLeavesOneScheduled() = runTest(testDispatcher) {
        viewModel.setTimeSelected(1)
        viewModel.startTimer(1)
        advanceUntilIdle()
        viewModel.startTimer(2)
        advanceUntilIdle()

        verify(timerEngine, atLeastOnce()).cancel(anyString())
        val scheduled = runBlocking { repository.getAll().filter { it.status == TimerStatus.SCHEDULED } }
        assertEquals(1, scheduled.size)
        verify(timerEngine, times(2)).schedule(anyString(), anyLong())
    }

    @Test
    fun cancelNotification_setsAlarmOff_andStopsElapsedUpdates() = runTest(testDispatcher) {
        viewModel.setTimeSelected(0)
        viewModel.startTimer(0)
        advanceUntilIdle()

        advanceTimeBy(1_100)
        advanceUntilIdle()
        val elapsedAfterTick = viewModel.elapsedTime.value
        assertNotNull(elapsedAfterTick)

        viewModel.cancelTimer()
        advanceUntilIdle()
        assertEquals(false, viewModel.isAlarmOn.value)

        val elapsedAfterCancel = viewModel.elapsedTime.value
        advanceTimeBy(5_000)
        advanceUntilIdle()
        assertEquals(elapsedAfterCancel, viewModel.elapsedTime.value)
    }

    @Test
    fun rapidStartCancelStart_leavesSingleScheduled_andAlarmOn() = runTest(testDispatcher) {
        viewModel.setTimeSelected(1)
        viewModel.startTimer(1)
        viewModel.cancelTimer()
        viewModel.startTimer(1)
        advanceUntilIdle()

        assertEquals(true, viewModel.isAlarmOn.value)
        val scheduled = runBlocking { repository.getAll().filter { it.status == TimerStatus.SCHEDULED } }
        assertEquals(1, scheduled.size)
        val cancelled = runBlocking { repository.getAll().filter { it.status == TimerStatus.CANCELLED } }
        assertEquals(1, cancelled.size)
    }

    @Test
    fun restoreTimers_marksExpiredFired_andKeepsEarliestFutureOnly() = runTest(testDispatcher) {
        val seededDao = InMemoryTimerDao()
        val seededRepo = TimerRepository(seededDao)
        val now = System.currentTimeMillis()
        runBlocking {
            seededRepo.save(TimerEntity("expired", now - 10_000, TimerStatus.SCHEDULED, "a"))
            seededRepo.save(TimerEntity("keep", now + 60_000, TimerStatus.SCHEDULED, "b"))
            seededRepo.save(TimerEntity("stale", now + 120_000, TimerStatus.SCHEDULED, "c"))
        }

        val vm = createViewModel(seededRepo)
        advanceUntilIdle()

        val all = runBlocking { seededRepo.getAll() }.associateBy { it.id }
        assertEquals(TimerStatus.FIRED, all["expired"]!!.status)
        assertEquals(TimerStatus.SCHEDULED, all["keep"]!!.status)
        assertEquals(TimerStatus.CANCELLED, all["stale"]!!.status)
        assertEquals(true, vm.isAlarmOn.value)
        verify(timerEngine).cancel(eq("stale"))
        closeViewModel(vm)
    }

    @Test
    fun onTimerRescheduledExternally_alignsElapsedWithNewTrigger() = runTest(testDispatcher) {
        viewModel.setTimeSelected(1)
        viewModel.startTimer(1)
        advanceUntilIdle()

        val id = runBlocking {
            repository.getAll().single { it.status == TimerStatus.SCHEDULED }.id
        }
        val newTrigger = System.currentTimeMillis() + 300_000L
        runBlocking {
            repository.withPersistenceLock {
                val t = repository.getById(id)!!
                repository.update(t.copy(triggerAtMillis = newTrigger))
            }
        }

        viewModel.onTimerRescheduledExternally(id)
        advanceUntilIdle()

        val elapsed = viewModel.elapsedTime.value ?: 0L
        assertTrue(elapsed in 295_000L..305_000L)
    }

    @Test
    fun setAlarm_turnsOnAlarm() = runTest(testDispatcher) {
        val observer: Observer<Boolean> = mock()
        viewModel.isAlarmOn.observeForever(observer)

        viewModel.setTimeSelected(1)
        viewModel.setAlarm(true)
        advanceUntilIdle()

        verify(observer).onChanged(true)
    }

    @Test
    fun setAlarm_turnsOffAlarm() = runTest(testDispatcher) {
        viewModel.setTimeSelected(1)
        viewModel.startTimer(1)
        advanceUntilIdle()

        val observer: Observer<Boolean> = mock()
        viewModel.isAlarmOn.observeForever(observer)

        viewModel.setAlarm(false)
        advanceUntilIdle()

        verify(observer).onChanged(false)
    }

    @Test
    fun startTimer_notifiesAndSchedules() = runTest(testDispatcher) {
        viewModel.setTimeSelected(1)
        viewModel.startTimer(1)
        advanceUntilIdle()

        verify(timerEngine).schedule(anyString(), anyLong())
        verify(notificationManager).cancelNotifications()
    }

    @Test
    fun cancelTimer_cancelsEngineAndClearsNotifications() = runTest(testDispatcher) {
        viewModel.setTimeSelected(1)
        viewModel.startTimer(1)
        advanceUntilIdle()
        viewModel.cancelTimer()
        advanceUntilIdle()

        verify(timerEngine).cancel(anyString())
        verify(notificationManager, times(1)).cancelNotifications()
    }

    private class InMemoryTimerDao : TimerDao {
        private val timers = mutableMapOf<String, TimerEntity>()

        override suspend fun getAll(): List<TimerEntity> = timers.values.toList()

        override suspend fun getById(timerId: String): TimerEntity? = timers[timerId]

        override fun observeTimers(): Flow<List<TimerEntity>> = emptyFlow()

        override suspend fun insert(timer: TimerEntity) {
            timers[timer.id] = timer
        }

        override suspend fun delete(id: String) {
            timers.remove(id)
        }
    }
}
