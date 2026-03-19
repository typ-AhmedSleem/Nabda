package com.typ.nabda.core.model

import kotlinx.coroutines.flow.Flow

interface TelemetryRepository {
    suspend fun storeTelemetry(caregiverId: String, payload: TelemetryHeartbeatPayload)
    fun observeLatest(caregiverId: String, deafDeviceId: String): Flow<TelemetryHeartbeatPayload?>
}

class TelemetryRepositoryStubImpl : TelemetryRepository {

    override suspend fun storeTelemetry(caregiverId: String, payload: TelemetryHeartbeatPayload) {
        TODO("Not yet implemented")
    }

    override fun observeLatest(
        caregiverId: String,
        deafDeviceId: String,
    ): Flow<TelemetryHeartbeatPayload?> {
        TODO("Not yet implemented")
    }

}