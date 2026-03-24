package com.typ.nabda.infrastructure.localnetwork.dispatcher

import com.typ.nabda.core.common.NabdaResult
import com.typ.nabda.core.dispatcher.SignalDispatcher
import com.typ.nabda.core.model.RequestedAction
import com.typ.nabda.infrastructure.localnetwork.model.ActionPayload
import com.typ.nabda.infrastructure.localnetwork.server.LocalServerRegistry
import java.util.UUID

/**
 * Implementation of [SignalDispatcher] that broadcasts actions to all connected Ktor clients.
 */
class WifiSignalDispatcher : SignalDispatcher {

    override suspend fun dispatchAction(requestedAction: RequestedAction): NabdaResult<Unit> {
        val server = LocalServerRegistry.activeServer
            ?: return NabdaResult.Error(Exception("Server not running"))

        // Convert core Action to ActionPayload
        val payload = ActionPayload(
            actionId = requestedAction.id,
            title = requestedAction.name,
            priority = requestedAction.priority,
            timestamp = System.currentTimeMillis(),
            correlationId = UUID.randomUUID().toString()
        )

        return try {
            server.broadcastAction(payload)
            NabdaResult.Success(Unit)
        } catch (e: Exception) {
            NabdaResult.Error(e)
        }
    }
}
