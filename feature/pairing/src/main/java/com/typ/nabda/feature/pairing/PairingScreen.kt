package com.typ.nabda.feature.pairing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.typ.nabda.core.messaging.TokenRepository
import com.typ.nabda.core.model.PairedDevice
import com.typ.nabda.core.pairing.PairingRepository
import com.typ.nabda.feature.pairing.deaf.DeafFailedScreen
import com.typ.nabda.feature.pairing.deaf.DeafPairedScreen
import com.typ.nabda.feature.pairing.deaf.DeafQrScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
            is PairingUiState.PairingInProgress -> {
                if (isCaregiver) {
                    com.typ.nabda.feature.pairing.caregiver.CaregiverPairingInProgressScreen()
                } else {
                    com.typ.nabda.feature.pairing.deaf.DeafPairingScreen()
                }
            }

            is PairingUiState.Error -> {
                if (isCaregiver) {
                    com.typ.nabda.feature.pairing.caregiver.CaregiverFailedScreen(onRetry = { viewModel.switchToScanning() })
                } else {
                    DeafFailedScreen(onRetry = { viewModel.switchToDisplay() })
                }
            }

            is PairingUiState.Paired -> {
                if (isCaregiver) {
                    com.typ.nabda.feature.pairing.caregiver.CaregiverPairedScreen(onFinish = onPairingComplete)
                } else {
                    DeafPairedScreen(onFinish = onPairingComplete)
                }
            }

            is PairingUiState.DisplayQr -> {
                DeafQrScreen(
                    uuid = s.uuid,
                    qrBitmap = s.qrBitmap
                )
            }

            is PairingUiState.Scanning -> {
                if (isCaregiver) {
                    com.typ.nabda.feature.pairing.caregiver.CaregiverPairingScreen(
                        onPairManual = { viewModel.onQrScanned(it) },
                        onPairUsingQr = { /* For now, just show camera if needed, or keep in this selection state */ }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (!LocalView.current.isInEditMode) {
                            CameraScreen(onCodeScanned = { viewModel.onQrScanned(it) })
                        }
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
}

@Preview(showBackground = true)
@Composable
private fun PairingScreenCaregiverPreview() {
    val vm = remember {
        PairingViewModel(
            pairingRepository = object : PairingRepository {
                override suspend fun getPairedDevice(): PairedDevice? {
                    return null
                }

                override suspend fun setPairedDevice(device: PairedDevice) {
                }

                override suspend fun clearPairedDevice() {
                }

                override fun getPairedDeviceFlow(): Flow<PairedDevice?> {
                    return flow {}
                }

                override suspend fun setMyUUID(uuid: String) {
                }

                override suspend fun getMyUUID(): String {
                    return ":)"
                }

            },
            tokenRepository = object : TokenRepository {
                override suspend fun updateMyToken(token: String) {
                }

                override suspend fun getRemoteToken(uuid: String): String? {
                    return null
                }

            }
        )
    }
    MaterialTheme {
        PairingScreen(
            isCaregiver = true,
            viewModel = vm,
            onPairingComplete = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PairingScreenDeafPreview() {
    val vm = remember {
        PairingViewModel(
            pairingRepository = object : PairingRepository {
                override suspend fun getPairedDevice(): PairedDevice? {
                    return null
                }

                override suspend fun setPairedDevice(device: PairedDevice) {
                }

                override suspend fun clearPairedDevice() {
                }

                override fun getPairedDeviceFlow(): Flow<PairedDevice?> {
                    return flow {}
                }

                override suspend fun setMyUUID(uuid: String) {
                }

                override suspend fun getMyUUID(): String {
                    return ":)"
                }

            },
            tokenRepository = object : TokenRepository {
                override suspend fun updateMyToken(token: String) {
                }

                override suspend fun getRemoteToken(uuid: String): String? {
                    return null
                }

            }
        )
    }
    MaterialTheme {
        PairingScreen(
            isCaregiver = false,
            viewModel = vm,
            onPairingComplete = {})
    }
}

