package com.typ.nabda.infrastructure.localnetwork.client

import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.infrastructure.localnetwork.model.ActionPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LocalClientRegistry {
    private val _status = MutableStateFlow(ConnectionStatus.IDLE)
    val status: StateFlow<ConnectionStatus> = _status.asStateFlow()

    private val _telemetry = MutableStateFlow<TelemetryHeartbeatPayload?>(null)
    val telemetry: StateFlow<TelemetryHeartbeatPayload?> = _telemetry.asStateFlow()

    private val _alerts = MutableStateFlow<ActionPayload?>(null)
    val alerts: StateFlow<ActionPayload?> = _alerts.asStateFlow()

    fun updateStatus(newStatus: ConnectionStatus) {
        _status.value = newStatus
    }

    fun updateTelemetry(payload: TelemetryHeartbeatPayload?) {
        _telemetry.value = payload
    }

    fun updateAlert(payload: ActionPayload?) {
        _alerts.value = payload
    }
}
