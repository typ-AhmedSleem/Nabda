package com.typ.nabda.infrastructure.fcm

import android.util.Log
import com.typ.nabda.core.common.NabdaResult
import com.typ.nabda.core.messaging.MessageSender
import com.typ.nabda.core.messaging.TelemetrySender
import com.typ.nabda.core.model.RequestedAction
import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FcmSender(
    private val serverKey: String, // Injected
) : MessageSender, TelemetrySender {

    private val fcmService: FcmService by lazy {
        Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FcmService::class.java)
    }

    override suspend fun sendAction(targetToken: String, requestedAction: RequestedAction): NabdaResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val dataPayload = mapOf(
                    "type" to "ACTION",
                    "actionId" to requestedAction.id,
                    "actionName" to requestedAction.name,
                    "actionPriority" to requestedAction.priority.name,
                    "timestamp" to System.currentTimeMillis().toString()
                )

                val payload = FcmPayload(
                    to = targetToken,
                    data = dataPayload
                )

                val response = fcmService.sendNotification(
                    authHeader = "key=$serverKey",
                    payload = payload
                )

                if (response.isSuccessful) {
                    NabdaResult.Success(Unit)
                } else {
                    NabdaResult.Error(Exception("FCM Send Failed: ${response.code()} ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Log.e("FcmSender", "Error sending FCM Action", e)
                NabdaResult.Error(e)
            }
        }
    }

    override suspend fun sendTelemetry(targetToken: String, payload: TelemetryHeartbeatPayload): NabdaResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonPayload = Json.encodeToString(payload)
                val dataPayload = mapOf(
                    "type" to "TELEMETRY_HEARTBEAT",
                    "payload" to jsonPayload,
                    "timestamp" to System.currentTimeMillis().toString()
                )

                val fcmPayload = FcmPayload(
                    to = targetToken,
                    data = dataPayload,
                    priority = "high"
                )

                val response = fcmService.sendNotification(
                    authHeader = "key=$serverKey",
                    payload = fcmPayload
                )

                if (response.isSuccessful) {
                    NabdaResult.Success(Unit)
                } else {
                    NabdaResult.Error(Exception("FCM Send Telemetry Failed: ${response.code()} ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Log.e("FcmSender", "Error sending FCM Telemetry", e)
                NabdaResult.Error(e)
            }
        }
    }
}
