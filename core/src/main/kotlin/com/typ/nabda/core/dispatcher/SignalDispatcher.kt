package com.typ.nabda.core.dispatcher

import com.typ.nabda.core.common.NabdaResult
import com.typ.nabda.core.model.RequestedAction

interface SignalDispatcher {
    suspend fun dispatchAction(requestedAction: RequestedAction): NabdaResult<Unit>
}

/*class SignalDispatcherImpl(
    private val pairingRepository: PairingRepository,
    private val tokenRepository: TokenRepository,
    private val messageSender: MessageSender,
) : SignalDispatcher {

    override suspend fun dispatchAction(action: Action): NabdaResult<Unit> {
        // 1. Get Paired Device UUID
        val pairedDevice = pairingRepository.getPairedDeviceFlow().firstOrNull()
            ?: return NabdaResult.Error(Exception("No paired device found"))

        // 2. Refresh Token (or use cached if valid?) - Always fetch from Firestore for reliability in this demo
        // (Since "No backend" to push token updates to us efficiently, we pull on send)
        val token = tokenRepository.getRemoteToken(pairedDevice.uuid)
            ?: pairedDevice.fcmToken // Fallback to last known token

        // 3. Send
        return messageSender.sendAction(token, action)
    }
}*/
