package com.typ.nabda.feature.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConnectWithoutContact
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.typ.nabda.core.haptic.HapticEngine
import com.typ.nabda.core.model.HapticEnginePattern
import com.typ.nabda.designsystem.theme.NabdaTheme
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import com.typ.nabda.infrastructure.localnetwork.R as R2

@Composable
fun DisconnectedFromServerOverlay(
    hapticEngine: HapticEngine = koinInject(),
) {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(5000)
        repeat(10) {
            hapticEngine.performHaptic(HapticEnginePattern.NotConnected)
            delay(1000)
        }
    }

    if (isVisible) {
        val containerColor = MaterialTheme.colorScheme.secondaryContainer
        val contentColor = MaterialTheme.colorScheme.onSecondaryContainer

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput("") {},
            contentColor = contentColor,
            color = containerColor,
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
                    CircularProgressIndicator(
                        modifier = Modifier.size(120.dp),
                        trackColor = contentColor.copy(alpha = 0.2f),
                        color = contentColor,
                        gapSize = 8.dp
                    )
                    Icon(
                        imageVector = Icons.Filled.ConnectWithoutContact,
                        modifier = Modifier.size(64.dp),
                        contentDescription = "Disconnected",
                        tint = contentColor,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R2.string.status_connecting),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = contentColor.copy(alpha = 0.9f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    textAlign = TextAlign.Center,
                    text = stringResource(R2.string.desc_status_disconnected),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                )
            }
        }
    }
}

@Preview(locale = "ar")
@Composable
private fun HighPriorityAlertOverlayAssistancePreview() {
    NabdaTheme {
        DisconnectedFromServerOverlay()
    }
}
