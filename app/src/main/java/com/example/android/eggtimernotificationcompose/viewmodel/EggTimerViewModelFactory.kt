package com.example.android.eggtimernotificationcompose.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EggTimerViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EggTimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EggTimerViewModel.getInstance(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}