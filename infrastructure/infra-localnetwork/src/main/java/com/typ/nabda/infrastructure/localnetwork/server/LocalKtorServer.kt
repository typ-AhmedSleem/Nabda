package com.typ.nabda.infrastructure.localnetwork.server

import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants
import com.typ.nabda.infrastructure.localnetwork.model.ActionPayload
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Embedded Ktor server that broadcasts telemetry and actions to connected Caregiver apps.
 */
class LocalKtorServer(
    private val port: Int = LocalNetworkConstants.SERVER_PORT,
    private val onClientConnected: (String) -> Unit,
    private val onClientDisconnected: (String) -> Unit,
    private val onActionReceived: (String) -> Unit, // For 'Received' ACK
) {
    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null
    private val clients = ConcurrentHashMap<String, DefaultWebSocketServerSession>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start() {
        server = embeddedServer(CIO, port = port) {
            install(ContentNegotiation) {
                json()
            }
            install(WebSockets) {
                pingPeriodMillis = 15000L
                timeoutMillis = 30000L
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            routing {
                webSocket("/ws/events") {
                    val clientId = UUID.randomUUID().toString()
                    clients[clientId] = this
                    onClientConnected(clientId)

                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                // Handle incoming messages (like acknowledgements)
                                if (text.startsWith("ACK:")) {
                                    val actionId = text.removePrefix("ACK:")
                                    onActionReceived(actionId)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        clients.remove(clientId)
                        onClientDisconnected(clientId)
                    }
                }

                get("/status") {
                    // Simple health check
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 2000)
        scope.cancel()
    }

    suspend fun broadcastTelemetry(telemetry: TelemetryHeartbeatPayload) {
        val message = Json.encodeToString(telemetry)
        broadcast(message)
    }

    suspend fun broadcastAction(action: ActionPayload) {
        val message = Json.encodeToString(action)
        broadcast(message)
    }

    private suspend fun broadcast(message: String) {
        clients.values.forEach { session ->
            if (session.isActive) {
                session.send(Frame.Text(message))
            }
        }
    }

    fun getConnectedClientsCount(): Int = clients.size
}
