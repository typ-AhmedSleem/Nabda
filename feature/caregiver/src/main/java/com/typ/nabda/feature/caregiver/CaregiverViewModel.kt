package com.typ.nabda.feature.caregiver

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.nabda.core.model.Alert
import com.typ.nabda.core.model.ConnectivitySource
import com.typ.nabda.core.model.SupportedAction
import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.core.model.TelemetryRepository
import com.typ.nabda.core.notifications.NabdaNotificationManager
import com.typ.nabda.core.pairing.PairingRepository
import com.typ.nabda.feature.caregiver.localclient.DeviceDiscoveryManager
import com.typ.nabda.feature.caregiver.localclient.HeartbeatPoller
import com.typ.nabda.infrastructure.localnetwork.model.ActionPayload
import com.typ.nabda.infrastructure.localnetwork.model.DeviceConnectionState
import com.typ.nabda.infrastructure.localnetwork.model.GestureAction
import com.typ.nabda.infrastructure.localnetwork.transport.TelemetryTransport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CaregiverViewModel(
    private val notificationManager: NabdaNotificationManager,
    private val telemetryRepository: TelemetryRepository,
    private val pairingRepository: PairingRepository,
    private val geocoder: LocationGeocoder,
    private val discoveryManager: DeviceDiscoveryManager,
    private val heartbeatPoller: HeartbeatPoller,
    private val transport: TelemetryTransport,
) : ViewModel() {

    // Dashboard states
    val isInternetConnected = MutableStateFlow(true).asStateFlow()
    val isNotificationPermissionGranted = MutableStateFlow(true).asStateFlow()
    val isCameraPermissionGranted = MutableStateFlow(true).asStateFlow()
    val isPhoneSilent = MutableStateFlow(false).asStateFlow()

    init {
        // Start local device discovery
        discoveryManager.startDiscovery()

        // Automatically start/stop polling based on discovery
        discoveryManager.discoveredHost
            .onEach { host ->
                if (host != null) heartbeatPoller.startPolling()
                else heartbeatPoller.stopPolling()
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        discoveryManager.stopDiscovery()
        heartbeatPoller.stopPolling()
    }

    // Telemetry State
    @OptIn(ExperimentalCoroutinesApi::class)
    val telemetryUiState: StateFlow<DeviceTelemetryUiState?> = combine(
        pairingRepository.getPairedDeviceFlow(),
        heartbeatPoller.latestTelemetry,
        heartbeatPoller.connectionState
    ) { device, localTelemetry, localState ->
        Triple(device, localTelemetry, localState)
    }.flatMapLatest { (device, localTelemetry, localState) ->
        // Priority 1: Local Telemetry (WORKS EVEN IF NO DEVICE PAIRED)
        if (localTelemetry != null) {
            return@flatMapLatest flowOf(mapToUiState(localTelemetry, localState))
        }

        // Priority 2: Remote Telemetry (REQUIRES PAIRED DEVICE)
        if (device != null) {
            val caregiverId = pairingRepository.getMyUUID()
            return@flatMapLatest telemetryRepository.observeLatest(caregiverId, device.uuid).map { payload ->
                if (payload == null) null
                else {
                    val now = System.currentTimeMillis()
                    val diff = now - payload.timestamp
                    val status = when {
                        diff < 7 * 60 * 1000 -> DeviceStatus.ONLINE
                        diff < 15 * 60 * 1000 -> DeviceStatus.DELAYED
                        else -> DeviceStatus.OFFLINE
                    }
                    mapToUiState(payload, status)
                }
            }
        }

        // Priority 3: No device, but maybe we are scanning/connecting
        if (localState != DeviceConnectionState.OFFLINE) {
            // Show a "Connecting..." state even without a formal device object
            return@flatMapLatest flowOf(
                DeviceTelemetryUiState(
                    deviceStatus = DeviceStatus.DELAYED,
                    locationLabel = "Searching locally...",
                    lastSeenLabel = "mDNS Active",
                    batteryLevel = BatteryLevel.NORMAL,
                    batteryPercentage = 0,
                    isCharging = false,
                    connectivity = ConnectivitySource.NONE,

                    )
            )
        }

        flowOf(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private suspend fun mapToUiState(payload: TelemetryHeartbeatPayload, state: DeviceConnectionState): DeviceTelemetryUiState {
        val status = when (state) {
            DeviceConnectionState.ONLINE -> DeviceStatus.ONLINE
            DeviceConnectionState.WARNING -> DeviceStatus.ONLINE
            DeviceConnectionState.RETRY -> DeviceStatus.DELAYED
            DeviceConnectionState.OFFLINE -> DeviceStatus.OFFLINE
        }
        return mapToUiState(payload, status)
    }

    private suspend fun mapToUiState(payload: TelemetryHeartbeatPayload, status: DeviceStatus): DeviceTelemetryUiState {
        val payloadBatteryPercentage = payload.batteryPercentage ?: 0
        val batteryLevel = when {
            payloadBatteryPercentage > 30 -> BatteryLevel.NORMAL
            payloadBatteryPercentage > 15 -> BatteryLevel.WARNING
            else -> BatteryLevel.CRITICAL
        }

        val timestamp = if (payload.timestamp > 0) payload.timestamp else System.currentTimeMillis()
        val lastSeenLabel = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

        return DeviceTelemetryUiState(
            deviceStatus = status,
            batteryLevel = batteryLevel,
            batteryPercentage = payloadBatteryPercentage,
            isCharging = payload.isCharging ?: false,
            connectivity = payload.connectivitySource,
            locationLabel = geocoder.geocode(payload.location),
            lastSeenLabel = "Last seen: $lastSeenLabel"
        )
    }

    /**
     * Sends an action directly to the locally discovered device, bypassing the pairing check.
     */
    fun sendAction(supportedAction: SupportedAction) {
        val gesture = mapToGesture(supportedAction.id)

        viewModelScope.launch {
            try {
                // If we have a local host, send it directly via HTTP
                if (discoveryManager.discoveredHost.value != null) {
                    val payload = ActionPayload(
                        action = gesture,
                        timestamp = System.currentTimeMillis(),
                        correlationId = UUID.randomUUID().toString(),
                    )
                    transport.sendAction(payload)
                    // We could update local UI history here if needed
                } else {
                    // Fallback to existing SignalDispatcher (Requires pairing)
                    // Note: This requires the ViewModel to have SignalDispatcher injected, 
                    // but since we are bypassing for demo, we focus on direct local transport.
                    Log.w("CaregiverViewModel", "No local device found for direct action")
                }
            } catch (e: Exception) {
                Log.e("CaregiverViewModel", "Failed to send local action", e)
            }
        }
    }

    private fun mapToGesture(actionId: String): GestureAction {
        return when (actionId) {
//            "vibrate" -> GestureAction.VIBRATE
//            "alert" -> GestureAction.ALERT
            "voice" -> GestureAction.FALL_ALERT
            else -> GestureAction.HELP_REQUEST
        }
    }

    // History states
    val alerts: StateFlow<List<Alert>> = notificationManager.alertHistory
    private val _selectedFilter = MutableStateFlow<SupportedAction?>(null) // null means "All Alerts"
    val selectedFilter = _selectedFilter.asStateFlow()

    val filteredAlerts: StateFlow<List<Alert>> = combine(alerts, _selectedFilter) { alerts, filter ->
        if (filter == null) alerts
        else alerts.filter { it.actionId == filter.id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onFilterSelected(filter: SupportedAction?) {
        _selectedFilter.value = filter
    }
}
