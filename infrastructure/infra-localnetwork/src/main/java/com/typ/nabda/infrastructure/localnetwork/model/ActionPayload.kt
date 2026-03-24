package com.typ.nabda.infrastructure.localnetwork.model

import com.typ.nabda.core.model.ActionPriority
import kotlinx.serialization.Serializable

/**
 * Represents an action command payload sent between Caregiver and Deaf apps.
 */
@Serializable
data class ActionPayload(
    val correlationId: String,
    val actionId: String,
    val title: String,
    val priority: ActionPriority,
    val timestamp: Long,
)
