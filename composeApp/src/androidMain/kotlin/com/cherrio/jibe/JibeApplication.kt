package com.cherrio.jibe

import android.app.Application
import com.cherrio.jibe.di.Di
import com.cherrio.jibe.features.AndroidClipboardProvider

class JibeApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        val androidClipboardProvider = AndroidClipboardProvider(applicationContext)
        Di.injectClipboardProvider(androidClipboardProvider)
    }
}