package com.example.android.eggtimernotificationcompose.manager

import android.content.Context
import android.content.res.Resources
import android.widget.Toast
import com.example.android.eggtimernotificationcompose.BuildConfig
import com.example.android.eggtimernotificationcompose.R
import javax.inject.Inject
import javax.inject.Singleton

interface TimerAction {
    fun startTimer(timerLengthSelection: Int)
    fun updateLiveDataForTimerStartAction(timerLengthSelection: Int)
    fun cancelTimer()
}

@Singleton
class GoogleAssistantManager @Inject constructor(
    private val resources: Resources,
    private val context: Context
) {
    fun startTimerThroughGoogleAssistant(
        softnessLevel: String,
        timerAction: TimerAction,
    ) {
        var timeSelection = resources.getStringArray(R.array.egg_array).indexOf(softnessLevel)
        if (BuildConfig.IS_TESTING) timeSelection++

        if (timeSelection != -1) {
            timerAction.startTimer(timeSelection)
            timerAction.updateLiveDataForTimerStartAction(timeSelection)
        } else {
            showInvalidSoftnessLevelError()
        }
    }

    fun showInvalidSoftnessLevelError() {
        val supportedSoftnessLevels = resources.getStringArray(R.array.egg_array).joinToString(", ")
        val message = "Invalid softness level. Please choose from: $supportedSoftnessLevels"

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

