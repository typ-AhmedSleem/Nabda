package com.typ.nabda.feature.caregiver

import androidx.compose.runtime.Immutable
import com.typ.nabda.core.model.ConnectivitySource

/**
 * Represents the UI state for the device telemetry information displayed to a caregiver.
 *
 * @property deviceStatus The current operational status of the device (e.g., Online, Offline).
 * @property batteryLevel The categorized battery health level.
 * @property batteryPercentage The current battery charge percentage.
 * @property isCharging Indicates whether the device is currently connected to a power source.
 * @property connectivity The source of the device's network connection.
 * @property locationLabel A human-readable string representing the device's last known location.
 * @property lastSeenLabel A human-readable string representing how long ago the device was last active.
 */
@Immutable
data class DeviceTelemetryUiState(
    val deviceStatus: DeviceStatus,
    val batteryLevel: BatteryLevel,
    val batteryPercentage: Int,
    val isCharging: Boolean,
    val signalStrength: Int, // 0-4 bars
    val isSilentMode: Boolean,
    val connectivity: ConnectivitySource,
    val locationLabel: String,
    val lastSeenLabel: String,
    val rawTimestamp: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

/**
 * Represents the possible operational states of a monitored device.
 *
 * @property ONLINE The device is currently active and communicating in real-time.
 * @property DELAYED The device is active but has not sent data within the expected timeframe.
 * @property OFFLINE The device is disconnected or has not been seen for an extended period.
 */
enum class DeviceStatus { ONLINE, DELAYED, OFFLINE }

/**
 * Represents the categorized battery health levels for a device.
 *
 * @property NORMAL Indicates the battery is at a safe and functional level.
 * @property WARNING Indicates the battery is low and may soon require charging.
 * @property CRITICAL Indicates the battery is extremely low and requires immediate attention.
 */
enum class BatteryLevel { NORMAL, WARNING, CRITICAL }
