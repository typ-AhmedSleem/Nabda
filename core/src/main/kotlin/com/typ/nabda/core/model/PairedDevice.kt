package com.typ.nabda.core.model

import kotlinx.serialization.Serializable

@Serializable
data class PairedDevice(
    val uuid: String,
    val fcmToken: String,
    val name: String? = null,
    val pairedAt: Long = System.currentTimeMillis(),
)
