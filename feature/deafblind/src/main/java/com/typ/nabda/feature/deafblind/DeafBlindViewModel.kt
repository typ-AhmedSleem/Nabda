package com.typ.nabda.feature.deafblind

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.nabda.core.actions.ActionMapper
import com.typ.nabda.core.common.NabdaResult
import com.typ.nabda.core.dispatcher.SignalDispatcher
import com.typ.nabda.core.gestures.GestureClassifier
import com.typ.nabda.core.messaging.ActionHandler
import com.typ.nabda.core.messaging.IncomingActionDispatcher
import com.typ.nabda.core.model.Action
import com.typ.nabda.core.model.GestureInput
import com.typ.nabda.core.model.GestureType
import com.typ.nabda.feature.deafblind.localserver.LocalServerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

data class DeafBlindUiState(
    val lastAction: Action? = null,
    val feedbackMessage: String = "Waiting for action...",
    val isSending: Boolean = false,
    val pointers: Map<Int, Offset> = emptyMap(),
)

class DeafBlindViewModel(
    private val signalDispatcher: SignalDispatcher,
    context: Context,
    incomingActionDispatcher: IncomingActionDispatcher,
) : ViewModel(), ActionHandler, KoinComponent {

    private val _uiState = MutableStateFlow(DeafBlindUiState())
    val uiState: StateFlow<DeafBlindUiState> = _uiState.asStateFlow()

    init {
        LocalServerService.start(context)

        // Observe incoming actions from both FCM and Local Server
        incomingActionDispatcher.actions
            .onEach { onActionReceived(it) }
            .launchIn(viewModelScope)
    }

    override fun onActionReceived(action: Action) {
        _uiState.value = _uiState.value.copy(
            lastAction = action,
            feedbackMessage = "REMOTE ACTION: ${action.name}"
        )
    }

    fun onPointersChanged(pointers: Map<Int, Offset>) {
        _uiState.value = _uiState.value.copy(pointers = pointers)
    }

    fun onGestureInput(input: GestureInput) {
        val gesture = GestureClassifier.classify(input)

        if (gesture == GestureType.UNKNOWN) {
            _uiState.value = _uiState.value.copy(feedbackMessage = "Unknown Gesture")
            return
        }

        val action = ActionMapper.getActionForGesture(gesture)
        if (action != null) {
            _uiState.value = _uiState.value.copy(
                lastAction = action,
                feedbackMessage = "Detected: ${action.name}",
                isSending = true
            )

            sendAction(action)
        } else {
            _uiState.value = _uiState.value.copy(feedbackMessage = "No Action for ${gesture.name}")
        }
    }

    private fun sendAction(action: Action) {
        viewModelScope.launch {
            when (val result = signalDispatcher.dispatchAction(action)) {
                is NabdaResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        feedbackMessage = "Sent: ${action.name}",
                        isSending = false
                    )
                }

                is NabdaResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        feedbackMessage = "Failed: ${result.exception.message}",
                        isSending = false
                    )
                }

                else -> {}
            }
        }
    }
}
