package com.typ.nabda.infrastructure.localnetwork.client

import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.infrastructure.localnetwork.model.ActionPayload
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Client for receiving real-time telemetry and alerts from the Deaf-blind app.
 */
class TelemetryClient(
    private val host: String,
    private val port: Int = 8080,
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val _events = MutableSharedFlow<Any>(extraBufferCapacity = 10)
    val telemetryEvents: Flow<TelemetryHeartbeatPayload> = _events.filterIsInstance<TelemetryHeartbeatPayload>()
    val actionEvents: Flow<ActionPayload> = _events.filterIsInstance<ActionPayload>()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var session: DefaultClientWebSocketSession? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var connectionJob: Job? = null

    fun connect() {
        connectionJob?.cancel()
        connectionJob = scope.launch {
            while (isActive) {
                try {
                    client.webSocket(host = host, port = port, path = "/ws/events") {
                        session = this
                        _isConnected.value = true
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                handleIncomingFrame(text)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    session = null
                    _isConnected.value = false
                }
                delay(3000) // Retry every 3 seconds
            }
        }
    }

    private suspend fun handleIncomingFrame(text: String) {
        try {
            // Try parsing as Telemetry first
            val telemetry = Json.decodeFromString<TelemetryHeartbeatPayload>(text)
            _events.emit(telemetry)
        } catch (e: Exception) {
            try {
                // Then try as ActionPayload
                val action = Json.decodeFromString<ActionPayload>(text)
                _events.emit(action)
            } catch (e2: Exception) {
                // Ignore unknown formats
            }
        }
    }

    suspend fun acknowledgeAlert(actionId: String) {
        session?.send(Frame.Text("ACK:$actionId"))
    }

    fun disconnect() {
        scope.cancel()
        client.close()
    }
}
