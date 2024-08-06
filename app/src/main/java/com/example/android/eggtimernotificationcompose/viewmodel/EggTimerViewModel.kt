package com.example.android.eggtimernotificationcompose.viewmodel

import android.app.*
import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.AlarmManagerCompat
import androidx.lifecycle.*
import com.example.android.eggtimernotificationcompose.R
import com.example.android.eggtimernotificationcompose.di.CustomTimerPrefs
import com.example.android.eggtimernotificationcompose.di.LastEffectiveTimerSelectionPrefs
import com.example.android.eggtimernotificationcompose.manager.TimerAction
import com.example.android.eggtimernotificationcompose.model.CustomTimer
import com.example.android.eggtimernotificationcompose.di.Clock
import com.example.android.eggtimernotificationcompose.di.Timer
import com.example.android.eggtimernotificationcompose.util.cancelNotifications
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EggTimerViewModel @Inject constructor(
    app: Application,
    private val alarmManager: AlarmManager,
    @CustomTimerPrefs private val customTimerPrefs: SharedPreferences,
    @LastEffectiveTimerSelectionPrefs private val lastEffectiveTimerSelectionPrefs: SharedPreferences,
    private val gson: Gson,
    private val notificationManager: NotificationManager,
    private val notifyPendingIntent: PendingIntent,
    private val clock: Clock,
    private val timerFactory: Timer.Factory,
    isTesting: Boolean
) : AndroidViewModel(app), TimerAction {
    private val minute: Long = 60_000L
    private val second: Long = 1_000L

    private var timerLengthOptions: MutableList<Int>
    private val customTimers: MutableList<CustomTimer> = mutableListOf()
    internal val defaultEggTimerOptionsSize: Int

    private val _timeSelection = MutableLiveData<Int>().apply { value = 0 }
    val timeSelection: LiveData<Int>
        get() = _timeSelection

    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long>
        get() = _elapsedTime

    private var _alarmOn = MutableLiveData<Boolean>()
    val isAlarmOn: LiveData<Boolean>
        get() = _alarmOn

    private var _eggTimerItems = MutableLiveData<List<String>>()
    val eggTimerItems: LiveData<List<String>>
        get() = _eggTimerItems

    private lateinit var timer: Timer

    init {
        _alarmOn.value = false

        loadLastEffectiveTimerSelection(app)
        loadCustomTimers(app)

        val eggTimerItems = app.resources.getStringArray(R.array.egg_array).toMutableList()
        if (isTesting) {
            eggTimerItems.add(0, app.getString(R.string.egg_item_for_testing))
        }
        this.defaultEggTimerOptionsSize = eggTimerItems.size

        customTimers.forEach {
            eggTimerItems.add(it.label)
        }
        _eggTimerItems.value = eggTimerItems

        val timerLengthOptions = app.resources.getIntArray(R.array.minutes_array).toMutableList()
        if (isTesting) {
            timerLengthOptions.add(0, 0)
        }

        customTimers.forEach {
            timerLengthOptions += it.minutes
        }
        this.timerLengthOptions = timerLengthOptions
    }

    /**
     * Turns on or off the alarm
     *
     * @param isChecked, alarm status to be set.
     */
    fun setAlarm(isChecked: Boolean) {
        when (isChecked) {
            true -> timeSelection.value?.let { startTimer(it) }
            false -> cancelNotification()
        }
    }

    /**
     * Sets the desired interval for the alarm
     *
     * @param timerLengthSelection, interval timerLengthSelection value.
     */
    fun setTimeSelected(timerLengthSelection: Int) {
        _timeSelection.value = timerLengthSelection
    }

    /**
     * Creates a new alarm, notification and timer
     *
     * @param timerLengthSelection, interval timerLengthSelection value.
     */
    override fun startTimer(timerLengthSelection: Int) {
        _alarmOn.value?.let {
            if (!it) {
                _alarmOn.value = true

                saveEffectiveTimerSelection(timerLengthSelection)

                val selectedInterval = when (timerLengthSelection) {
                    0 -> second * 10 // For testing only
                    else -> timerLengthOptions[timerLengthSelection] * minute
                }
                val triggerTime = clock.elapsedRealtime() + selectedInterval

                // call cancel notification
                notificationManager.cancelNotifications()

                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    notifyPendingIntent
                )

                createTimer(triggerTime)
            }
        }
    }

    /**
     * Updates the LiveData for timer start action
     *
     * @param timerLengthSelection, interval timerLengthSelection value.
     */
    override fun updateLiveDataForTimerStartAction(timerLengthSelection: Int) {
        _timeSelection.value = timerLengthSelection
        _alarmOn.value = true
    }

    /**
     * Updates the LiveData for timer cancel action
     */
    override fun cancelTimer() {
        cancelNotification()
    }

    /**
     * Creates a new timer
     *
     * @param triggerTime, future trigger time in milliseconds.
     */
    private fun createTimer(triggerTime: Long) {
        timer = timerFactory.create(triggerTime - clock.elapsedRealtime(), 1000L,
            {
                _elapsedTime.value = triggerTime - clock.elapsedRealtime()
                if (_elapsedTime.value!! <= 0) {
                    resetTimer()
                }
            },
            {
                resetTimer()
            }
        )
        timer.start()
    }


    /**
     * Cancels the alarm, notification and resets the timer
     */
    private fun cancelNotification() {
        resetTimer()
        alarmManager.cancel(notifyPendingIntent)
    }

    /**
     * Resets the timer on screen and sets alarm value false
     */
    private fun resetTimer() {
        if (::timer.isInitialized) {
            timer.cancel()
        }
        _elapsedTime.value = 0
        _alarmOn.value = false
    }

    /**
     * Saves custom timer inputted from dialog to SharedPreferences
     *
     * @param customItem, custom timer inputted from dialog.
     */
    fun saveCustomTimers(customItem: CustomTimer) {
        customTimers.add(customItem)

        timerLengthOptions.add(customItem.minutes)
        val updatedItems = _eggTimerItems.value.orEmpty().toMutableList().apply {
            add(customItem.label)
        }
        _eggTimerItems.value = updatedItems

        addToSharedPreferences(customTimers)
    }

    /**
     * Loads custom timers from SharedPreferences
     *
     * @param context, application context.
     */
    private fun loadCustomTimers(context: Context) {
        val json = customTimerPrefs.getString("customTimers", null)

        if (json != null) {
            val type = object : TypeToken<List<CustomTimer>>() {}.type
            val customTimers: List<CustomTimer> = gson.fromJson(json, type)
            this.customTimers.addAll(customTimers)
        }
    }

    /**
     * Deletes custom timer at the specified index
     *
     * @param index, index of the custom timer to be deleted.
     */
    fun deleteCustomTimer(index: Int) {
        val updatedItems = _eggTimerItems.value.orEmpty().toMutableList().apply {
            removeAt(index)
        }
        _eggTimerItems.value = updatedItems
        customTimers.removeAt(index - defaultEggTimerOptionsSize)
        timerLengthOptions.removeAt(index)

        addToSharedPreferences(customTimers)

        if (_timeSelection.value == index) {
            _timeSelection.value = 0
            saveEffectiveTimerSelection()
        }
    }

    /**
     * Adds custom timers to SharedPreferences
     *
     * @param customTimers, list of custom timers to be added.
     */
    private fun addToSharedPreferences(customTimers: List<CustomTimer>) {
        val editor = customTimerPrefs.edit()
        val json = gson.toJson(customTimers)

        editor.putString("customTimers", json)
        editor.apply()
    }

    /**
     * Saves the last effective timer selection to SharedPreferences
     */
    private fun saveEffectiveTimerSelection(timerLengthSelection: Int? = null) {
        val editor = lastEffectiveTimerSelectionPrefs.edit()
        var json = gson.toJson(_timeSelection.value)

        if (timerLengthSelection != null) json = gson.toJson(timerLengthSelection)

        editor.putString("lastEffectiveTimerSelection", json)
        editor.apply()
    }

    /**
     * Loads the last effective timer selection from SharedPreferences
     */
    private fun loadLastEffectiveTimerSelection(context: Context) {
        val json = lastEffectiveTimerSelectionPrefs.getString("lastEffectiveTimerSelection", null)

        if (json != null) {
            val type = object : TypeToken<Int>() {}.type
            val lastEffectiveTimerSelection: Int = gson.fromJson(json, type)
            _timeSelection.value = lastEffectiveTimerSelection
        } else {
            // Handle null case here, e.g., setting a default value
            _timeSelection.value = 0
        }
    }
}