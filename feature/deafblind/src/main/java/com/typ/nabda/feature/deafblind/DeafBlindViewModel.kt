package com.typ.nabda.feature.deafblind

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.nabda.core.actions.ActionMapper
import com.typ.nabda.core.common.NabdaResult
import com.typ.nabda.core.dispatcher.SignalDispatcher
import com.typ.nabda.core.haptic.HapticEngine
import com.typ.nabda.core.messaging.IncomingActionDispatcher
import com.typ.nabda.core.model.Action
import com.typ.nabda.core.model.GestureInput
import com.typ.nabda.core.model.GestureType
import com.typ.nabda.core.model.HapticEnginePattern
import com.typ.nabda.infrastructure.localnetwork.server.LocalServerRegistry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeafBlindUiState(
    val pointers: Map<Int, Offset> = emptyMap(),
    val feedbackMessage: String = "READY",
    val isWaitingForConfirmation: Boolean = false,
    val connectedClientsCount: Int = 0,
)

class DeafBlindViewModel(
    private val signalDispatcher: SignalDispatcher,
    private val hapticEngine: HapticEngine,
    private val incomingActionDispatcher: IncomingActionDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeafBlindUiState())
    val uiState: StateFlow<DeafBlindUiState> = _uiState.asStateFlow()

    private var pendingAction: Action? = null
    private var confirmationJob: Job? = null

    init {
        // Observe connected clients from LocalServerRegistry
        viewModelScope.launch {
            while (true) {
                val count = LocalServerRegistry.activeServer?.getConnectedClientsCount() ?: 0
                _uiState.update { it.copy(connectedClientsCount = count) }
                delay(2000)
            }
        }
    }

    fun onPointersChanged(pointers: Map<Int, Offset>) {
        _uiState.update { it.copy(pointers = pointers) }
    }

    fun onGestureInput(input: GestureInput) {
        if (_uiState.value.isWaitingForConfirmation) {
            handleConfirmation(input)
        } else {
            initiateAction(input)
        }
    }

    private fun initiateAction(input: GestureInput) {
        val gesture = mapInputToGesture(input)
        val action = ActionMapper.getActionForGesture(gesture)

        if (action != null) {
            pendingAction = action
            _uiState.update {
                it.copy(
                    feedbackMessage = "CONFIRM: ${action.name}?",
                    isWaitingForConfirmation = true
                )
            }
            hapticEngine.performHaptic(HapticEnginePattern.ConfirmActionAgain)

            confirmationJob?.cancel()
            confirmationJob = viewModelScope.launch {
                delay(5000) // 5 seconds to confirm
                cancelPendingAction("TIMED OUT")
            }
        }
    }

    private fun handleConfirmation(input: GestureInput) {
        val action = pendingAction
        if (action != null) {
            val gesture = mapInputToGesture(input)
            val newAction = ActionMapper.getActionForGesture(gesture)
            // * Check if same action is performed before confirming
            if (action.id == newAction?.id) {
                confirmAction(action)
            } else {
                // * Cancel pending action if wrong gesture
                cancelPendingAction("WRONG GESTURE")
            }
        }
    }

    private fun confirmAction(action: Action) {
        confirmationJob?.cancel()
        _uiState.update {
            it.copy(
                feedbackMessage = "SENDING...",
                isWaitingForConfirmation = false
            )
        }

        viewModelScope.launch {
            val result = signalDispatcher.dispatchAction(action)
            if (result is NabdaResult.Success) {
                _uiState.update { it.copy(feedbackMessage = "SENT ✅") }
                hapticEngine.performHaptic(HapticEnginePattern.ActionSent)
            } else {
                _uiState.update { it.copy(feedbackMessage = "FAILED ❌") }
                hapticEngine.performHaptic(HapticEnginePattern.ActionNotConfirmed)
            }
            delay(2000)
            _uiState.update { it.copy(feedbackMessage = "READY") }
        }
    }

    private fun cancelPendingAction(reason: String) {
        pendingAction = null
        _uiState.update {
            it.copy(
                feedbackMessage = reason,
                isWaitingForConfirmation = false
            )
        }
        viewModelScope.launch {
            delay(2000)
            _uiState.update { it.copy(feedbackMessage = "READY") }
        }
    }

    private fun mapInputToGesture(input: GestureInput): GestureType {
        return when (input.fingerCount) {
            1 -> when (input.direction) {
                com.typ.nabda.core.model.GestureDirection.UP -> GestureType.ONE_FINGER_SWIPE_UP
                com.typ.nabda.core.model.GestureDirection.DOWN -> GestureType.ONE_FINGER_SWIPE_DOWN
                com.typ.nabda.core.model.GestureDirection.RIGHT -> GestureType.ONE_FINGER_SWIPE_RIGHT
                else -> GestureType.UNKNOWN
            }

            2 -> when (input.direction) {
                com.typ.nabda.core.model.GestureDirection.UP -> GestureType.TWO_FINGER_SWIPE_UP
                com.typ.nabda.core.model.GestureDirection.RIGHT -> GestureType.TWO_FINGER_SWIPE_RIGHT
                else -> GestureType.UNKNOWN
            }

            3 -> GestureType.THREE_FINGER_SWIPE_DOWN
            else -> GestureType.UNKNOWN
        }
    }
}
