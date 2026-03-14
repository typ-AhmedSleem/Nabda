package com.typ.nabda.feature.caregiver.di

import com.typ.nabda.core.messaging.TelemetryHandler
import com.typ.nabda.feature.caregiver.CaregiverTelemetryHandler
import com.typ.nabda.feature.caregiver.CaregiverViewModel
import com.typ.nabda.feature.caregiver.LocationGeocoder
import com.typ.nabda.feature.caregiver.localclient.DeviceDiscoveryManager
import com.typ.nabda.feature.caregiver.localclient.HeartbeatPoller
import com.typ.nabda.feature.caregiver.localclient.LocalHttpTransport
import com.typ.nabda.infrastructure.localnetwork.transport.TelemetryTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val caregiverFeatureModule = module {
    single { LocationGeocoder(androidContext()) }
    single<TelemetryHandler> { CaregiverTelemetryHandler(get(), get()) }

    // Local Networking Components
    single { DeviceDiscoveryManager(androidContext()) }
    single<TelemetryTransport> {
        LocalHttpTransport(
            baseUrlProvider = { get<DeviceDiscoveryManager>().discoveredHost.value }
        )
    }
    single {
        HeartbeatPoller(
            transport = get(),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
    }

    viewModel {
        CaregiverViewModel(
            notificationManager = get(),
            telemetryRepository = get(),
            pairingRepository = get(),
            geocoder = get(),
            discoveryManager = get(),
            heartbeatPoller = get(),
            transport = get()
        )
    }
}
