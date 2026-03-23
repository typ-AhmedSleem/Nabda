package com.typ.nabda.infrastructure.localnetwork.client

import com.typ.nabda.infrastructure.localnetwork.R

enum class ConnectionStatus(val resId: Int) {
    IDLE(R.string.status_idle),
    SCANNING(R.string.status_scanning),
    PAIRING(R.string.status_pairing),
    CONNECTING(R.string.status_connecting),
    CONNECTED(R.string.status_paired),
    DISCONNECTED(R.string.status_disconnected),
    RECONNECTING(R.string.status_reconnecting),
    FAILED(R.string.status_failed),
    WIFI_DISABLED(R.string.status_wifi_disabled);
}

enum class ServerStatus {
    OFFLINE,
    STARTING,
    RUNNING,
    ERROR
}
