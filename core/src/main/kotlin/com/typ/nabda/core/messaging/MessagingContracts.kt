package com.typ.nabda.core.messaging

import com.typ.nabda.core.common.NabdaResult
import com.typ.nabda.core.model.RequestedAction
import com.typ.nabda.core.model.TelemetryHeartbeatPayload

interface TokenRepository {
    suspend fun updateMyToken(token: String)
    suspend fun getRemoteToken(uuid: String): String?
}

interface MessageSender {
    suspend fun sendAction(
        targetToken: String,
        requestedAction: RequestedAction,
    ): NabdaResult<Unit>
}

interface TelemetrySender {
    suspend fun sendTelemetry(
        targetToken: String,
        payload: TelemetryHeartbeatPayload,
    ): NabdaResult<Unit>
}

fun interface TelemetryHandler {
    fun onTelemetryReceived(payload: TelemetryHeartbeatPayload)
}

fun interface ActionHandler {
    fun onActionReceived(requestedAction: RequestedAction)
}
