package com.example.android.eggtimernotificationcompose.util

import android.text.format.DateUtils

/**
 * Converts milliseconds to formatted mm:ss
 *
 * @param value, time in milliseconds.
 */
fun setElapsedTime(value: Long): String {
    val seconds = value / 1000
    return DateUtils.formatElapsedTime(seconds)
}

