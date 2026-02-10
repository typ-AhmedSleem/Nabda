package com.typ.nabda

import android.app.Application
import com.typ.nabda.core.dispatcher.di.dispatcherModule
import com.typ.nabda.core.notifications.di.notificationsModule
import com.typ.nabda.feature.caregiver.di.caregiverFeatureModule
import com.typ.nabda.feature.deafblind.di.deafBlindFeatureModule
import com.typ.nabda.feature.pairing.di.pairingFeatureModule
import com.typ.nabda.infrastructure.fcm.di.fcmModule
import com.typ.nabda.infrastructure.storage.di.storageModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NabdaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

//        initializeFirebase(this)

        startKoin {
            androidContext(this@NabdaApplication)
            modules(
                notificationsModule,
                storageModule,
                fcmModule,
                pairingFeatureModule,
                dispatcherModule,
                deafBlindFeatureModule,
                caregiverFeatureModule
            )
        }
    }
}
