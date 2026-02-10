package com.typ.nabda.feature.caregiver.di

import com.typ.nabda.feature.caregiver.CaregiverViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val caregiverFeatureModule = module {
    viewModel { CaregiverViewModel(get()) }
}
