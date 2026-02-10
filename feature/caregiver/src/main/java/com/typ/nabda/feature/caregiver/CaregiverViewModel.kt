package com.typ.nabda.feature.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.nabda.core.notifications.Alert
import com.typ.nabda.core.notifications.NabdaNotificationManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CaregiverViewModel(
    private val notificationManager: NabdaNotificationManager,
) : ViewModel() {

    val alerts: StateFlow<List<Alert>> = notificationManager.alertHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
