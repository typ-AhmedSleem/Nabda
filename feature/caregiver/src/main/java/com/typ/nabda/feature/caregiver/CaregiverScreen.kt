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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typ.nabda.core.model.ConnectivitySource
import org.koin.compose.viewmodel.koinViewModel

// New Design Colors
private val DashboardBackground = Color(0xFFFBF7EB)
private val PrimaryGreen = Color(0xFF4CAF50)
private val CardWhite = Color.White
private val TitleContentColor = Color(0xFF333333)
private val MetricLabelColor = Color(0xFF999999)
private val SuccessGreen = Color(0xFF4CAF50)
private val StatusIconBg = Color(0xFFE8F3F1)

@Composable
fun CaregiverScreen(
    viewModel: CaregiverViewModel = koinViewModel(),
) {
    val isConnected by viewModel.isInternetConnected.collectAsStateWithLifecycle()
    val notifGranted by viewModel.isNotificationPermissionGranted.collectAsStateWithLifecycle()
    val cameraGranted by viewModel.isCameraPermissionGranted.collectAsStateWithLifecycle()
    val isSilent by viewModel.isPhoneSilent.collectAsStateWithLifecycle()
    val telemetryState by viewModel.telemetryUiState.collectAsStateWithLifecycle()

    androidx.compose.material3.Scaffold(
        bottomBar = { DashboardBottomBar() },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            CaregiverDashboardContent(
                isConnected = isConnected,
                notifGranted = notifGranted,
                cameraGranted = cameraGranted,
                isSilent = isSilent,
                telemetryState = telemetryState
            )
        }
    }
}

@Composable
fun DashboardBottomBar() {
    androidx.compose.material3.NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf("HOME", "HISTORY", "SETTINGS")
        val icons = listOf(Icons.Default.Home, Icons.Default.History, Icons.Default.Settings)

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == 0,
                onClick = { /* TODO */ },
                icon = { Icon(icons[index], contentDescription = item) },
                label = { Text(item, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold)) },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = MetricLabelColor,
                    unselectedTextColor = MetricLabelColor,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun CaregiverDashboardContent(
    isConnected: Boolean,
    notifGranted: Boolean,
    cameraGranted: Boolean,
    isSilent: Boolean,
    telemetryState: DeviceTelemetryUiState?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp
        )
    ) {
        // App Title
        Text(
            text = "Caregiver Dashboard",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TitleContentColor
            ),
            modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
        )

        // System Active Indicator Card
        SystemActiveCard(isConnected = isConnected)

        Spacer(modifier = Modifier.height(0.dp))

        // Metrics Section Header
        Text(
            text = "DEVICE METRICS",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MetricLabelColor,
                letterSpacing = 1.sp
            ),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 0.dp, start = 8.dp)
        )

        // Metrics Cards
        MetricItemCard(
            icon = Icons.Default.Notifications,
            label = "NOTIFICATIONS",
            value = if (notifGranted) "Enabled" else "Disabled",
            isOk = notifGranted
        )

        MetricItemCard(
            icon = if (isSilent) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
            label = "SILENT MODE",
            value = if (isSilent) "Active" else "Inactive",
            isOk = !isSilent // Caregiver usually wants to be alerted
        )

        // Battery Metric
        val batteryValue =
            telemetryState?.let { "${it.batteryPercentage}% ${if (it.batteryLevel == BatteryLevel.NORMAL) "Optimal" else "Low"}" } ?: "Unknown"
        MetricItemCard(
            icon = Icons.Default.BatteryChargingFull,
            label = "BATTERY",
            value = batteryValue,
            isOk = (telemetryState?.batteryLevel ?: BatteryLevel.NORMAL) != BatteryLevel.CRITICAL,
            trailingIcon = if (telemetryState?.isCharging == true) Icons.Default.BatteryChargingFull else null // Use a lightning bolt if available
        )

        // Network Metric
        val networkValue = telemetryState?.connectivity?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Disconnected"
        MetricItemCard(
            icon = Icons.Default.Wifi,
            label = "NETWORK",
            value = networkValue,
            isOk = isConnected
        )

        // Signal Strength Metric (Placeholder for now)
        MetricItemCard(
            icon = Icons.Default.CellTower,
            label = "SIGNAL STRENGTH",
            value = "Excellent (4G)",
            isOk = true
        )

        // Location Metric
        MetricItemCard(
            icon = Icons.Default.LocationOn,
            label = "LOCATION",
            value = telemetryState?.locationLabel ?: "Unknown",
            isOk = telemetryState != null
        )

        Spacer(modifier = Modifier.height(32.dp)) // Extra space for bottom nav
    }
}

@Composable
fun SystemActiveCard(isConnected: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = 24.dp,
                    vertical = 16.dp
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Signal Icon Circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SettingsInputAntenna,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isConnected) "SYSTEM ACTIVE" else "DISCONNECTED",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
                Text(
                    text = if (isConnected) "Monitoring paired device" else "Check paired device connection",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }

            // Small status dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}

@Composable
fun MetricItemCard(
    icon: ImageVector,
    label: String,
    value: String,
    isOk: Boolean,
    trailingIcon: ImageVector? = null,
) {
    Surface(
        shape = CircleShape,
        color = StatusIconBg,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(CardWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TitleContentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MetricLabelColor,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = value,
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isOk) {
                            PrimaryGreen
                        } else {
                            TitleContentColor
                        }
                    )
                )
            }

            // Trailing Indicator/Icon
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = TitleContentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            /*if (isOk) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Status OK",
                    tint = SuccessGreen,
                    modifier = Modifier.size(24.dp)
                )
            }*/
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CaregiverDashboardPreview() {
    MaterialTheme {

    CaregiverDashboardContent(
            isConnected = true,
            notifGranted = false,
            cameraGranted = false,
            isSilent = false,
            telemetryState = DeviceTelemetryUiState(
                deviceStatus = DeviceStatus.ONLINE,
                batteryLevel = BatteryLevel.NORMAL,
                batteryPercentage = 77,
                isCharging = false,
                connectivity = ConnectivitySource.WIFI,
                locationLabel = "Cairo, Egypt",
                lastSeenLabel = "Last seen label"
            )
        )
    }
}