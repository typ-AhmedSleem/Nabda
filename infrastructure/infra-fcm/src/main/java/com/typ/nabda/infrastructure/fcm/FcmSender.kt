package com.typ.nabda.infrastructure.fcm

import android.util.Log
import com.typ.nabda.core.common.NabdaResult
import com.typ.nabda.core.messaging.MessageSender
import com.typ.nabda.core.model.Action
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FcmSender(
    private val serverKey: String, // Injected
) : MessageSender {

    private val fcmService: FcmService by lazy {
        Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FcmService::class.java)
    }

    override suspend fun sendAction(targetToken: String, action: Action): NabdaResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val dataPayload = mapOf(
                    "actionId" to action.id,
                    "actionName" to action.name,
                    "actionPriority" to action.priority.name,
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
                Log.e("FcmSender", "Error sending FCM", e)
                NabdaResult.Error(e)
            }
        }
    }
}
