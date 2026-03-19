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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typ.nabda.caregiver.R
import com.typ.nabda.core.haptic.HapticEngine
import com.typ.nabda.core.model.HapticEnginePattern
import com.typ.nabda.infrastructure.localnetwork.model.ActionPayload
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun HighPriorityAlertOverlay(
    alert: ActionPayload,
    onReceived: () -> Unit,
    hapticEngine: HapticEngine = org.koin.compose.koinInject(),
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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Red
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
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.high_priority_alert),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        isVisible = false
                        onReceived()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = stringResource(R.string.received),
                        style = TextStyle(
                            color = Color.Red,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
