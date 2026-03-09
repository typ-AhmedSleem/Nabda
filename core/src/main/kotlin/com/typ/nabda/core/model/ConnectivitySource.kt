package com.typ.nabda.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ConnectivitySource {
    WIFI,
    CELLULAR,
    NONE
}
