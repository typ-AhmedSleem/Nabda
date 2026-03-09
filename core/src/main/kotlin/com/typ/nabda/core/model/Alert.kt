package com.typ.nabda.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Alert(
    val actionId: String,
    val actionName: String,
    val priority: ActionPriority,
    val timestamp: Long = System.currentTimeMillis(),
)
