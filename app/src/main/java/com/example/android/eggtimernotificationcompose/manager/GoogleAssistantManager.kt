package com.example.android.eggtimernotificationcompose.manager

import android.content.Context
import android.content.res.Resources
import android.widget.Toast
import com.example.android.eggtimernotificationcompose.BuildConfig
import com.example.android.eggtimernotificationcompose.R

interface TimerAction {
    fun startTimer(timerLengthSelection: Int)
    fun updateLiveDataForTimerStartAction(timerLengthSelection: Int)
    fun cancelTimer()
}

object GoogleAssistantManager {
    fun startTimerThroughGoogleAssistant(
        softnessLevel: String,
        resources: Resources,
        timerAction: TimerAction,
        context: Context
    ) {
        var timeSelection = resources.getStringArray(R.array.egg_array).indexOf(softnessLevel)
        if (BuildConfig.IS_TESTING) timeSelection++

        if (timeSelection != -1) {
            timerAction.startTimer(timeSelection)
            timerAction.updateLiveDataForTimerStartAction(timeSelection)
        } else {
            showInvalidSoftnessLevelError(resources, context)
        }
    }

    fun showInvalidSoftnessLevelError(resources: Resources, context: Context) {
        val supportedSoftnessLevels = resources.getStringArray(R.array.egg_array).joinToString(", ")
        val message = "Invalid softness level. Please choose from: $supportedSoftnessLevels"

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

