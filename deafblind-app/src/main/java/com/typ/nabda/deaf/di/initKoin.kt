package com.typ.nabda.deaf.di

import android.content.Context
import com.typ.nabda.core.dispatcher.di.dispatcherModule
import com.typ.nabda.core.notifications.di.notificationsModule
import com.typ.nabda.feature.deafblind.di.deafBlindFeatureModule
import com.typ.nabda.feature.pairing.di.pairingFeatureModule
import com.typ.nabda.infrastructure.fcm.di.fcmModule
import com.typ.nabda.infrastructure.storage.di.storageModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

fun initKoin(ctx: Context) {
    try {
        startKoin {
            androidContext(ctx)
            modules(
                notificationsModule,
                storageModule,
                fcmModule,
                pairingFeatureModule,
                dispatcherModule,
                deafBlindFeatureModule,
            )
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}