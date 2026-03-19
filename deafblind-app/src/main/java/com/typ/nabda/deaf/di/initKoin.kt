package com.typ.nabda.deaf.di

import android.content.Context
import com.typ.nabda.core.haptic.di.hapticModule
import com.typ.nabda.core.location.di.locationModule
import com.typ.nabda.core.notifications.di.notificationsModule
import com.typ.nabda.feature.deafblind.di.deafBlindFeatureModule
import com.typ.nabda.feature.pairing.di.pairingFeatureModule
import com.typ.nabda.infrastructure.storage.di.storageModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

fun initKoin(ctx: Context) {
    try {
        startKoin {
            androidContext(ctx)
            modules(
                notificationsModule,
                locationModule,
                storageModule,
                pairingFeatureModule,
                deafBlindFeatureModule,
                hapticModule
            )
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}