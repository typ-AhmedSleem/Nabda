package com.typ.nabda.feature.deafblind.localserver

import android.util.Log
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.AUTH_BEARER_PREFIX
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.AUTH_HEADER
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.DEMO_AUTH_TOKEN
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.TAG_SERVER
import com.typ.nabda.infrastructure.localnetwork.model.ActionAckPayload
import com.typ.nabda.infrastructure.localnetwork.model.ActionPayload
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

/**
 * Simple route-scoped plugin that validates a static demo bearer token.
 */
private val DemoAuthPlugin = createRouteScopedPlugin("DemoAuth") {
    onCall { call ->
        val authHeader = call.request.headers[AUTH_HEADER]
        val expectedToken = "$AUTH_BEARER_PREFIX$DEMO_AUTH_TOKEN"
        if (authHeader != expectedToken) {
            Log.w(TAG_SERVER, "Unauthorized request from ${call.request.local.remoteAddress}")
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
        }
    }
}

/**
 * Configures the Ktor server routing and plugins for the Deaf device's local HTTP server.
 */
fun Application.configureDeafServer(
    telemetryCollector: TelemetryCollector,
    deviceId: String,
    onActionReceived: (ActionPayload) -> ActionAckPayload,
) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            isLenient = false
            ignoreUnknownKeys = true
        })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            Log.e(TAG_SERVER, "Unhandled server error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Internal Server Error"))
            )
        }
    }

    routing {
        install(DemoAuthPlugin)

        // ── GET /ping ───────────────────────────────────────────────────
        get("/ping") {
            Log.d(TAG_SERVER, "Received /ping request")
            call.respond(HttpStatusCode.OK)
        }

        // ── GET /telemetry ──────────────────────────────────────────────
        get("/telemetry") {
            Log.d(TAG_SERVER, "Received /telemetry request")
            val payload = telemetryCollector.collect(deviceId)
            call.respond(payload)
        }

        // ── POST /action ────────────────────────────────────────────────
        post("/action") {
            Log.d(TAG_SERVER, "Received /action request")
            try {
                val actionPayload = call.receive<ActionPayload>()
                val ack = onActionReceived(actionPayload)
                call.respond(ack)
            } catch (e: Exception) {
                Log.w(TAG_SERVER, "Bad request on /action", e)
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Malformed action payload"))
                )
            }
        }
    }
}
