package com.typ.nabda.core.location.di

import com.typ.nabda.core.location.NabdaLocationManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val locationModule = module {
    singleOf(::NabdaLocationManager)
}