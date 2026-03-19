package com.typ.nabda.infrastructure.localnetwork.client

import com.typ.nabda.infrastructure.localnetwork.R

enum class ConnectionStatus(val resId: Int) {
    IDLE(R.string.status_idle),
    SCANNING(R.string.status_scanning),
    PAIRING(R.string.status_pairing),
    PAIRED(R.string.status_paired),
    FAILED(R.string.status_failed),
    WIFI_DISABLED(R.string.status_wifi_disabled);
}
