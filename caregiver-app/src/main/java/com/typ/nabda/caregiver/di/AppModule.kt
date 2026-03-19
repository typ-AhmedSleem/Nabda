package com.typ.nabda.caregiver.di

import com.typ.nabda.caregiver.pairing.WifiPairingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::WifiPairingViewModel)
}
