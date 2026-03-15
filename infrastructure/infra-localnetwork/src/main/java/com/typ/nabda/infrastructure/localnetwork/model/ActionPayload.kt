package com.typ.nabda.infrastructure.localnetwork.model

import com.typ.nabda.core.model.ActionPriority
import kotlinx.serialization.Serializable

/**
 * Represents an action command sent from the Caregiver to the Deaf device.
 */
@Serializable
data class ActionPayload(
    val correlationId: String,
    val action: GestureAction,
    val title: String,
    val priority: ActionPriority,
    val timestamp: Long,
)
