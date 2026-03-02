package com.typ.nabda.feature.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.nabda.core.model.Alert
import com.typ.nabda.core.model.SupportedAction
import com.typ.nabda.core.model.TelemetryRepository
import com.typ.nabda.core.notifications.NabdaNotificationManager
import com.typ.nabda.core.pairing.PairingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaregiverViewModel(
    private val notificationManager: NabdaNotificationManager,
    private val telemetryRepository: TelemetryRepository,
    private val pairingRepository: PairingRepository,
    private val geocoder: LocationGeocoder,
) : ViewModel() {

    // Dashboard states
    val isInternetConnected = MutableStateFlow(true).asStateFlow()
    val isNotificationPermissionGranted = MutableStateFlow(true).asStateFlow()
    val isCameraPermissionGranted = MutableStateFlow(true).asStateFlow()
    val isPhoneSilent = MutableStateFlow(false).asStateFlow()

    // Telemetry State
    @OptIn(ExperimentalCoroutinesApi::class)
    val telemetryUiState: StateFlow<DeviceTelemetryUiState?> = pairingRepository.getPairedDeviceFlow()
        .flatMapLatest { device ->
            if (device == null) flowOf(null)
            else {
                val caregiverId = pairingRepository.getMyUUID()
                telemetryRepository.observeLatest(caregiverId, device.uuid).map { payload ->
                    if (payload == null) null
                    else {
                        val now = System.currentTimeMillis()
                        val diff = now - payload.timestamp
                        val status = when {
                            diff < 7 * 60 * 1000 -> DeviceStatus.ONLINE
                            diff < 15 * 60 * 1000 -> DeviceStatus.DELAYED
                            else -> DeviceStatus.OFFLINE
                        }

                        val batteryLevel = when {
                            payload.batteryPercentage > 30 -> BatteryLevel.NORMAL
                            payload.batteryPercentage > 15 -> BatteryLevel.WARNING
                            else -> BatteryLevel.CRITICAL
                        }

                        val lastSeenLabel = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(payload.timestamp))

                        DeviceTelemetryUiState(
                            deviceStatus = status,
                            batteryLevel = batteryLevel,
                            batteryPercentage = payload.batteryPercentage,
                            isCharging = payload.isCharging,
                            connectivity = payload.connectivitySource,
                            locationLabel = geocoder.geocode(payload.location),
                            lastSeenLabel = "Last seen: $lastSeenLabel"
                        )
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

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
