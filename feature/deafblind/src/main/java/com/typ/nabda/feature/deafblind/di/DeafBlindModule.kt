package com.typ.nabda.feature.deafblind.di

import com.typ.nabda.core.messaging.ActionHandler
import com.typ.nabda.core.messaging.IncomingActionDispatcher
import com.typ.nabda.feature.deafblind.DeafBlindViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val deafBlindFeatureModule = module {
    single { IncomingActionDispatcher() } bind ActionHandler::class
    viewModel {
        DeafBlindViewModel(
            signalDispatcher = get(),
            context = androidContext(),
            incomingActionDispatcher = get()
        )
    }
}
