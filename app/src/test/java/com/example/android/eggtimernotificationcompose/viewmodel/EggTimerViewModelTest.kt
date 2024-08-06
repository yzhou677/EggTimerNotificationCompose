package com.example.android.eggtimernotificationcompose.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.android.eggtimernotificationcompose.R
import com.example.android.eggtimernotificationcompose.di.CustomTimerPrefs
import com.example.android.eggtimernotificationcompose.di.LastEffectiveTimerSelectionPrefs
import com.example.android.eggtimernotificationcompose.di.Clock
import com.example.android.eggtimernotificationcompose.di.Timer
import com.example.android.eggtimernotificationcompose.util.cancelNotifications
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
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
    private lateinit var notifyPendingIntent: PendingIntent

    @Mock
    private lateinit var timer: Timer

    @Mock
    private lateinit var clock: Clock

    @Mock
    private lateinit var timerFactory: Timer.Factory

    private lateinit var viewModel: EggTimerViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        Dispatchers.setMain(testDispatcher)

        gson = Gson()

        // Mock the application resources
        `when`(application.resources).thenReturn(resources)
        `when`(resources.getStringArray(R.array.egg_array)).thenReturn(arrayOf("Soft", "Medium", "Hard"))
        `when`(resources.getIntArray(R.array.minutes_array)).thenReturn(intArrayOf(3, 4, 5))

        // Mock SharedPreferences.Editor
        `when`(lastEffectiveTimerSelectionPrefs.edit()).thenReturn(lastEffectiveTimerSelectionEditor)

        // Stub methods of SharedPreferences.Editor to return the editor itself for chaining
        `when`(lastEffectiveTimerSelectionEditor.putString(anyString(), anyString())).thenReturn(lastEffectiveTimerSelectionEditor)
        `when`(lastEffectiveTimerSelectionEditor.apply()).then { }

        // Mock the clock
        `when`(clock.elapsedRealtime()).thenReturn(1000L)

        `when`(timerFactory.create(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(timer)

        viewModel = EggTimerViewModel(
            application,
            alarmManager,
            customTimerPrefs,
            lastEffectiveTimerSelectionPrefs,
            gson,
            notificationManager,
            notifyPendingIntent,
            clock,
            timerFactory,
            true
        )
    }

    @Test
    fun setTimeSelected_updatesLiveData() {
        val observer: Observer<Int> = mock()
        viewModel.timeSelection.observeForever(observer)

        val timeSelection = 2
        viewModel.setTimeSelected(timeSelection)

        verify(observer).onChanged(timeSelection)
    }

    @Test
    fun setAlarm_turnsOnAlarm() = runBlockingTest {
        val observer: Observer<Boolean> = mock()
        viewModel.isAlarmOn.observeForever(observer)

        viewModel.setTimeSelected(1)
        viewModel.setAlarm(true)

        verify(observer).onChanged(true)
    }

    @Test
    fun setAlarm_turnsOffAlarm() = runBlockingTest {
        // Initialize timer before calling setAlarm(false)
        viewModel.setTimeSelected(1)
        viewModel.startTimer(1)

        val observer: Observer<Boolean> = mock()
        viewModel.isAlarmOn.observeForever(observer)

        viewModel.setAlarm(false)

        verify(observer).onChanged(false)
    }


    @Test
    fun startTimer_setsAlarmAndNotification() = runBlockingTest {
        viewModel.setTimeSelected(1)
        viewModel.startTimer(1)

        verify(alarmManager).setExact(
            eq(AlarmManager.ELAPSED_REALTIME_WAKEUP),
            anyLong(),
            eq(notifyPendingIntent)
        )
        verify(notificationManager).cancelNotifications()
    }

    @Test
    fun cancelTimer_resetsAlarmAndNotification() = runBlockingTest {
        viewModel.setTimeSelected(1)
        viewModel.startTimer(1)
        viewModel.cancelTimer()

        verify(alarmManager).cancel(eq(notifyPendingIntent))
        verify(notificationManager).cancelNotifications()
    }
}
