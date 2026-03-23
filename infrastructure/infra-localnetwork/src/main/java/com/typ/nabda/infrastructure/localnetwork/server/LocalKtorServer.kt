package com.typ.nabda.infrastructure.localnetwork.server

import android.util.Log
import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.TAG_SERVER
import com.typ.nabda.infrastructure.localnetwork.client.ServerStatus
import com.typ.nabda.infrastructure.localnetwork.model.ActionAckPayload
import com.typ.nabda.infrastructure.localnetwork.model.ActionPayload
import com.typ.nabda.infrastructure.localnetwork.model.CaregiverActionPayload
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.origin
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
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
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

    @OptIn(ExperimentalUuidApi::class)
    fun start() {
        LocalServerRegistry.updateStatus(ServerStatus.STARTING)
        server = embeddedServer(CIO, port = port, host = LocalNetworkConstants.SERVER_HOST) {
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
                    val clientId = call.request.origin.remoteHost
                    clients[clientId] = this
                    LocalServerRegistry.updateClientsCount(clients.size)
                    onClientConnected(clientId)
                    Log.d(TAG_SERVER, "Client connected: $clientId. Total: ${clients.size}")

                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                Log.d(TAG_SERVER, "Received: $text")
                                // Handle incoming messages (like acknowledgements)
                                if (text.startsWith("ACK:")) {
                                    val actionId = text.removePrefix("ACK:")
                                    onActionReceived(actionId)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG_SERVER, "Error in WebSocket for $clientId", e)
                    } finally {
                        clients.remove(clientId)
                        LocalServerRegistry.updateClientsCount(clients.size)
                        onClientDisconnected(clientId)
                        Log.d(TAG_SERVER, "Client disconnected: $clientId. Total: ${clients.size}")
                    }
                }

                get("/status") {
                    Log.d(TAG_SERVER, "Received /status request")
                    call.respond(HttpStatusCode.OK, "Server is running")
                }

                // ── POST /action ────────────────────────────────────────────────
                post("/action") {
                    Log.d(TAG_SERVER, "Received /action request")
                    val correlationId = Uuid.random().toString()
                    try {
                        val actionPayload = call.receive<CaregiverActionPayload>()
                        onActionReceived(actionPayload.actionId)
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = ActionAckPayload(
                                correlationId = correlationId,
                                success = true,
                                message = "Received '${actionPayload.actionId}' successfully."
                            )
                        )
                        Log.d(TAG_SERVER, "Received /action request for ${actionPayload.actionId} and responded with ACK.")
                    } catch (e: Exception) {
                        Log.w(TAG_SERVER, "Bad request on /action", e)
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = ActionAckPayload(
                                success = false,
                                correlationId = correlationId,
                                message = "Bad request on '/action'."
                            )
                        )
                    }
                }
            }
        }.start(wait = false)
        LocalServerRegistry.updateStatus(ServerStatus.RUNNING)
        Log.i(TAG_SERVER, "Server started on host ${LocalNetworkConstants.SERVER_HOST} port $port")
    }

    fun stop() {
        server?.stop(1000, 1000)
        server = null
        LocalServerRegistry.updateStatus(ServerStatus.OFFLINE)
        Log.i(TAG_SERVER, "Server stopped")
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
