package com.typ.nabda.core.notifications.di

import com.typ.nabda.core.model.TelemetryRepository
import com.typ.nabda.core.model.TelemetryRepositoryStubImpl
import com.typ.nabda.core.notifications.NabdaNotificationManager
import com.typ.nabda.core.notifications.NabdaNotificationManagerImpl
import org.koin.dsl.module

val notificationsModule = module {
    single<NabdaNotificationManager> { NabdaNotificationManagerImpl(get()) }
    single<TelemetryRepository> { TelemetryRepositoryStubImpl() }
}
