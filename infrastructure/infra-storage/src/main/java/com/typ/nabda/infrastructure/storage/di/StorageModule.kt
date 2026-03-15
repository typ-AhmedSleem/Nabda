package com.typ.nabda.infrastructure.storage.di

import com.typ.nabda.core.messaging.TokenRepository
import com.typ.nabda.core.pairing.PairingRepository
import com.typ.nabda.infrastructure.storage.PairingRepositoryImpl
import com.typ.nabda.infrastructure.storage.TokenRepositoryImpl
import org.koin.dsl.module

val storageModule = module {

    // PairingRepository
    single<PairingRepository> { PairingRepositoryImpl(get()) }

    // TokenRepository
    single<TokenRepository> { TokenRepositoryImpl(get(), get()) }

    // TelemetryRepository
}
