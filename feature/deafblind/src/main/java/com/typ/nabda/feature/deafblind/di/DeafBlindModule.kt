package com.typ.nabda.feature.deafblind.di

import com.typ.nabda.core.dispatcher.SignalDispatcher
import com.typ.nabda.core.messaging.ActionHandler
import com.typ.nabda.core.messaging.IncomingActionDispatcher
import com.typ.nabda.feature.deafblind.DeafBlindViewModel
import com.typ.nabda.infrastructure.localnetwork.dispatcher.WifiSignalDispatcher
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val deafBlindFeatureModule = module {
    single { IncomingActionDispatcher() } bind ActionHandler::class
    single(createdAtStart = true) { WifiSignalDispatcher() } bind SignalDispatcher::class

    viewModel {
        DeafBlindViewModel(
            signalDispatcher = get(),
            hapticEngine = get(),
            incomingActionDispatcher = get()
        )
    }
}
