package com.typ.nabda.core.dispatcher.di

import com.typ.nabda.core.dispatcher.SignalDispatcher
import com.typ.nabda.core.dispatcher.SignalDispatcherImpl
import org.koin.dsl.module

val dispatcherModule = module {
    single<SignalDispatcher> { SignalDispatcherImpl(get(), get(), get()) }
}
