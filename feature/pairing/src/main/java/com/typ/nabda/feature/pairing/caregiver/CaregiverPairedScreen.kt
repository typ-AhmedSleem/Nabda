package com.typ.nabda.feature.pairing.caregiver

import androidx.compose.runtime.Composable
import com.typ.nabda.feature.pairing.common.PairingSuccessContent

@Composable
fun CaregiverPairedScreen(
    onFinish: () -> Unit,
) {
    PairingSuccessContent(
        onFinish = onFinish,
        buttonText = "Finish Setup"
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CaregiverPairedScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        CaregiverPairedScreen(onFinish = {})
    }
}
