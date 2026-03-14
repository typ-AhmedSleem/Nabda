package com.typ.nabda.feature.caregiver.localclient

import android.util.Log
import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.HEARTBEAT_BACKOFF_MS
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.HEARTBEAT_INTERVAL_MS
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.MAX_CONSECUTIVE_FAILURES
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants.TAG_HEARTBEAT
import com.typ.nabda.infrastructure.localnetwork.model.DeviceConnectionState
import com.typ.nabda.infrastructure.localnetwork.transport.TelemetryTransport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Polls the Deaf device for telemetry at a configurable interval.
 *
 * Implements a **3-strike offline** policy:
 * - 1st failure → [DeviceConnectionState.WARNING]
 * - 2nd failure → [DeviceConnectionState.RETRY] (with backoff to 15s)
 * - 3rd failure → [DeviceConnectionState.OFFLINE] (polling paused, waits for /ping recovery)
 *
 * A successful heartbeat resets the strike counter and restores [DeviceConnectionState.ONLINE].
 */
class HeartbeatPoller(
    private val transport: TelemetryTransport,
    private val scope: CoroutineScope,
) {

    private val _connectionState = MutableStateFlow(DeviceConnectionState.OFFLINE)
    val connectionState: StateFlow<DeviceConnectionState> = _connectionState.asStateFlow()

    private val _latestTelemetry = MutableStateFlow<TelemetryHeartbeatPayload?>(null)
    val latestTelemetry: StateFlow<TelemetryHeartbeatPayload?> = _latestTelemetry.asStateFlow()

    private var pollingJob: Job? = null
    private var consecutiveFailures = 0

    // ── Public API ──────────────────────────────────────────────────────────

    fun startPolling() {
        if (pollingJob?.isActive == true) {
            Log.d(TAG_HEARTBEAT, "Polling already active")
            return
        }
        consecutiveFailures = 0
        _connectionState.value = DeviceConnectionState.ONLINE
        pollingJob = scope.launch { pollingLoop() }
        Log.i(TAG_HEARTBEAT, "Heartbeat polling started")
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        consecutiveFailures = 0
        _connectionState.value = DeviceConnectionState.OFFLINE
        _latestTelemetry.value = null
        Log.i(TAG_HEARTBEAT, "Heartbeat polling stopped")
    }

    // ── Polling loop ────────────────────────────────────────────────────────

    private suspend fun pollingLoop() {
        while (scope.isActive) {
            try {
                val telemetry = transport.getTelemetry()
                onSuccess(telemetry)
            } catch (e: CancellationException) {
                throw e // Respect structured concurrency
            } catch (e: Exception) {
                onFailure(e)
            }

            val delayMs = if (consecutiveFailures >= 2) HEARTBEAT_BACKOFF_MS else HEARTBEAT_INTERVAL_MS
            delay(delayMs)

            // If offline, wait for /ping recovery before resuming telemetry polling
            if (_connectionState.value == DeviceConnectionState.OFFLINE) {
                Log.i(TAG_HEARTBEAT, "Device offline. Waiting for /ping recovery...")
                waitForPingRecovery()
            }
        }
    }

    private fun onSuccess(telemetry: TelemetryHeartbeatPayload) {
        if (consecutiveFailures > 0) {
            Log.i(TAG_HEARTBEAT, "Heartbeat recovered after $consecutiveFailures failure(s)")
        }
        consecutiveFailures = 0
        _connectionState.value = DeviceConnectionState.ONLINE
        _latestTelemetry.value = telemetry
        Log.d(TAG_HEARTBEAT, "Telemetry received: battery=${telemetry.batteryPercentage}%")
    }

    private fun onFailure(exception: Exception) {
        consecutiveFailures++
        Log.w(TAG_HEARTBEAT, "Heartbeat failure #$consecutiveFailures", exception)

        _connectionState.value = when {
            consecutiveFailures >= MAX_CONSECUTIVE_FAILURES -> {
                Log.e(TAG_HEARTBEAT, "Device marked OFFLINE after $MAX_CONSECUTIVE_FAILURES consecutive failures")
                DeviceConnectionState.OFFLINE
            }

            consecutiveFailures == 2 -> {
                Log.w(TAG_HEARTBEAT, "Retry with backoff")
                DeviceConnectionState.RETRY
            }

            else -> {
                Log.w(TAG_HEARTBEAT, "Warning: first failure")
                DeviceConnectionState.WARNING
            }
        }
    }

    /**
     * Blocks the polling loop until a /ping succeeds, polling at the backoff interval.
     */
    private suspend fun waitForPingRecovery() {
        while (scope.isActive && _connectionState.value == DeviceConnectionState.OFFLINE) {
            delay(HEARTBEAT_BACKOFF_MS)
            try {
                if (transport.getPing()) {
                    Log.i(TAG_HEARTBEAT, "Ping recovery succeeded — resuming telemetry polling")
                    consecutiveFailures = 0
                    _connectionState.value = DeviceConnectionState.ONLINE
                    return
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.d(TAG_HEARTBEAT, "Ping recovery attempt failed", e)
            }
        }
    }
}
