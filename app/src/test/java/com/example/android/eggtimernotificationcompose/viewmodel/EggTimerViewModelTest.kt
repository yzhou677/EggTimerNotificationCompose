package com.example.android.eggtimernotificationcompose.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.android.eggtimernotificationcompose.R
import com.example.android.eggtimernotificationcompose.data.TimerRepository
import com.example.android.eggtimernotificationcompose.di.CustomTimerPrefs
import com.example.android.eggtimernotificationcompose.di.LastEffectiveTimerSelectionPrefs
import com.example.android.eggtimernotificationcompose.engine.TimerEngine
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlinx.coroutines.runBlocking

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
    private lateinit var timerEngine: TimerEngine

    @Mock
    private lateinit var repository: TimerRepository

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

        runBlocking {
            whenever(repository.getAll()).thenReturn(emptyList())
        }

        viewModel = EggTimerViewModel(
            application,
            alarmManager,
            customTimerPrefs,
            lastEffectiveTimerSelectionPrefs,
            gson,
            notificationManager,
            timerEngine,
            repository,
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

        verify(timerEngine).schedule(anyString(), anyLong())
        verify(notificationManager).cancelNotifications()
    }

    @Test
    fun cancelTimer_resetsAlarmAndNotification() = runBlockingTest {
        viewModel.setTimeSelected(1)
        viewModel.startTimer(1)
        viewModel.cancelTimer()

        verify(timerEngine).cancel(anyString())
        verify(notificationManager).cancelNotifications()
    }
}
