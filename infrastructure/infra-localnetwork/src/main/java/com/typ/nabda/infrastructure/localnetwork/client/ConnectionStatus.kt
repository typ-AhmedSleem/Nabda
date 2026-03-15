package com.typ.nabda.infrastructure.localnetwork.client

enum class ConnectionStatus {
    IDLE,
    SCANNING,
    PAIRING,
    PAIRED,
    FAILED,
    WIFI_DISABLED
}
