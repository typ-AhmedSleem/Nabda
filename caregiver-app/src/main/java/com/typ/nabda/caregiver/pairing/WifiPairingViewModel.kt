package com.typ.nabda.caregiver.pairing

import androidx.lifecycle.ViewModel
import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.infrastructure.localnetwork.client.ConnectionStatus
import com.typ.nabda.infrastructure.localnetwork.client.LocalClientRegistry
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the pairing_scanner route in the Caregiver app.
 * Observes the state from [LocalClientRegistry].
 */
class WifiPairingViewModel : ViewModel() {

    val pairingStatus: StateFlow<ConnectionStatus> = LocalClientRegistry.status

    val telemetry: StateFlow<TelemetryHeartbeatPayload?> = LocalClientRegistry.telemetry
}
