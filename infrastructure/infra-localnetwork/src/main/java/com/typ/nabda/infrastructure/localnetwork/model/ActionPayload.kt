package com.typ.nabda.infrastructure.localnetwork.model

import kotlinx.serialization.Serializable

/**
 * Represents an action command sent from the Caregiver to the Deaf device.
 *
 * @property correlationId Unique ID for tracking request/response pairs.
 * @property action The gesture action to execute.
 * @property timestamp Epoch millis when the action was created.
 */
@Serializable
data class ActionPayload(
    val correlationId: String,
    val action: GestureAction,
    val timestamp: Long,
)

/**
 * Enumerates the possible actions that can be triggered remotely.
 */
@Serializable
enum class GestureAction {
    HELP_REQUEST,
    FALL_ALERT,
}
