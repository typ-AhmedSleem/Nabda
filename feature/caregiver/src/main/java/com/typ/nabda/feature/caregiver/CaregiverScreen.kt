package com.typ.nabda.feature.caregiver

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typ.nabda.core.model.ConnectivitySource
import com.typ.nabda.core.model.SupportedAction
import org.koin.compose.viewmodel.koinViewModel

// Target Design Colors
private val DarkBlue = Color(0xFF326680)
private val LabelGray = Color(0xFF999999)
private val GreenText = Color(0xFF4CAF50)
private val PillGreenBg = Color(0xFFE8F5E9)
private val PillGreenText = Color(0xFF4CAF50)
private val PillBlueBg = Color(0xFFE3F2FD)
private val PillBlueText = Color(0xFF2196F3)
private val PillGrayBg = Color(0xFFF5F5F5)
private val PillGrayText = Color(0xFF9E9E9E)
private val BackgroundColor = Color(0xFFF8F9FB)

@Composable
fun CaregiverScreen(
    viewModel: CaregiverViewModel = koinViewModel(),
) {
    val isSilent by viewModel.isPhoneSilent.collectAsStateWithLifecycle()
    val isConnected by viewModel.isInternetConnected.collectAsStateWithLifecycle()
    val notifGranted by viewModel.isNotificationPermissionGranted.collectAsStateWithLifecycle()
    val telemetryState by viewModel.telemetryUiState.collectAsStateWithLifecycle()

    CaregiverDashboardContent(
        isConnected = isConnected,
        notifGranted = notifGranted,
        isSilent = isSilent,
        telemetryState = telemetryState,
        onActionClick = viewModel::sendAction
    )
}

@Composable
fun DashboardBottomBar() {
    NavigationBar(
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DarkBlue,
                    selectedTextColor = DarkBlue,
                    unselectedIconColor = LabelGray,
                    unselectedTextColor = LabelGray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun CaregiverDashboardContent(
    modifier: Modifier = Modifier,
    isConnected: Boolean,
    notifGranted: Boolean,
    isSilent: Boolean,
    telemetryState: DeviceTelemetryUiState?,
    onActionClick: (SupportedAction) -> Unit,
) {
    Scaffold(
        bottomBar = { DashboardBottomBar() },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App Title
            Text(
                text = "Nabda",
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF333333)
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // System Active Card
            SystemActiveCard(isConnected, telemetryState?.deviceStatus ?: DeviceStatus.OFFLINE)

            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PAIRED DEVICE METRICS",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LabelGray
                    )
                )
                StatusBadge(text = "UPDATED JUST NOW")
            }

            // Row 1: Battery & Wi-Fi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricGridCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.BatteryChargingFull,
                    topLabel = if (telemetryState?.isCharging == true) "CHARGING" else "DISCHARGING",
                    topLabelColor = PillGreenText,
                    topLabelBg = PillGreenBg,
                    value = "${telemetryState?.batteryPercentage ?: 82}%",
                    bottomLabel = "BATTERY LIFE",
                    valueSize = 34.sp
                )
                MetricGridCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Wifi,
                    topLabel = "STABLE",
                    topLabelColor = PillBlueText,
                    topLabelBg = PillBlueBg,
                    value = "Wi-Fi",
                    bottomLabel = "NETWORK SOURCE",
                    valueSize = 34.sp
                )
            }

            // Row 2: Signal & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricGridCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.SignalCellularAlt,
                    topLabel = "LTE",
                    topLabelColor = PillGrayText,
                    topLabelBg = PillGrayBg,
                    value = "Excellent",
                    bottomLabel = "SIGNAL BARS",
                    valueSize = 26.sp
                )
                MetricGridCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocationOn,
                    topIcon = Icons.AutoMirrored.Filled.OpenInNew,
                    value = telemetryState?.locationLabel?.split(",")?.firstOrNull()?.uppercase() ?: "MAIN ST, NY",
                    bottomLabel = "DEVICE LOCATION",
                    valueSize = 20.sp
                )
            }

            // Row 3: Notifications & Silent Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricGridCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Notifications,
                    topIcon = Icons.Default.CheckCircle,
                    topIconColor = GreenText,
                    value = if (notifGranted) "ALLOWED" else "DENIED",
                    bottomLabel = "NOTIFICATIONS",
                    valueSize = 24.sp
                )
                MetricGridCard(
                    modifier = Modifier.weight(1f),
                    icon = if (isSilent) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    topIcon = Icons.Default.CheckCircle,
                    topIconColor = GreenText,
                    value = if (isSilent) "ACTIVE" else "INACTIVE",
                    bottomLabel = "SILENT MODE",
                    valueSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SystemActiveCard(isConnected: Boolean, localStatus: DeviceStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                )
                Icon(
                    imageVector = Icons.Default.SettingsInputAntenna,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.White, CircleShape)
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (localStatus == DeviceStatus.ONLINE) Color.White else DarkBlue, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (localStatus == DeviceStatus.ONLINE || isConnected) "SYSTEM ACTIVE" else "SYSTEM OFFLINE",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(Color.White.copy(alpha = 0.5f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE MONITORING",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun MetricGridCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    topLabel: String = "",
    topLabelColor: Color = GreenText,
    topLabelBg: Color = Color.Transparent,
    topIcon: ImageVector? = null,
    topIconColor: Color = LabelGray,
    value: String,
    bottomLabel: String,
    valueSize: androidx.compose.ui.unit.TextUnit = 24.sp,
) {
    Card(
        modifier = modifier.height(135.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F1F1))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DarkBlue,
                    modifier = Modifier.size(24.dp)
                )

                if (topLabel.isNotEmpty()) {
                    Surface(
                        color = topLabelBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = topLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = topLabelColor
                            )
                        )
                    }
                } else if (topIcon != null) {
                    Icon(
                        imageVector = topIcon,
                        contentDescription = null,
                        tint = topIconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = value,
                    style = TextStyle(
                        fontSize = valueSize,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF333333)
                    ),
                    maxLines = 1
                )
                Text(
                    text = bottomLabel,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LabelGray
                    )
                )
            }
        }
    }
}

@Composable
fun StatusBadge(text: String) {
    Row(
        modifier = Modifier
            .background(Color(0xFFE3F2FD), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(DarkBlue, CircleShape)
        )
        Text(
            text = text,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
        )
    }
}

@PreviewLightDark
@Composable
fun CaregiverDashboardPreview() {
    MaterialTheme {
        CaregiverDashboardContent(
            isConnected = true,
            notifGranted = true,
            isSilent = true,
            telemetryState = DeviceTelemetryUiState(
                deviceStatus = DeviceStatus.ONLINE,
                batteryLevel = BatteryLevel.NORMAL,
                batteryPercentage = 82,
                isCharging = true,
                connectivity = ConnectivitySource.WIFI,
                locationLabel = "Main St, NY",
                lastSeenLabel = "Last seen label"
            ),
            onActionClick = {}
        )
    }
}
