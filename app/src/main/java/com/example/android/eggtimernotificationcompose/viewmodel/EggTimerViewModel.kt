package com.example.android.eggtimernotificationcompose.viewmodel

import android.app.*
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import com.example.android.eggtimernotificationcompose.R
import com.example.android.eggtimernotificationcompose.data.TimerRepository
import com.example.android.eggtimernotificationcompose.di.CustomTimerPrefs
import com.example.android.eggtimernotificationcompose.di.LastEffectiveTimerSelectionPrefs
import com.example.android.eggtimernotificationcompose.manager.TimerAction
import com.example.android.eggtimernotificationcompose.model.CustomTimer
import com.example.android.eggtimernotificationcompose.engine.TimerEngine
import com.example.android.eggtimernotificationcompose.model.TimerEntity
import com.example.android.eggtimernotificationcompose.model.TimerStatus
import com.example.android.eggtimernotificationcompose.util.cancelNotifications
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EggTimerViewModel @Inject constructor(
    app: Application,
    private val alarmManager: AlarmManager,
    @CustomTimerPrefs private val customTimerPrefs: SharedPreferences,
    @LastEffectiveTimerSelectionPrefs private val lastEffectiveTimerSelectionPrefs: SharedPreferences,
    private val gson: Gson,
    private val notificationManager: NotificationManager,
    private val timerEngine: TimerEngine,
    private val repository: TimerRepository,
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

    private var countdownJob: Job? = null

    private var currentTimerId: String? = null

    /** Serializes full start flows so two rapid starts cannot interleave countdown vs cancel. */
    private val startTimerSequenceMutex = Mutex()

    init {
        _alarmOn.value = false

        restoreTimers()

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
        // Always schedule here. External entry points (e.g. widget / Assistant) may call
        // updateLiveDataForTimerStartAction first, which must not block this path via _alarmOn.
        _alarmOn.value = true

        saveEffectiveTimerSelection(timerLengthSelection)

        val selectedInterval = when (timerLengthSelection) {
            0 -> second * 10 // For testing only
            else -> timerLengthOptions[timerLengthSelection] * minute
        }
        val triggerAtMillis = System.currentTimeMillis() + selectedInterval
        val timerId = UUID.randomUUID().toString()

        val label = _eggTimerItems.value?.get(timerLengthSelection) ?: "Timer"

        val timer = TimerEntity(
            id = timerId,
            triggerAtMillis = triggerAtMillis,
            status = TimerStatus.SCHEDULED,
            label = label
        )

        // call cancel notification
        notificationManager.cancelNotifications()

        viewModelScope.launch {
            startTimerSequenceMutex.withLock {
                cancelCountdownJob()
                repository.withPersistenceLock {
                    cancelCurrentTimerLocked()
                    currentTimerId = timerId
                    repository.save(timer)
                    timerEngine.schedule(timerId, triggerAtMillis)
                }
                startCountdownLoop(triggerAtMillis)
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
    }

    /**
     * Updates the LiveData for timer cancel action
     */
    override fun cancelTimer() {
        cancelNotification()
    }

    /**
     * Restores persisted timers from the database and reconciles them with the current system time.
     *
     * For each stored timer:
     * - If the timer is not in SCHEDULED state, it is ignored.
     * - If the timer has already expired, it is marked as FIRED in the database.
     * - If the timer is still valid, it is rescheduled using TimerEngine
     *   and the UI countdown is restored.
     */
    fun restoreTimers() {
        viewModelScope.launch {
            val toResume = repository.withPersistenceLock {
                val timers = repository.getAll()
                val now = System.currentTimeMillis()
                val scheduled = timers.filter { it.status == TimerStatus.SCHEDULED }
                val (expired, future) = scheduled.partition { it.triggerAtMillis <= now }

                expired.forEach { timer ->
                    repository.update(timer.copy(status = TimerStatus.FIRED))
                }

                if (future.isEmpty()) {
                    return@withPersistenceLock null
                }

                val keep = future.minByOrNull { it.triggerAtMillis }!!
                future.filter { it.id != keep.id }.forEach { stale ->
                    timerEngine.cancel(stale.id)
                    repository.update(stale.copy(status = TimerStatus.CANCELLED))
                }
                keep
            }

            if (toResume != null) {
                timerEngine.schedule(toResume.id, toResume.triggerAtMillis)
                currentTimerId = toResume.id
                _alarmOn.value = true
                startCountdownLoop(toResume.triggerAtMillis)
            }
        }
    }

    /**
     * Called when [SnoozeReceiver] (or similar) updates [TimerEntity.triggerAtMillis] in the DB.
     * Keeps the countdown aligned with Room without a Flow refactor.
     */
    fun onTimerRescheduledExternally(timerId: String) {
        viewModelScope.launch {
            startTimerSequenceMutex.withLock {
                val entity = repository.getById(timerId) ?: return@withLock
                if (entity.status != TimerStatus.SCHEDULED) return@withLock
                cancelCountdownJob()
                currentTimerId = timerId
                _alarmOn.value = true
                startCountdownLoop(entity.triggerAtMillis)
            }
        }
    }

    private fun cancelCountdownJob() {
        countdownJob?.cancel()
        countdownJob = null
    }

    /**
     * Drives UI from wall-clock time so countdown stays correct across background/foreground.
     */
    private fun startCountdownLoop(triggerAtMillis: Long) {
        cancelCountdownJob()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                val remaining = triggerAtMillis - System.currentTimeMillis()
                if (remaining <= 0L) {
                    resetUiAfterCountdownFinished()
                    break
                }
                _elapsedTime.value = remaining
                delay(1000L)
            }
        }
    }

    private fun resetUiAfterCountdownFinished() {
        cancelCountdownJob()
        _elapsedTime.value = 0L
        _alarmOn.value = false
    }

    private suspend fun cancelCurrentTimerLocked() {
        currentTimerId?.let { id ->
            timerEngine.cancel(id)
            val timers = repository.getAll()
            timers.find { it.id == id }?.let {
                repository.update(it.copy(status = TimerStatus.CANCELLED))
            }
        }
        currentTimerId = null
    }

    /**
     * Cancels the alarm, notification and resets the timer
     */
    private fun cancelNotification() {
        viewModelScope.launch {
            startTimerSequenceMutex.withLock {
                repository.withPersistenceLock {
                    cancelCurrentTimerLocked()
                }
                // After persistence: stop any countdown [startTimer] may have started while this
                // coroutine was waiting — avoids UI counting down with no scheduled alarm.
                cancelCountdownJob()
                _elapsedTime.value = 0L
                _alarmOn.value = false
            }
        }
    }

    override fun onCleared() {
        cancelCountdownJob()
        super.onCleared()
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