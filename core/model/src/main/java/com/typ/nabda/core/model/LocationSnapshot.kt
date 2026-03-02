package com.typ.nabda.core.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
)
