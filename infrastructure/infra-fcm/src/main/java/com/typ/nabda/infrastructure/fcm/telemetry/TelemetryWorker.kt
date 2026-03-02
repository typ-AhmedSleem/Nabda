package com.typ.nabda.infrastructure.fcm.telemetry

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.typ.nabda.core.common.NabdaResult
import com.typ.nabda.core.messaging.TelemetrySender
import com.typ.nabda.core.pairing.PairingRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TelemetryWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val telemetryCollector: TelemetryCollector by inject()
    private val telemetrySender: TelemetrySender by inject()
    private val pairingRepository: PairingRepository by inject()

    override suspend fun doWork(): Result {
        Log.d("TelemetryWorker", "Starting telemetry work")

        val pairedDevice = pairingRepository.getPairedDevice()
        if (pairedDevice == null) {
            Log.w("TelemetryWorker", "No paired device found, skipping telemetry")
            return Result.success()
        }

        val myUuid = pairingRepository.getMyUUID()

        return try {
            val payload = telemetryCollector.collect(myUuid)
            val result = telemetrySender.sendTelemetry(pairedDevice.fcmToken, payload)

            if (result is NabdaResult.Success) {
                Log.d("TelemetryWorker", "Telemetry sent successfully")
                Result.success()
            } else {
                Log.e("TelemetryWorker", "Failed to send telemetry: $result")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("TelemetryWorker", "Error in telemetry work", e)
            Result.retry()
        }
    }
}
