package com.typ.nabda.core.haptic.di

import com.typ.nabda.core.haptic.AndroidHapticEngine2
import com.typ.nabda.core.haptic.HapticEngine
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val hapticModule = module {
    single<HapticEngine> { AndroidHapticEngine2(androidContext()) }
}
