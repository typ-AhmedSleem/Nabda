package com.typ.nabda.feature.pairing.caregiver

import androidx.compose.runtime.Composable
import com.typ.nabda.feature.pairing.common.PairingFailureContent

@Composable
fun CaregiverFailedScreen(
    onRetry: () -> Unit,
) {
    PairingFailureContent(
        onRetry = onRetry,
        title = "Pairing failed"
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CaregiverFailedScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        CaregiverFailedScreen(onRetry = {})
    }
}
