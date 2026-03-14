package com.typ.nabda.feature.pairing

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typ.nabda.core.messaging.TokenRepository
import com.typ.nabda.core.model.PairedDevice
import com.typ.nabda.core.pairing.PairingRepository
import com.typ.nabda.feature.pairing.caregiver.CaregiverPairedScreen
import com.typ.nabda.feature.pairing.caregiver.CaregiverPairingInProgressScreen
import com.typ.nabda.feature.pairing.caregiver.CaregiverPairingScreen
import com.typ.nabda.feature.pairing.common.PairingFailureContent
import com.typ.nabda.feature.pairing.deaf.DeafPairedScreen
import com.typ.nabda.feature.pairing.deaf.DeafPairingScreen
import com.typ.nabda.feature.pairing.deaf.DeafQrScreen
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PairingScreen(
    isCaregiver: Boolean,
    onPairingComplete: () -> Unit,
    viewModel: PairingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(isCaregiver) {
        viewModel.setRole(isCaregiver)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val pairingState = state) {
            is PairingUiState.Loading -> CircularProgressIndicator()
            is PairingUiState.PairingInProgress -> {
                if (isCaregiver) {
                    CaregiverPairingInProgressScreen()
                } else {
                    DeafPairingScreen()
                }
            }

            is PairingUiState.Error -> {
                PairingFailureContent(
                    onRetry = {
                        if (isCaregiver) {
                            viewModel.switchToScanning()
                        } else {
                            viewModel.switchToDisplay()
                        }
                    },
                    subtitle = pairingState.message
                )
            }

            is PairingUiState.Paired -> {
                if (isCaregiver) {
                    CaregiverPairedScreen(onFinish = onPairingComplete)
                } else {
                    DeafPairedScreen(onFinish = onPairingComplete)
                }
            }

            is PairingUiState.DisplayQr -> {
                DeafQrScreen(
                    uuid = pairingState.uuid,
                    qrBitmap = pairingState.qrBitmap
                )
            }

            is PairingUiState.NeedsPairing -> {
                val ctx = LocalContext.current
                val scanQrCodeLauncher = rememberLauncherForActivityResult(ScanQRCode()) { result ->
                    when (result) {
                        is QRResult.QRError -> {
                            Log.w("QR", "QR Error", result.exception)
                            viewModel.switchToError(result.exception)
                        }

                        QRResult.QRMissingPermission -> {
                            Log.w("QR", "QR Missing Permission")
                            viewModel.switchToError(Exception("Missing Camera Permission"))
                        }

                        QRResult.QRUserCanceled -> {
                            Log.w("QR", "QR User Canceled")
                            viewModel.switchToError(Exception("User cancelled QR scanning."))
                        }

                        is QRResult.QRSuccess -> {
                            val scannedContent = result.content.rawValue?.trim()
                            Log.d("QR", "QR Success: $scannedContent")
                            if (scannedContent == null) {
                                Toast
                                    .makeText(ctx, "Invalid QR Code.", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                viewModel.submitPairingCode(scannedContent)
                            }
                        }
                    }
                }
                CaregiverPairingScreen(
                    onPairManual = { viewModel.submitPairingCode(it) },
                    onPairUsingQr = { scanQrCodeLauncher.launch(null) }
                )
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
            onPairingComplete = {}
        )
    }
}

