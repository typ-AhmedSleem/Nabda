package com.typ.nabda.feature.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

// Hardcoded Colors for Consistency
private val BackgroundColor = Color(0xFFFDF8E8)
private val PrimaryTextColor = Color(0xFF3C3228)
private val SecondaryTextColor = Color(0xFF7A8499)
private val CardBackgroundColor = Color.White
private val StatusCircleColor = Color(0xFFF1F4F1)
private val ActiveGreen = Color(0xFF6F8B70)
private val DisconnectedRed = Color(0xFFD9534F)

@Composable
fun CaregiverScreen(
    viewModel: CaregiverViewModel = koinViewModel(),
) {
    val isConnected by viewModel.isInternetConnected.collectAsStateWithLifecycle()
    val notifGranted by viewModel.isNotificationPermissionGranted.collectAsStateWithLifecycle()
    val cameraGranted by viewModel.isCameraPermissionGranted.collectAsStateWithLifecycle()
    val isSilent by viewModel.isPhoneSilent.collectAsStateWithLifecycle()

    CaregiverDashboardContent(
        isConnected = isConnected,
        notifGranted = notifGranted,
        cameraGranted = cameraGranted,
        isSilent = isSilent
    )
}

@Composable
fun CaregiverDashboardContent(
    isConnected: Boolean,
    notifGranted: Boolean,
    cameraGranted: Boolean,
    isSilent: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title
        Text(
            text = "Nabda",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryTextColor
            ),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // System Active Indicator Card
        SystemActiveCard(isConnected = isConnected)

        Spacer(modifier = Modifier.height(32.dp))

        // Device Status Section Header
        Text(
            text = "DEVICE STATUS",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryTextColor.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            ),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 16.dp, start = 8.dp)
        )

        // Status Cards
        StatusItemCard(
            icon = Icons.Default.Notifications,
            label = "Notifications Permission",
            isOk = notifGranted
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatusItemCard(
            icon = Icons.Default.Wifi,
            label = "Internet Connection",
            isOk = isConnected
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatusItemCard(
            icon = Icons.Default.CameraAlt,
            label = "Camera Permission",
            isOk = cameraGranted
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Phone Silent Card (User requested)
        StatusItemCard(
            icon = if (isSilent) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
            label = "Phone Silent",
            isOk = !isSilent // OK if NOT silent for a caregiver
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CaregiverDashboardPreview() {
    MaterialTheme {
        CaregiverDashboardContent(
            isConnected = true,
            notifGranted = false,
            cameraGranted = false,
            isSilent = false
        )
    }
}

@Composable
fun SystemActiveCard(isConnected: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(48.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 48.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Signal Icon Circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(StatusCircleColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SettingsInputAntenna,
                    contentDescription = null,
                    tint = if (isConnected) ActiveGreen else DisconnectedRed,
                    modifier = Modifier.size(56.dp)
                )
                // Small dot on the circle border as seen in design
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .background(if (isConnected) ActiveGreen else DisconnectedRed, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (isConnected) "SYSTEM ACTIVE" else "DISCONNECTED",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryTextColor
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (isConnected) ActiveGreen else DisconnectedRed, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isConnected) "MONITORING" else "CHECK NETWORK",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) ActiveGreen else DisconnectedRed,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

@Composable
fun StatusItemCard(
    icon: ImageVector,
    label: String,
    isOk: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(StatusCircleColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SecondaryTextColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTextColor
                ),
                modifier = Modifier.weight(1f)
            )

            if (isOk) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Status OK",
                    tint = ActiveGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
