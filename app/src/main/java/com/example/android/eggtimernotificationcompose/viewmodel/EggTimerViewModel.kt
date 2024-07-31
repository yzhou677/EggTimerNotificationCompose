package com.example.android.eggtimernotificationcompose.viewmodel

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.android.eggtimernotificationcompose.BuildConfig
import com.example.android.eggtimernotificationcompose.R
import com.example.android.eggtimernotificationcompose.manager.TimerAction
import com.example.android.eggtimernotificationcompose.model.CustomTimer
import com.example.android.eggtimernotificationcompose.receiver.AlarmReceiver
import com.example.android.eggtimernotificationcompose.util.cancelNotifications
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.*

class EggTimerViewModel(private val app: Application) : AndroidViewModel(app), TimerAction {
    companion object {
        @Volatile
        private var INSTANCE: EggTimerViewModel? = null

        fun getInstance(app: Application): EggTimerViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EggTimerViewModel(app).also { INSTANCE = it }
            }
        }
    }

    private val REQUEST_CODE = 0
    private val isTesting = BuildConfig.IS_TESTING

    private val minute: Long = 60_000L
    private val second: Long = 1_000L

    private var timerLengthOptions: MutableList<Int>
    private val notifyPendingIntent: PendingIntent
    private val customTimers: MutableList<CustomTimer> = mutableListOf()
    internal val defaultEggTimerOptionsSize: Int

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notifyIntent = Intent(app, AlarmReceiver::class.java)

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

    private lateinit var timer: CountDownTimer

    init {
        _alarmOn.value = false

        notifyPendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            REQUEST_CODE,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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

                saveEffectiveTimerSelection()

                val selectedInterval = when (timerLengthSelection) {
                    0 -> second * 10 // For testing only
                    else -> timerLengthOptions[timerLengthSelection] * minute
                }
                val triggerTime = SystemClock.elapsedRealtime() + selectedInterval

                // get an instance of NotificationManager and call sendNotification
                val notificationManager =
                    ContextCompat.getSystemService(
                        app,
                        NotificationManager::class.java
                    ) as NotificationManager

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
        viewModelScope.launch {
            timer = object : CountDownTimer(triggerTime, second) {
                override fun onTick(millisUntilFinished: Long) {
                    _elapsedTime.value = triggerTime - SystemClock.elapsedRealtime()
                    if (_elapsedTime.value!! <= 0) {
                        resetTimer()
                    }
                }

                override fun onFinish() {
                    resetTimer()
                }
            }
            timer.start()
        }
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
        timer.cancel()
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
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("CustomTimerPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("customTimers", null)

        if (json != null) {
            val type = object : TypeToken<List<CustomTimer>>() {}.type
            val customTimers: List<CustomTimer> = Gson().fromJson(json, type)
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
        val sharedPreferences: SharedPreferences =
            app.getSharedPreferences("CustomTimerPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(customTimers)

        editor.putString("customTimers", json)
        editor.apply()
    }

    /**
     * Saves the last effective timer selection to SharedPreferences
     */
    private fun saveEffectiveTimerSelection() {
        val sharedPreferences: SharedPreferences =
            app.getSharedPreferences("LastEffectiveTimerSelection", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(_timeSelection.value)

        editor.putString("lastEffectiveTimerSelection", json)
        editor.apply()
    }

    /**
     * Loads the last effective timer selection from SharedPreferences
     */
    private fun loadLastEffectiveTimerSelection(context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("LastEffectiveTimerSelection", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("lastEffectiveTimerSelection", null)

        if (json != null) {
            val type = object : TypeToken<Int>() {}.type
            val lastEffectiveTimerSelection: Int = Gson().fromJson(json, type)
            _timeSelection.value = lastEffectiveTimerSelection
        }
    }
}