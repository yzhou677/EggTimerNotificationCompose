package com.example.android.eggtimernotificationcompose.manager

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import com.example.android.eggtimernotificationcompose.R

object EggTimerShortcutManager {
    fun createEggTimerShortcut(context: Context, softnessLevel: String) {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("eggtimer://eggtimer.com/starteggtimer?softness_level=$softnessLevel")
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val shortcut = ShortcutInfo.Builder(context, "egg_timer_$softnessLevel")
            .setShortLabel(softnessLevel)
            .setLongLabel("Start Egg Timer for $softnessLevel")
            .setIcon(Icon.createWithResource(context, R.drawable.egg_icon))
            .setIntent(intent)
            .build()

        shortcutManager.requestPinShortcut(shortcut, null)
    }
}