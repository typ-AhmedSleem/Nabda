package com.typ.nabda.infrastructure.localnetwork.server

import com.typ.nabda.infrastructure.localnetwork.client.ServerStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

private typealias CaregiverActionId = String

object LocalServerRegistry {
    var activeServer: LocalKtorServer? = null

    private val _status = MutableStateFlow(ServerStatus.OFFLINE)
    val status: StateFlow<ServerStatus> = _status.asStateFlow()

    private val _connectedClientsCount = MutableStateFlow(0)
    val connectedClientsCount: StateFlow<Int> = _connectedClientsCount.asStateFlow()

    private val _acks = MutableSharedFlow<CaregiverActionId>(extraBufferCapacity = 5)
    val acks: SharedFlow<CaregiverActionId> = _acks.asSharedFlow()

    fun updateStatus(newStatus: ServerStatus) {
        _status.value = newStatus
    }

    fun updateClientsCount(count: Int) {
        _connectedClientsCount.value = count
    }

    fun emitAckForCaregiverAction(actionId: CaregiverActionId) {
        _acks.tryEmit(actionId)
    }
}
