package com.typ.nabda.infrastructure.localnetwork.model

/**
 * Represents the connectivity state of the remote device as observed by the [HeartbeatPoller].
 */
enum class DeviceConnectionState {
    /** Device is reachable and telemetry is flowing. */
    ONLINE,

    /** First heartbeat failure — device may be temporarily unreachable. */
    WARNING,

    /** Second consecutive failure — actively retrying with backoff. */
    RETRY,

    /** Third consecutive failure — device is considered fully offline. */
    OFFLINE,
}
