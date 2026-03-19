package com.typ.nabda.feature.pairing.deaf

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.typ.nabda.feature.pairing.R
import com.typ.nabda.feature.pairing.common.PairingSuccessContent

@Composable
fun DeafPairedScreen(
    onFinish: () -> Unit,
) {
    PairingSuccessContent(
        onFinish = onFinish,
        buttonText = stringResource(R.string.finish_setup)
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DeafPairedScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        DeafPairedScreen(onFinish = {})
    }
}
