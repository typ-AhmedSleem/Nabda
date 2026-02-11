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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CaregiverHowToScreen(
    onScanQr: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "How it works",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Steps
        HowToStep(
            number = 1,
            title = "Scan QR",
            description = "Scan the QR code on the device to pair it with your app.",
            icon = Icons.Default.QrCodeScanner
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Swapped order as requested: 3. Stay Connected comes before 2. Receive Alerts
        HowToStep(
            number = 2,
            title = "Stay Connected",
            description = "Stay connected and monitor the well-being of your loved one.",
            icon = Icons.Default.Wifi
        )

        Spacer(modifier = Modifier.height(24.dp))

        HowToStep(
            number = 3,
            title = "Receive Alerts",
            description = "Receive real-time alerts and notifications from the paired device.",
            icon = Icons.Default.Notifications
        )

        Spacer(modifier = Modifier.weight(1f))

        // Get Started Button
        Button(
            onClick = onScanQr,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0C1D13) // Dark green
            ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text(
                text = "Scan QR now",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun HowToStep(
    number: Int,
    title: String,
    description: String,
    icon: ImageVector,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFFF1F4F1), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF0C1D13),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "$number. $title",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF6F8B70), // Muted green for description as seen in design
                    lineHeight = 22.sp
                )
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CaregiverHowToScreenPreview() {
    MaterialTheme {
        CaregiverHowToScreen(onScanQr = {})
    }
}
