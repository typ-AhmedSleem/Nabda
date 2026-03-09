package com.typ.nabda.core.pairing

import com.typ.nabda.core.model.PairedDevice
import kotlinx.coroutines.flow.Flow

interface PairingRepository {
    // Defines who I am paired with (The Caregiver)
    suspend fun getPairedDevice(): PairedDevice?
    suspend fun setPairedDevice(device: PairedDevice)
    suspend fun clearPairedDevice()
    fun getPairedDeviceFlow(): Flow<PairedDevice?>

    // Defines who is paired with me (The DeafBlind User) - mainly for Caregiver app to know
    suspend fun setMyUUID(uuid: String)
    suspend fun getMyUUID(): String
}
