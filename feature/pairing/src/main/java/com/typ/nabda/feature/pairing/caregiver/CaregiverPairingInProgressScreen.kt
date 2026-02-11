package com.typ.nabda.feature.pairing.caregiver

import androidx.compose.runtime.Composable
import com.typ.nabda.feature.pairing.common.PairingInProgressContent

@Composable
fun CaregiverPairingInProgressScreen() {
    PairingInProgressContent()
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CaregiverPairingInProgressScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        CaregiverPairingInProgressScreen()
    }
}
