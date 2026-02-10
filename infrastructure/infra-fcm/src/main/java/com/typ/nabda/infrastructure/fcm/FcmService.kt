package com.typ.nabda.infrastructure.fcm

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FcmService {
    @POST("fcm/send")
    suspend fun sendNotification(
        @Header("Authorization") authHeader: String,
        @Body payload: FcmPayload,
    ): Response<ResponseBody>
}

data class FcmPayload(
    val to: String,
    val data: Map<String, String>,
    val priority: String = "high",
)
