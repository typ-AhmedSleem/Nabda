package com.typ.nabda.feature.pairing.deaf

import androidx.compose.runtime.Composable
import com.typ.nabda.feature.pairing.common.PairingFailureContent

@Composable
fun DeafFailedScreen(
    onRetry: () -> Unit,
) {
    PairingFailureContent(
        onRetry = onRetry,
        title = "Pairing failed"
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DeafFailedScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        DeafFailedScreen(onRetry = {})
    }
}
