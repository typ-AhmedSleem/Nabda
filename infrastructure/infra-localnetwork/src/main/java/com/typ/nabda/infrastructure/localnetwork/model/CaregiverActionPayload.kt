package com.typ.nabda.infrastructure.localnetwork.model

import kotlinx.serialization.Serializable

/**
 * Represents an action command payload sent from Caregiver to Deaf app.
 */
@Serializable
data class CaregiverActionPayload(
    val correlationId: String,
    val actionId: String,
    val timestamp: Long,
)
