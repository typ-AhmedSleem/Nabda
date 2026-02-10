package com.typ.nabda

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NabdaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@NabdaApplication)
            // modules() // Will add modules later
        }
    }
}
