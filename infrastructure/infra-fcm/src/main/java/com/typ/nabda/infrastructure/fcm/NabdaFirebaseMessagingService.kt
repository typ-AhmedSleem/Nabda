package com.typ.nabda.infrastructure.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.typ.nabda.core.messaging.TokenRepository
import com.typ.nabda.core.notifications.NabdaNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class NabdaFirebaseMessagingService : FirebaseMessagingService() {

    private val tokenRepository: TokenRepository by inject()
    private val notificationManager: NabdaNotificationManager by inject()

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        serviceScope.launch {
            tokenRepository.updateMyToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received: ${message.data}")

        val data = message.data
        val actionId = data["actionId"]
        val actionName = data["actionName"]
        val priority = data["actionPriority"] ?: "NORMAL"

        if (actionId != null && actionName != null) {
            notificationManager.showActionNotification(actionId, actionName, priority)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel job? Service lifecycle is tricky, but SupervisionJob handles it.
    }
}
