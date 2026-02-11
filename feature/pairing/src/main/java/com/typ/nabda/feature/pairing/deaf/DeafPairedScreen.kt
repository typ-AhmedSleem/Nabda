package com.typ.nabda.feature.pairing.deaf

import androidx.compose.runtime.Composable
import com.typ.nabda.feature.pairing.common.PairingSuccessContent

@Composable
fun DeafPairedScreen(
    onFinish: () -> Unit,
) {
    PairingSuccessContent(
        onFinish = onFinish,
        buttonText = "Finish Setup"
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DeafPairedScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        DeafPairedScreen(onFinish = {})
    }
}
