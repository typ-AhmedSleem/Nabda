package com.typ.nabda.infrastructure.localnetwork.model

import kotlinx.serialization.Serializable

/**
 * Acknowledgement response returned after processing an [ActionPayload].
 *
 * @property correlationId Matches the original [ActionPayload.correlationId].
 * @property success Whether the action was handled successfully.
 * @property message Optional human-readable status or error message.
 */
@Serializable
data class ActionAckPayload(
    val correlationId: String,
    val success: Boolean,
    val message: String? = null,
)
