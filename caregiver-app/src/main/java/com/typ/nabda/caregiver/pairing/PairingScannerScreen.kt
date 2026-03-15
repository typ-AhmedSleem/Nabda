package com.typ.nabda.caregiver.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typ.nabda.infrastructure.localnetwork.client.ConnectionStatus

@Composable
fun PairingScannerScreen(
    viewModel: WifiPairingViewModel,
    onPairingSuccess: () -> Unit,
) {
    val status by viewModel.pairingStatus.collectAsState()

    LaunchedEffect(status) {
        if (status == ConnectionStatus.PAIRED) {
            onPairingSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Pairing Caregiver",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = when (status) {
                    ConnectionStatus.SCANNING -> MaterialTheme.colorScheme.primary
                    ConnectionStatus.PAIRING -> Color(0xFFFF9800)
                    ConnectionStatus.PAIRED -> Color(0xFF4CAF50)
                    else -> MaterialTheme.colorScheme.error
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (status) {
                    ConnectionStatus.IDLE -> "Starting scanning..."
                    ConnectionStatus.SCANNING -> "Searching for Nabda Device..."
                    ConnectionStatus.PAIRING -> "Connecting to device..."
                    ConnectionStatus.PAIRED -> "Connected and paired!"
                    ConnectionStatus.FAILED -> "Discovery or pairing failed."
                    ConnectionStatus.WIFI_DISABLED -> "WiFi is disabled. Please enable it."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (status == ConnectionStatus.FAILED) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* Could trigger retry in Service if exposed */ }) {
                    Text("Retry")
                }
            }
        }
    }
}
