package com.typ.nabda.feature.caregiver

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.nabda.core.model.ActionPriority
import com.typ.nabda.core.model.Alert
import com.typ.nabda.core.model.SupportedAction
import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.core.model.TelemetryRepository
import com.typ.nabda.core.notifications.NabdaNotificationManager
import com.typ.nabda.core.pairing.PairingRepository
import com.typ.nabda.feature.caregiver.localclient.DeviceDiscoveryManager
import com.typ.nabda.feature.caregiver.localclient.HeartbeatPoller
import com.typ.nabda.infrastructure.localnetwork.client.ConnectionStatus
import com.typ.nabda.infrastructure.localnetwork.client.LocalClientRegistry
import com.typ.nabda.infrastructure.localnetwork.model.ActionPayload
import com.typ.nabda.infrastructure.localnetwork.model.GestureAction
import com.typ.nabda.infrastructure.localnetwork.transport.TelemetryTransport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Stable
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

    override fun onCleared() {
        super.onCleared()
        discoveryManager.stopDiscovery()
        heartbeatPoller.stopPolling()
    }

    // Telemetry State
    @OptIn(ExperimentalCoroutinesApi::class)
    val telemetryUiState: StateFlow<DeviceTelemetryUiState?> = combine(
        LocalClientRegistry.telemetry,
        LocalClientRegistry.status
    ) { localTelemetry, status ->
        if (localTelemetry != null) {
            val deviceStatus = when (status) {
                ConnectionStatus.PAIRED -> DeviceStatus.ONLINE
                else -> DeviceStatus.DELAYED
            }
            mapToUiState(localTelemetry, deviceStatus)
        } else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /*private suspend fun mapToUiState(payload: TelemetryHeartbeatPayload, state: DeviceConnectionState): DeviceTelemetryUiState {
        val status = when (state) {
            DeviceConnectionState.ONLINE -> DeviceStatus.ONLINE
            DeviceConnectionState.WARNING -> DeviceStatus.ONLINE
            DeviceConnectionState.RETRY -> DeviceStatus.DELAYED
            DeviceConnectionState.OFFLINE -> DeviceStatus.OFFLINE
        }
        return mapToUiState(payload, status)
    }*/

    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    private suspend fun mapToUiState(payload: TelemetryHeartbeatPayload, status: DeviceStatus): DeviceTelemetryUiState {
        val payloadBatteryPercentage = payload.batteryPercentage ?: 0
        val batteryLevel = when {
            payloadBatteryPercentage > 30 -> BatteryLevel.NORMAL
            payloadBatteryPercentage > 15 -> BatteryLevel.WARNING
            else -> BatteryLevel.CRITICAL
        }

        val timestamp = if (payload.timestamp > 0) payload.timestamp else System.currentTimeMillis()
        val lastSeenLabel = timeFormatter.format(Date(timestamp))

        return DeviceTelemetryUiState(
            deviceStatus = status,
            batteryLevel = batteryLevel,
            batteryPercentage = payloadBatteryPercentage,
            isCharging = payload.isCharging ?: false,
            signalStrength = payload.signalStrength ?: 0,
            isSilentMode = payload.isSilentMode ?: false,
            connectivity = payload.connectivitySource,
            locationLabel = if (payload.location != null) geocoder.geocode(payload.location) else "Unknown",
            lastSeenLabel = "Last seen: $lastSeenLabel",
            rawTimestamp = timestamp
        )
    }

    // Navigation events
    private val _navigationEvents = MutableSharedFlow<CaregiverNavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    init {
        // ... (previous init code)
        viewModelScope.launch {
            LocalClientRegistry.status.collect { status ->
                if (status == ConnectionStatus.IDLE) {
                    _navigationEvents.emit(CaregiverNavigationEvent.NavigateToDiscovery)
                }
            }
        }
    }

    enum class CaregiverNavigationEvent {
        NavigateToDiscovery
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
                        title = supportedAction.name,
                        priority = ActionPriority.NORMAL, // Defaulting to normal for manual sends
                        timestamp = System.currentTimeMillis(),
                        correlationId = UUID.randomUUID().toString(),
                    )
                    transport.sendAction(payload)
                    // We could update local UI history here if needed
                } else {
                    // Fallback to existing SignalDispatcher (Requires pairing)
                    Log.w("CaregiverViewModel", "No local device found for direct action")
                }
            } catch (e: Exception) {
                Log.e("CaregiverViewModel", "Failed to send local action", e)
            }
        }
    }

    private fun mapToGesture(actionId: String): GestureAction {
        return when (actionId) {
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
