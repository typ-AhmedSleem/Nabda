package com.typ.nabda.feature.caregiver.localclient

import android.util.Log
import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.AUTH_BEARER_PREFIX
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.AUTH_HEADER
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.CONNECT_TIMEOUT_MS
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.DEMO_AUTH_TOKEN
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.READ_TIMEOUT_MS
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.TAG_CLIENT
import com.typ.nabda.infrastructure.localnetwork.model.ActionAckPayload
import com.typ.nabda.infrastructure.localnetwork.model.ActionPayload
import com.typ.nabda.infrastructure.localnetwork.transport.TelemetryTransport
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * [TelemetryTransport] implementation that communicates with the Deaf device's local HTTP server.
 *
 * @property baseUrlProvider Dynamically provides the current base URL discovered by [DeviceDiscoveryManager].
 */
class LocalHttpTransport(
    private val baseUrlProvider: () -> String?,
) : TelemetryTransport {

    private val client = HttpClient(CIO) {
        engine {
            requestTimeout = READ_TIMEOUT_MS
            endpoint {
                connectTimeout = CONNECT_TIMEOUT_MS
            }
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = false
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.HEADERS
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    Log.d(TAG_CLIENT, message)
                }
            }
        }
    }

    private fun requireBaseUrl(): String {
        return baseUrlProvider()
            ?: throw IllegalStateException("No device discovered. Base URL is null.")
    }

    override suspend fun getPing(): Boolean {
        return try {
            val response = client.get("${requireBaseUrl()}/ping") {
                header(AUTH_HEADER, "$AUTH_BEARER_PREFIX$DEMO_AUTH_TOKEN")
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            Log.w(TAG_CLIENT, "Ping failed", e)
            false
        }
    }

    override suspend fun getTelemetry(): TelemetryHeartbeatPayload {
        val response = client.get("${requireBaseUrl()}/telemetry") {
            header(AUTH_HEADER, "$AUTH_BEARER_PREFIX$DEMO_AUTH_TOKEN")
        }
        return response.body()
    }

    override suspend fun sendAction(action: ActionPayload): ActionAckPayload {
        val response = client.post("${requireBaseUrl()}/action") {
            header(AUTH_HEADER, "$AUTH_BEARER_PREFIX$DEMO_AUTH_TOKEN")
            contentType(ContentType.Application.Json)
            setBody(action)
        }
        return response.body()
    }
}
