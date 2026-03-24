package com.typ.nabda.core.messaging

import com.typ.nabda.core.model.RequestedAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * A shared bridge for incoming actions received via FCM or Local HTTP.
 * This allows specialized components (like Services) to dispatch actions
 * that UI components (like ViewModels) can observe.
 */
class IncomingActionDispatcher : ActionHandler {
    private val _actions = MutableSharedFlow<RequestedAction>(extraBufferCapacity = 16)
    val actions: SharedFlow<RequestedAction> = _actions.asSharedFlow()

    override fun onActionReceived(requestedAction: RequestedAction) {
        _actions.tryEmit(requestedAction)
    }
}
