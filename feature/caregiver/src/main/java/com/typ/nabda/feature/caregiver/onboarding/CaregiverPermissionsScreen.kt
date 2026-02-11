package com.typ.nabda.feature.caregiver.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CaregiverPermissionsScreen(
    onContinue: () -> Unit,
) {
    var notificationsEnabled by remember { mutableStateOf(false) }
    var cameraEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Top Title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Permissions",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Headline
        Text(
            text = "Enable permissions",
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001F35)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtitle
        Text(
            text = "To ensure you receive timely alerts and can pair with other devices, please enable the following permissions.",
            style = TextStyle(
                fontSize = 16.sp,
                color = Color(0xFF74777F),
                lineHeight = 24.sp
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Permission Cards
        PermissionCard(
            title = "Notifications",
            description = "Receive alerts when your loved one needs assistance.",
            isEnabled = notificationsEnabled,
            onToggle = { notificationsEnabled = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionCard(
            title = "Camera",
            description = "Used to scan QR codes for pairing with other devices.",
            isEnabled = cameraEnabled,
            onToggle = { cameraEnabled = it }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Continue Button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0B1D2D) // Dark navy/black
            ),
            shape = RoundedCornerShape(32.dp) // More circular shape
        ) {
            Text(
                text = "Continue",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Surface(
        color = Color(0xFFF8F9FF), // Tonal elevation
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 0.dp // No shadow elevation as requested
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF74777F),
                        lineHeight = 20.sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF0B1D2D),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE1E2EC)
                )
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CaregiverPermissionsScreenPreview() {
    MaterialTheme {
        CaregiverPermissionsScreen(onContinue = {})
    }
}
