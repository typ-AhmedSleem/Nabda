package com.typ.nabda

import android.app.Application
import com.typ.nabda.di.initKoin

class NabdaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(this@NabdaApplication)
    }
}
