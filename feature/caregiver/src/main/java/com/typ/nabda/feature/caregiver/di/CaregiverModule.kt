package com.typ.nabda.feature.caregiver.di

import com.typ.nabda.core.messaging.TelemetryHandler
import com.typ.nabda.feature.caregiver.CaregiverTelemetryHandler
import com.typ.nabda.feature.caregiver.CaregiverViewModel
import com.typ.nabda.feature.caregiver.LocationGeocoder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val caregiverFeatureModule = module {
    single { LocationGeocoder(androidContext()) }
    single<TelemetryHandler> { CaregiverTelemetryHandler(get(), get()) }
    viewModel { CaregiverViewModel(get(), get(), get(), get()) }
}
