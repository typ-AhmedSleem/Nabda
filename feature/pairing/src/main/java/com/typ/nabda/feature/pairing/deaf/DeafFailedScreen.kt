package com.typ.nabda.feature.pairing.deaf

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.typ.nabda.feature.pairing.R
import com.typ.nabda.feature.pairing.common.PairingFailureContent

@Composable
fun DeafFailedScreen(
    title: String = stringResource(R.string.pairing_failed),
    subtitle: String = stringResource(R.string.pairing_failed_subtitle),
    buttonText: String = stringResource(R.string.retry),
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
