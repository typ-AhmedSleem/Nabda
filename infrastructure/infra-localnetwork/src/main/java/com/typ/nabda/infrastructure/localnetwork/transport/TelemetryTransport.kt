package com.typ.nabda.infrastructure.localnetwork.transport

import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.infrastructure.localnetwork.model.ActionAckPayload
import com.typ.nabda.infrastructure.localnetwork.model.ActionPayload

/**
 * Abstraction for communicating between the Caregiver and Deaf devices.
 *
 * Implementations:
 * - [Firebase-based transport] for production.
 * - [LocalHttpTransport] for offline demo via local WiFi.
 */
interface TelemetryTransport {
    /** Lightweight availability check. Returns true if the remote device is reachable. */
    suspend fun getPing(): Boolean

    /** Retrieves the latest telemetry snapshot from the remote device. */
    suspend fun getTelemetry(): TelemetryHeartbeatPayload

    /** Sends an action command to the remote device and returns an acknowledgement. */
    suspend fun sendAction(action: ActionPayload): ActionAckPayload
}
