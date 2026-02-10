package com.typ.nabda.feature.deafblind.di

import com.typ.nabda.feature.deafblind.DeafBlindViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val deafBlindFeatureModule = module {
    viewModel { DeafBlindViewModel(get()) }
}
