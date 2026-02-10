package com.typ.nabda.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.typ.nabda.core.model.ActionPriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Alert(
    val actionId: String,
    val actionName: String,
    val priority: ActionPriority,
    val timestamp: Long = System.currentTimeMillis(),
)

interface NabdaNotificationManager {
    fun showActionNotification(actionId: String, actionName: String, priority: String)
    val alertHistory: StateFlow<List<Alert>>
}

class NabdaNotificationManagerImpl(
    private val context: Context,
) : NabdaNotificationManager {

    private val _alertHistory = MutableStateFlow<List<Alert>>(emptyList())
    override val alertHistory = _alertHistory.asStateFlow()

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID_HIGH = "nabda_urgent"
        private const val CHANNEL_ID_NORMAL = "nabda_normal"
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val highChannel = NotificationChannel(
                CHANNEL_ID_HIGH,
                "Urgent Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for urgent assistance and emergencies"
                enableVibration(true)
            }

            val normalChannel = NotificationChannel(
                CHANNEL_ID_NORMAL,
                "Normal Requests",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for routine requests"
            }

            notificationManager.createNotificationChannels(listOf(highChannel, normalChannel))
        }
    }

    override fun showActionNotification(actionId: String, actionName: String, priorityName: String) {
        val priority = try {
            ActionPriority.valueOf(priorityName)
        } catch (e: Exception) {
            ActionPriority.NORMAL
        }

        // Add to history
        val alert = Alert(actionId, actionName, priority)
        _alertHistory.value = listOf(alert) + _alertHistory.value

        val channelId = when (priority) {
            ActionPriority.EMERGENCY, ActionPriority.ASSISTANCE -> CHANNEL_ID_HIGH
            else -> CHANNEL_ID_NORMAL
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Placeholder icon
            .setContentTitle("Nabda Alert")
            .setContentText(actionName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // TODO: Add PendingIntent to open Activity

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
