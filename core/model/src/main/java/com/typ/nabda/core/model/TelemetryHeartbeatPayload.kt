package com.typ.nabda.core.model

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryHeartbeatPayload(
    val deviceId: String,
    val batteryPercentage: Int,
    val connectivitySource: ConnectivitySource,
    val isCharging: Boolean,
    val location: LocationSnapshot?,
    val timestamp: Long,
)
