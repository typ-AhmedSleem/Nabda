package com.typ.nabda.infrastructure.fcm.di

import com.typ.nabda.core.messaging.MessageSender
import com.typ.nabda.infrastructure.fcm.FcmSender
import org.koin.dsl.module

val fcmModule = module {
    // Provide Server Key (TODO: Move to safe place, using placeholder for now)
    single(org.koin.core.qualifier.named("FCM_SERVER_KEY")) { "YOUR_SERVER_KEY_HERE" }

    single<MessageSender> { FcmSender(get(org.koin.core.qualifier.named("FCM_SERVER_KEY"))) }
}
