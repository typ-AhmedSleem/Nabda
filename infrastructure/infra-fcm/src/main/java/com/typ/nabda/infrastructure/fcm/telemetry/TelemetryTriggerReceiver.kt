package com.typ.nabda.infrastructure.fcm.telemetry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TelemetryTriggerReceiver : BroadcastReceiver(), KoinComponent {

    private val telemetryScheduler: TelemetryScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BATTERY_LOW -> {
                telemetryScheduler.scheduleImmediate(context)
            }

            "android.net.conn.CONNECTIVITY_CHANGE" -> {
                telemetryScheduler.scheduleImmediate(context)
            }
        }
    }
}
