package com.typ.nabda.feature.caregiver

import com.typ.nabda.core.messaging.TelemetryHandler
import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.core.model.TelemetryRepository
import com.typ.nabda.core.pairing.PairingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CaregiverTelemetryHandler(
    private val telemetryRepository: TelemetryRepository,
    private val pairingRepository: PairingRepository,
) : TelemetryHandler {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onTelemetryReceived(payload: TelemetryHeartbeatPayload) {
        scope.launch {
            val myUuid = pairingRepository.getMyUUID()
            // In a real app, we might check if this deviceId is paired with us
            // For now, we store it under the current caregiver
            telemetryRepository.storeTelemetry(caregiverId = myUuid, payload = payload)

            // TODO: Trigger local notification if battery < 15
        }
    }
}
