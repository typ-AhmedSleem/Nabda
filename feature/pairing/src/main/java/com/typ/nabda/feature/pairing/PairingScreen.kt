package com.typ.nabda.feature.pairing

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PairingScreen(
    isCaregiver: Boolean,
    onPairingComplete: () -> Unit,
    viewModel: PairingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(isCaregiver) {
        viewModel.setRole(isCaregiver)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val s = state) {
            is PairingUiState.Loading -> CircularProgressIndicator()
            is PairingUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.switchToDisplay() }) {
                        Text("Retry")
                    }
                }
            }

            is PairingUiState.Paired -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Paired with: ${s.partnerUuid}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onPairingComplete) {
                        Text("Continue")
                    }
                }
            }

            is PairingUiState.DisplayQr -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Deaf-Blind User: Scan this code", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(24.dp))
                    s.qrBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(300.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("My UUID: ${s.uuid.take(8)}...")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.switchToScanning() }) {
                        Text("I am Deaf-Blind (Scan Code)")
                    }
                }
            }

            is PairingUiState.Scanning -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraScreen(onCodeScanned = { viewModel.onQrScanned(it) })
                    Button(
                        onClick = { viewModel.switchToDisplay() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(32.dp)
                    ) {
                        Text("Cancel Scanning")
                    }
                }
            }
        }
    }
}
