package com.typ.nabda.infrastructure.storage.di

import com.google.firebase.firestore.FirebaseFirestore
import com.typ.nabda.core.messaging.TokenRepository
import com.typ.nabda.core.model.TelemetryRepository
import com.typ.nabda.core.pairing.PairingRepository
import com.typ.nabda.infrastructure.storage.FirestoreTelemetryRepository
import com.typ.nabda.infrastructure.storage.PairingRepositoryImpl
import com.typ.nabda.infrastructure.storage.TokenRepositoryImpl
import org.koin.dsl.module

val storageModule = module {
    single { FirebaseFirestore.getInstance() }

    // PairingRepository
    single<PairingRepository> { PairingRepositoryImpl(get(), get()) }

    // TokenRepository
    single<TokenRepository> { TokenRepositoryImpl(get(), get()) }

    // TelemetryRepository
    single<TelemetryRepository> { FirestoreTelemetryRepository(get()) }
}
