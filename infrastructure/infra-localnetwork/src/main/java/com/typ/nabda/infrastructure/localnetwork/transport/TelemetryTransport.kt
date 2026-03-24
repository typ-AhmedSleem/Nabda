package com.typ.nabda.infrastructure.localnetwork.transport

import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.infrastructure.localnetwork.model.ActionAckPayload
import com.typ.nabda.infrastructure.localnetwork.model.CaregiverActionPayload

/**
 * Abstraction for communicating between the Caregiver and Deaf devices.
 */
interface TelemetryTransport {
    /** Lightweight availability check. Returns true if the remote device is reachable. */
    suspend fun getPing(): Boolean

    /** Retrieves the latest telemetry snapshot from the remote device. */
    suspend fun getTelemetry(): TelemetryHeartbeatPayload

    /** Sends a caregiver action command to the deaf app device and returns an acknowledgement. */
    suspend fun sendAction(action: CaregiverActionPayload): ActionAckPayload
}
