package com.typ.nabda.infrastructure.fcm.di

import com.typ.nabda.core.messaging.MessageSender
import com.typ.nabda.core.messaging.TelemetrySender
import com.typ.nabda.infrastructure.fcm.FcmSender
import com.typ.nabda.infrastructure.fcm.telemetry.TelemetryCollector
import com.typ.nabda.infrastructure.fcm.telemetry.TelemetryScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val fcmModule = module {
    // Provide Server Key (TODO: Move to safe place, using placeholder for now)
    single(org.koin.core.qualifier.named("FCM_SERVER_KEY")) { "YOUR_SERVER_KEY_HERE" }

    single { FcmSender(get(org.koin.core.qualifier.named("FCM_SERVER_KEY"))) }
    single<MessageSender> { get<FcmSender>() }
    single<TelemetrySender> { get<FcmSender>() }

    single { TelemetryCollector(androidContext()) }
    single { TelemetryScheduler() }
}
