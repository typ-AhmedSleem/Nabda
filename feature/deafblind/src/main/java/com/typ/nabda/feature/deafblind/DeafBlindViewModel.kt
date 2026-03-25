package com.typ.nabda.feature.deafblind

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.nabda.core.actions.RequestedActionMapper
import com.typ.nabda.core.common.NabdaResult
import com.typ.nabda.core.dispatcher.SignalDispatcher
import com.typ.nabda.core.gestures.GestureClassifier
import com.typ.nabda.core.haptic.HapticEngine
import com.typ.nabda.core.messaging.IncomingActionDispatcher
import com.typ.nabda.core.model.GestureInput
import com.typ.nabda.core.model.GestureType
import com.typ.nabda.core.model.HapticEnginePattern
import com.typ.nabda.core.model.RequestedAction
import com.typ.nabda.designsystem.UiText
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
    val feedbackMessage: UiText = UiText.StringResource(R.string.ready),
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

    private var pendingRequestedAction: RequestedAction? = null
    private var jobActionConfirmation: Job? = null
    private var jobMakingTextReady: Job? = null

    init {
        // Observe connected clients from LocalServerRegistry
        viewModelScope.launch {
            LocalServerRegistry.connectedClientsCount.collect { count ->
                _uiState.update { it.copy(connectedClientsCount = count) }
            }
        }
    }

    fun onPointersChanged(pointers: Map<Int, Offset>) {
        _uiState.update { it.copy(pointers = pointers) }
    }

    fun onGestureInput(input: GestureInput) {
        if (_uiState.value.connectedClientsCount == 0) {
            _uiState.update { it.copy(feedbackMessage = UiText.StringResource(R.string.not_connected)) }
            hapticEngine.performHaptic(HapticEnginePattern.NotConnected)
            viewModelScope.launch {
                delay(2000)
                _uiState.update { it.copy(feedbackMessage = UiText.StringResource(R.string.ready)) }
            }
            return
        }

        if (_uiState.value.isWaitingForConfirmation) {
            handleConfirmation(input)
        } else {
            initiateAction(input)
        }
    }

    private fun initiateAction(input: GestureInput) {
        hapticEngine.performHaptic(HapticEnginePattern.NormalRequest)
        val gesture = mapInputToGesture(input)
        val action = RequestedActionMapper.getActionForGesture(gesture)

        if (action != null) {
            jobMakingTextReady?.cancel()
            pendingRequestedAction = action
            _uiState.update {
                it.copy(
                    feedbackMessage = UiText.StringResource(R.string.confirm_action_format, action.name),
                    isWaitingForConfirmation = true
                )
            }
            hapticEngine.performHaptic(HapticEnginePattern.ConfirmActionAgain)

            jobActionConfirmation?.cancel()
            jobActionConfirmation = viewModelScope.launch {
                delay(5000) // 5 seconds to confirm
                cancelPendingAction(UiText.StringResource(R.string.timed_out))
            }
        }
    }

    private fun handleConfirmation(input: GestureInput) {
        val action = pendingRequestedAction
        if (action != null) {
            jobMakingTextReady?.cancel()
            val gesture = mapInputToGesture(input)
            val newAction = RequestedActionMapper.getActionForGesture(gesture)
            // * Check if same action is performed before confirming
            if (action.id == newAction?.id) {
                confirmAction(action)
            } else {
                // * Cancel pending action if wrong gesture
                cancelPendingAction(UiText.StringResource(R.string.wrong_gesture))
            }
        }
    }

    private fun confirmAction(requestedAction: RequestedAction) {
        jobActionConfirmation?.cancel()
        _uiState.update {
            it.copy(
                feedbackMessage = UiText.StringResource(R.string.sending),
                isWaitingForConfirmation = false
            )
        }

        viewModelScope.launch {
            val result = signalDispatcher.dispatchAction(requestedAction)
            if (result is NabdaResult.Success) {
                _uiState.update { it.copy(feedbackMessage = UiText.StringResource(R.string.sent)) }
                hapticEngine.performHaptic(HapticEnginePattern.ActionSent)
            } else {
                _uiState.update { it.copy(feedbackMessage = UiText.StringResource(R.string.failed)) }
                hapticEngine.performHaptic(HapticEnginePattern.ActionNotConfirmed)
            }
            jobMakingTextReady?.cancel()
            delay(2000)
            _uiState.update { it.copy(feedbackMessage = UiText.StringResource(R.string.ready)) }
        }
    }

    private fun cancelPendingAction(reason: UiText) {
        pendingRequestedAction = null
        _uiState.update {
            it.copy(
                feedbackMessage = reason,
                isWaitingForConfirmation = false
            )
        }
        jobMakingTextReady = viewModelScope.launch {
            delay(2000)
            _uiState.update { it.copy(feedbackMessage = UiText.StringResource(R.string.ready)) }
        }
    }

    private fun mapInputToGesture(input: GestureInput): GestureType {
        return GestureClassifier.classify(input)
    }
}
