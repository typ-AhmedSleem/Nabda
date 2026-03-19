package com.typ.nabda.infrastructure.localnetwork.model

import kotlinx.serialization.Serializable

@Serializable
enum class GestureAction {
    HELP_REQUEST,
    FALL_ALERT,
    UNKNOWN
}
