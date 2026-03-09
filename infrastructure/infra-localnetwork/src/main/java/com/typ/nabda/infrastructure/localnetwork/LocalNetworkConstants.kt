package com.typ.nabda.infrastructure.localnetwork

/**
 * Shared constants for the local networking layer.
 */
object LocalNetworkConstants {
    const val SERVER_PORT = 2001
    const val SERVER_HOST = "0.0.0.0"
    const val SERVICE_TYPE = "_nabda._tcp."
    const val SERVICE_NAME = "Nabda Device"
    const val DEMO_AUTH_TOKEN = "DEMO_TOKEN"

    /** HTTP header key for the static demo auth token. */
    const val AUTH_HEADER = "Authorization"
    const val AUTH_BEARER_PREFIX = "Bearer "

    /** Logging tags */
    const val TAG_SERVER = "NABDA_SERVER"
    const val TAG_CLIENT = "NABDA_CLIENT"
    const val TAG_DISCOVERY = "NABDA_DISCOVERY"
    const val TAG_HEARTBEAT = "NABDA_HEARTBEAT"

    /** Timeouts */
    const val CONNECT_TIMEOUT_MS = 3_000L
    const val READ_TIMEOUT_MS = 3_000L

    /** Heartbeat polling */
    const val HEARTBEAT_INTERVAL_MS = 10_000L
    const val HEARTBEAT_BACKOFF_MS = 15_000L
    const val MAX_CONSECUTIVE_FAILURES = 3
}
