package com.typ.nabda.deaf

import android.app.Application
import com.typ.nabda.deaf.di.initKoin

class NabdaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(this@NabdaApplication)
    }
}
