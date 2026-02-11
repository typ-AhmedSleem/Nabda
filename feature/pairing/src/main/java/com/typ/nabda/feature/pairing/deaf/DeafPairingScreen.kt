package com.typ.nabda.feature.pairing.deaf

import androidx.compose.runtime.Composable
import com.typ.nabda.feature.pairing.common.PairingInProgressContent

@Composable
fun DeafPairingScreen() {
    PairingInProgressContent()
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DeafPairingScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        DeafPairingScreen()
    }
}
