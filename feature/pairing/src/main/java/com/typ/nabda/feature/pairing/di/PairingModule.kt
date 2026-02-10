package com.typ.nabda.feature.pairing.di

import com.typ.nabda.feature.pairing.PairingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pairingFeatureModule = module {
    viewModel { PairingViewModel(get(), get()) }
}
