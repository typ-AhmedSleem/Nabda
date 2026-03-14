package com.typ.nabda.core.model

import kotlinx.serialization.Serializable

enum class ActionPriority {
    NORMAL,
    ASSISTANCE,
    EMERGENCY
}

@Serializable
data class Action(
    val id: String,
    val name: String,
    val description: String,
    val priority: ActionPriority,
)
