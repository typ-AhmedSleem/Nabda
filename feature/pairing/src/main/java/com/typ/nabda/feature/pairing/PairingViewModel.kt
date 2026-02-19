package com.typ.nabda.feature.pairing

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.nabda.core.messaging.TokenRepository
import com.typ.nabda.core.model.PairedDevice
import com.typ.nabda.core.pairing.PairingRepository
import com.typ.nabda.infrastructure.qr.QRCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

sealed interface PairingUiState {
    data object Loading : PairingUiState
    data object PairingInProgress : PairingUiState
    data class DisplayQr(val uuid: String, val qrBitmap: Bitmap?) : PairingUiState
    data object NeedsPairing : PairingUiState
    data class Paired(val partnerUuid: String) : PairingUiState
    data class Error(val message: String) : PairingUiState
}


class PairingViewModel(
    private val pairingRepository: PairingRepository,
    private val tokenRepository: TokenRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PairingUiState>(PairingUiState.Loading)
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    // init {
    //    checkPairingStatus()
    // }

    fun setRole(isCaregiver: Boolean) {
        viewModelScope.launch {
            val pairedDevice = pairingRepository.getPairedDevice()
            if (pairedDevice != null) {
                _uiState.value = PairingUiState.Paired(pairedDevice.uuid)
            } else {
                if (isCaregiver) {
                    switchToScanning()
                } else {
                    generateMyIdentity()
                }
            }
        }
    }

    private fun checkPairingStatus() {
        // Removed auto-check to allow role to drive state
    }

    private fun generateMyIdentity() {
        viewModelScope.launch {
            var myUuid = pairingRepository.getMyUUID()
            if (myUuid.isEmpty()) {
                myUuid = UUID.randomUUID().toString()
                pairingRepository.setMyUUID(myUuid)
            }
            // Generate QR
            val qr = withContext(Dispatchers.Default) {
                QRCodeGenerator.generateQRCode(myUuid)
            }
            _uiState.value = PairingUiState.DisplayQr(myUuid, qr)
        }
    }

    fun submitPairingCode(pairingCode: String) {
        viewModelScope.launch {
            if (pairingCode.length > 10) { // Basic UUID check
                _uiState.value = PairingUiState.PairingInProgress
                // Fetch Token
                val token = withContext(Dispatchers.IO) {
                    tokenRepository.getRemoteToken(pairingCode)
                }

                if (token != null) {
                    val device = PairedDevice(uuid = pairingCode, fcmToken = token)
                    pairingRepository.setPairedDevice(device)
                    _uiState.value = PairingUiState.Paired(pairingCode)
                } else {
                    _uiState.value = PairingUiState.Error("Could not find user. Make sure they are online.")
                }
            } else {
                _uiState.value = PairingUiState.Error("Invalid Pairing Code")
            }
        }
    }

    fun switchToScanning() {
        _uiState.value = PairingUiState.NeedsPairing
    }

    fun switchToDisplay() {
        generateMyIdentity()
    }

    fun switchToError(error: Exception) {
        _uiState.value = PairingUiState.Error(error.message ?: "Unknown Error")
    }
}
