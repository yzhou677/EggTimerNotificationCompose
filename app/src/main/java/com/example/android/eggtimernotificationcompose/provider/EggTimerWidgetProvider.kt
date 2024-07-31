package com.example.android.eggtimernotificationcompose.provider

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.android.eggtimernotificationcompose.R

class EggTimerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_eggtimer)

        views.setImageViewResource(R.id.widget_background, R.drawable.egg_notification)

        val eggArray = context.resources.getStringArray(R.array.egg_array)
        val containerId = R.id.button_container

        views.removeAllViews(containerId)

        eggArray.forEach { softnessLevel ->
            val button = RemoteViews(context.packageName, R.layout.widget_button)
            button.setTextViewText(R.id.button_text, softnessLevel)

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("eggtimer://eggtimer.com/starteggtimer?softness_level=$softnessLevel"))
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            button.setOnClickPendingIntent(R.id.button_text, pendingIntent)

            views.addView(containerId, button)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}