package com.typ.nabda

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NabdaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@NabdaApplication)
            modules(
                com.typ.nabda.core.notifications.di.notificationsModule,
                com.typ.nabda.infrastructure.storage.di.storageModule,
                com.typ.nabda.infrastructure.fcm.di.fcmModule
            )
        }
    }
}
