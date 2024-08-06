package com.example.android.eggtimernotificationcompose.di

import android.content.Context
import android.widget.Toast

interface ToastProvider {
    fun showToast(message: String, duration: Int)
}

class AndroidToastProvider(private val context: Context) : ToastProvider {
    override fun showToast(message: String, duration: Int) {
        Toast.makeText(context, message, duration).show()
    }
}