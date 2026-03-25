package com.typ.nabda.feature.caregiver

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.nabda.core.model.Alert
import com.typ.nabda.core.model.CaregiverAction
import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.core.notifications.NabdaNotificationManager
import com.typ.nabda.feature.caregiver.localclient.DeviceDiscoveryManager
import com.typ.nabda.feature.caregiver.localclient.HeartbeatPoller
import com.typ.nabda.infrastructure.localnetwork.client.ConnectionStatus
import com.typ.nabda.infrastructure.localnetwork.client.LocalClientRegistry
import com.typ.nabda.infrastructure.localnetwork.model.CaregiverActionPayload
import com.typ.nabda.infrastructure.localnetwork.transport.TelemetryTransport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Stable
class CaregiverViewModel(
    notificationManager: NabdaNotificationManager,
    private val geocoder: LocationGeocoder,
    private val discoveryManager: DeviceDiscoveryManager,
    private val heartbeatPoller: HeartbeatPoller,
    private val transport: TelemetryTransport,
) : ViewModel(), KoinComponent {

    private val context: Context by lazy { get<Context>() }

    enum class CaregiverNavigationEvent {
        NavigateToDiscovery
    }

    // Dashboard states
    val isInternetConnected = MutableStateFlow(true).asStateFlow()
    val isNotificationPermissionGranted = MutableStateFlow(true).asStateFlow()
    val isCameraPermissionGranted = MutableStateFlow(true).asStateFlow()
    val isPhoneSilent = MutableStateFlow(false).asStateFlow()
    val pairingStatus: StateFlow<ConnectionStatus> = LocalClientRegistry.status
    val connectedHost = discoveryManager.discoveredHost

    private val _lastGeocodedLocationLabel = MutableStateFlow(context.getString(R.string.location_unavailable))
    val lastGeocodedLocationLabel = _lastGeocodedLocationLabel.asStateFlow()

    // Navigation events
    private val _navigationEvents = MutableSharedFlow<CaregiverNavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            pairingStatus.collect { status ->
                if (status == ConnectionStatus.IDLE) {
                    _navigationEvents.emit(CaregiverNavigationEvent.NavigateToDiscovery)
                }
            }
        }
        viewModelScope.launch {
            LocalClientRegistry.telemetry
                .map { it?.location }
                .distinctUntilChanged()
                .collect { location ->
                    if (location != null) {
                        Log.i("NABDA_CaregiverViewModel", "Geocoding location in ViewModel for received telemetry.")
                        _lastGeocodedLocationLabel.value = geocoder.geocode(location)
                    }
                }
        }
    }

    fun emitEvent(event: CaregiverNavigationEvent) {
        viewModelScope.launch {
            _navigationEvents.emit(event)
        }
    }

    override fun onCleared() {
        super.onCleared()
        discoveryManager.stopDiscovery()
        heartbeatPoller.stopPolling()
    }

    // Telemetry State
    @OptIn(ExperimentalCoroutinesApi::class)
    val telemetryUiState: StateFlow<DeviceTelemetryUiState?> = combine(
        LocalClientRegistry.telemetry,
        pairingStatus
    ) { localTelemetry, status ->
        Log.i("NABDA_CaregiverViewModel", "Handling telemetry in ViewModel...")
        if (localTelemetry != null) {
            val deviceStatus = when (status) {
                ConnectionStatus.CONNECTED -> DeviceStatus.ONLINE
                else -> DeviceStatus.DELAYED
            }
            mapToUiState(localTelemetry, deviceStatus)
        } else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val timeFormatter = SimpleDateFormat("HH:mm aa", Locale.getDefault())

    private suspend fun mapToUiState(payload: TelemetryHeartbeatPayload, status: DeviceStatus): DeviceTelemetryUiState {
        val payloadBatteryPercentage = payload.batteryPercentage ?: 0
        val batteryLevel = when {
            payloadBatteryPercentage > 30 -> BatteryLevel.NORMAL
            payloadBatteryPercentage > 15 -> BatteryLevel.WARNING
            else -> BatteryLevel.CRITICAL
        }

        val timestamp = if (payload.timestamp > 0) payload.timestamp else System.currentTimeMillis()
        val lastSeenLabel = timeFormatter.format(Date(timestamp))

        Log.i("NABDA_CaregiverViewModel", "Mapping TelemetryHeartbeatPayload to DeviceTelemetryUiState.")

        return DeviceTelemetryUiState(
            deviceStatus = status,
            rawTimestamp = timestamp,
            batteryLevel = batteryLevel,
            lastSeenLabel = lastSeenLabel,
            isCharging = payload.isCharging ?: false,
            connectivity = payload.connectivitySource,
            signalStrength = payload.signalStrength ?: 0,
            isSilentMode = payload.isSilentMode ?: false,
            batteryPercentage = payloadBatteryPercentage,
            locationLabel = lastGeocodedLocationLabel.value,
        )
    }

    /**
     * Sends an action directly to the connected deaf app
     */
    fun sendAction(caregiverAction: CaregiverAction) {
        viewModelScope.launch {
            try {
                // If we have a local host, send it directly via HTTP
                if (connectedHost.value != null) {
                    val payload = CaregiverActionPayload(
                        actionId = caregiverAction.name,
                        timestamp = System.currentTimeMillis(),
                        correlationId = UUID.randomUUID().toString(),
                    )
                    val ack = transport.sendAction(payload)
                    if (ack.success) {
                        showToast(context.getString(R.string.action_delivered))
                    } else {
                        showToast(context.getString(R.string.action_not_sent))
                    }
                    Log.i("NABDA_CaregiverViewModel", "sendAction: ACK='$ack'.")
                } else {
                    showToast(context.getString(R.string.connection_lost))
                    Log.w("NABDA_CaregiverViewModel", "No local device found for direct action")
                }
            } catch (e: Exception) {
                Log.e("NABDA_CaregiverViewModel", "Failed to send local action", e)
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    // History states
    val alerts: StateFlow<List<Alert>> = notificationManager.alertHistory
    private val _selectedFilter = MutableStateFlow<CaregiverAction?>(null) // null means "All Alerts"
    val selectedFilter = _selectedFilter.asStateFlow()

    val filteredAlerts: StateFlow<List<Alert>> = combine(alerts, _selectedFilter) { alerts, filter ->
        if (filter == null) alerts
        else alerts.filter { it.actionId == filter.name }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onFilterSelected(filter: CaregiverAction?) {
        _selectedFilter.value = filter
    }
}
