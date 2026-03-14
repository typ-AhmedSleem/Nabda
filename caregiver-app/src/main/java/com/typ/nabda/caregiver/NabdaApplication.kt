package com.typ.nabda.caregiver

import android.app.Application
import com.typ.nabda.caregiver.di.initKoin

class NabdaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(this@NabdaApplication)
    }
}
