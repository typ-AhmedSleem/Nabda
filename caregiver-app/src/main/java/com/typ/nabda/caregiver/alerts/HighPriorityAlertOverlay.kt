package com.typ.nabda.caregiver.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typ.nabda.caregiver.R
import com.typ.nabda.core.haptic.HapticEngine
import com.typ.nabda.core.model.ActionPriority
import com.typ.nabda.core.model.HapticEnginePattern
import com.typ.nabda.designsystem.theme.NabdaTheme
import com.typ.nabda.infrastructure.localnetwork.model.ActionPayload
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.compose.koinInject

@Composable
fun HighPriorityAlertOverlay(
    alert: ActionPayload,
    onReceived: () -> Unit,
    hapticEngine: HapticEngine = koinInject(),
) {
    // This overlay should trigger continuous vibration and sound

    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            while (isActive) {
                hapticEngine.performHaptic(HapticEnginePattern.FallAlert)
                delay(2000) // Repeat pattern every 2 seconds
            }
        }
    }

    if (isVisible) {
        HighPriorityAlertOverlayContent(
            alert = alert,
            onDismiss = {
                isVisible = false
                onReceived()
            }
        )
    }
}

@Composable
private fun HighPriorityAlertOverlayContent(
    alert: ActionPayload,
    onDismiss: () -> Unit,
) {
    val containerColor = if (alert.priority == ActionPriority.ASSISTANCE) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = if (alert.priority == ActionPriority.ASSISTANCE) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (alert.priority == ActionPriority.ASSISTANCE) {
                        Icons.Filled.QuestionMark
                    } else {
                        Icons.Default.Warning
                    },
                    contentDescription = "Alert",
                    tint = contentColor,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (alert.priority == ActionPriority.ASSISTANCE) {
                    stringResource(R.string.assistance_priority_alert)
                } else {
                    stringResource(R.string.high_priority_alert)
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = contentColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = alert.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = contentColor.copy(alpha = 0.9f)
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(
                    text = stringResource(R.string.received),
                    style = TextStyle(
                        color = containerColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun HighPriorityAlertOverlayAssistancePreview() {
    NabdaTheme {
        HighPriorityAlertOverlayContent(
            alert = ActionPayload(
                correlationId = "1",
                actionId = "1",
                title = "Assistance Requested",
                priority = ActionPriority.ASSISTANCE,
                timestamp = System.currentTimeMillis()
            ),
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun HighPriorityAlertOverlayEmergencyPreview() {
    NabdaTheme {
        HighPriorityAlertOverlayContent(
            alert = ActionPayload(
                correlationId = "2",
                actionId = "2",
                title = "Emergency Detected",
                priority = ActionPriority.EMERGENCY,
                timestamp = System.currentTimeMillis()
            ),
            onDismiss = {}
        )
    }
}
