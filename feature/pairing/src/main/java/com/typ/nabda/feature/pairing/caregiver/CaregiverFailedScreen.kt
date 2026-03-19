package com.typ.nabda.feature.pairing.caregiver

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.typ.nabda.feature.pairing.R
import com.typ.nabda.feature.pairing.common.PairingFailureContent

@Composable
fun CaregiverFailedScreen(
    title: String = stringResource(R.string.pairing_failed),
    onRetry: () -> Unit,
) {
    PairingFailureContent(
        onRetry = onRetry,
        title = title
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CaregiverFailedScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        CaregiverFailedScreen(onRetry = {})
    }
}
