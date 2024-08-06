package com.example.android.eggtimernotificationcompose.di

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.media.RingtoneManager
import android.net.Uri
import com.example.android.eggtimernotificationcompose.BuildConfig
import com.example.android.eggtimernotificationcompose.manager.FireBaseManager
import com.example.android.eggtimernotificationcompose.manager.FireBaseManagerInterface
import com.example.android.eggtimernotificationcompose.manager.GoogleAssistantManager
import com.example.android.eggtimernotificationcompose.manager.NotificationChannelManager
import com.example.android.eggtimernotificationcompose.receiver.AlarmReceiver
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideRingtoneUri(): Uri {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    }

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    @Singleton
    fun provideNotificationChannelManager(
        @ApplicationContext context: Context,
        ringtoneUri: Uri,
        notificationManager: NotificationManager
    ): NotificationChannelManager {
        return NotificationChannelManager(context, ringtoneUri, notificationManager)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFireBaseManager(db: FirebaseFirestore, logger: Logger): FireBaseManagerInterface {
        return FireBaseManager(db, logger)
    }

    @Provides
    @Singleton
    fun provideResources(@ApplicationContext context: Context): Resources {
        return context.resources
    }

    @Provides
    @Singleton
    fun provideGoogleAssistantManager(
        resources: Resources,
        toastProvider: ToastProvider,
        isTesting: Boolean
    ): GoogleAssistantManager {
        return GoogleAssistantManager(resources, toastProvider, isTesting)
    }

    @Provides
    @Singleton
    @CustomTimerPrefs
    fun provideCustomTimerSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("CustomTimerPrefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    @LastEffectiveTimerSelectionPrefs
    fun provideLastEffectiveTimerSelectionSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("LastEffectiveTimerSelection", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson{
        return Gson()
    }

    @Provides
    fun providePendingIntent(@ApplicationContext context: Context): PendingIntent {
        val notifyIntent = Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @Provides
    @Singleton
    fun provideClock(): Clock {
        return SystemClockImpl()
    }

    @Provides
    @Singleton
    fun provideTimerFactory(): Timer.Factory {
        return object : Timer.Factory {
            override fun create(
                millisInFuture: Long,
                countDownInterval: Long,
                onTick: (Long) -> Unit,
                onFinish: () -> Unit
            ): Timer {
                return DefaultTimer(millisInFuture, countDownInterval, onTick, onFinish)
            }
        }
    }

    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return RealLogger() // Or provide a mock for testing
    }

    @Provides
    @Singleton
    fun provideToastProvider(@ApplicationContext context: Context): ToastProvider {
        return AndroidToastProvider(context)
    }

    @Provides
    @Singleton
    fun provideIsTesting(): Boolean {
        return BuildConfig.IS_TESTING
    }
}