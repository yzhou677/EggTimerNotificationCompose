package com.example.android.eggtimernotificationcompose.util

import java.util.Locale

/**
 * Checks if the device's locale is Spanish
 */
fun isSpanishLocale(): Boolean {
    val locale = Locale.getDefault()
    return Regex("^es").containsMatchIn(locale.language)
}