package com.typ.nabda.feature.pairing.deaf

import androidx.compose.runtime.Composable
import com.typ.nabda.feature.pairing.common.PairingFailureContent

@Composable
fun DeafFailedScreen(
    title: String = "Pairing failed",
    subtitle: String = "Something went wrong during the connection process. Please ensure both devices are online and try again.",
    buttonText: String = "Retry",
    onRetry: () -> Unit,
) {
    PairingFailureContent(
        onRetry = onRetry,
        title = title,
        subtitle = subtitle,
        buttonText = buttonText
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DeafFailedScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        DeafFailedScreen(onRetry = {})
    }
}
