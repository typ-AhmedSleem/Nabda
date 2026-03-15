package com.typ.nabda.core.model

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryHeartbeatPayload(
    val deviceId: String,
    val batteryPercentage: Int?,
    val connectivitySource: ConnectivitySource,
    val isCharging: Boolean?,
    val signalStrength: Int?, // 0-4 bars
    val isSilentMode: Boolean?,
    val location: LocationSnapshot?,
    val timestamp: Long,
)
