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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typ.nabda.core.model.ConnectivitySource
import com.typ.nabda.core.model.SupportedAction
import com.typ.nabda.designsystem.theme.NabdaTheme
import org.koin.compose.viewmodel.koinViewModel

// Target Design Colors
private val DarkBlue = Color(0xFF326680)
private val LabelGray = Color(0xff858383)
private val GreenText = Color(0xFF4CAF50)
private val PillGreenBg = Color(0xFFE8F5E9)
private val PillGreenText = Color(0xFF4CAF50)
private val PillBlueBg = Color(0xFFE3F2FD)
private val PillBlueText = Color(0xFF2196F3)
private val PillGrayBg = Color(0xFFF5F5F5)
private val PillGrayText = Color(0xFF9E9E9E)
private val BackgroundColor = Color(0xFFF8F9FB)
private val IconCircleColor = Color(0xFFF5E1D1)
private val IconColor = Color(0xFFDA7A5E)

@Composable
fun CaregiverScreen(
    viewModel: CaregiverViewModel = koinViewModel(),
    onNavigateToDiscovery: () -> Unit = {},
) {
    val isSilent by viewModel.isPhoneSilent.collectAsStateWithLifecycle()
    val isConnected by viewModel.isInternetConnected.collectAsStateWithLifecycle()
    val notifGranted by viewModel.isNotificationPermissionGranted.collectAsStateWithLifecycle()
    val telemetryState by viewModel.telemetryUiState.collectAsStateWithLifecycle()
    // Navigation logic
    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                CaregiverViewModel.CaregiverNavigationEvent.NavigateToDiscovery -> {
                    onNavigateToDiscovery()
                }
            }
        }
    }

    CaregiverDashboardContent(
        isConnected = isConnected,
        notifGranted = notifGranted,
        isSilent = isSilent,
        telemetryState = telemetryState,
        onActionClick = viewModel::sendAction
    )
}

// ... helper to format relative time
@Composable
fun formatRelativeTime(timestamp: Long): String {
    // todo: use prettytime lib
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60000 -> "JUST NOW"
        diff < 3600000 -> "UPDATED ${diff / 60000} MINS AGO"
        else -> "UPDATED LONGER AGO"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        containerColor = BackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Nabda Caregiver",
                        maxLines = 1
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
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
                StatusBadge(text = if (telemetryState != null) formatRelativeTime(telemetryState.rawTimestamp) else "OFFLINE")
            }

            val isCharging = telemetryState?.isCharging == true
            MetricGridCard(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.BatteryStd,
                topLabel = if (isCharging) "CHARGING" else "DISCHARGING",
                topLabelColor = if (isCharging) PillGreenText else Color.Red,
                topLabelBg = if (isCharging) PillGreenBg else Color(0xFFFFEBEE),
                value = "${telemetryState?.batteryPercentage ?: 0}%",
                bottomLabel = "BATTERY LIFE",
                valueSize = 34.sp
            )

            // Row 2: Signal & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val signalBars = (telemetryState?.signalStrength ?: 0)
                val signalLabel = when (signalBars) {
                    4 -> "EXCELLENT"
                    3 -> "GOOD"
                    2 -> "FAIR"
                    1 -> "POOR"
                    else -> "NONE"
                }

                MetricGridCard(
                    modifier = Modifier.weight(1f),
                    icon = if (telemetryState?.connectivity == ConnectivitySource.WIFI) Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
                    topLabel = "STABLE",
                    topLabelColor = PillBlueText,
                    topLabelBg = PillBlueBg,
                    value = telemetryState?.connectivity?.name ?: "NONE",
                    bottomLabel = "NETWORK SOURCE",
                    valueSize = 34.sp
                )

                MetricGridCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.SignalCellularAlt,
                    topLabel = "$signalBars/4 BARS",
                    topLabelColor = IconColor,
                    topLabelBg = IconCircleColor,
                    value = signalLabel,
                    bottomLabel = "Signal Strength",
                    valueSize = 26.sp
                )
            }

            MetricGridCard(
                icon = Icons.Default.LocationOn,
                modifier = Modifier.fillMaxWidth(),
//                    topIcon = Icons.AutoMirrored.Filled.OpenInNew,
                value = telemetryState?.locationLabel ?: "UNKNOWN",
                topLabelColor = DarkBlue,
                topLabel = if (telemetryState != null) formatRelativeTime(telemetryState.rawTimestamp) else "OFFLINE",
                topLabelBg = DarkBlue.copy(0.1f),
                bottomLabel = "LOCATION",
                valueSize = 24.sp
            )

            // Row 3: Notifications & Silent Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricGridCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Notifications,
                    topIcon = Icons.Default.CheckCircle,
                    topLabelColor = if (notifGranted) GreenText else {
                        MaterialTheme.colorScheme.error
                    },
                    value = if (notifGranted) "ALLOWED" else "DENIED",
                    bottomLabel = "NOTIFICATIONS",
                    valueSize = 24.sp
                )
                val isDeafSilent = telemetryState?.isSilentMode == true
                MetricGridCard(
                    modifier = Modifier.weight(1f),
                    icon = if (isDeafSilent) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    topIcon = Icons.Default.CheckCircle.takeUnless { isDeafSilent },
                    topLabelColor = if (isDeafSilent) {
                        MaterialTheme.colorScheme.error
                    } else {
                        GreenText
                    },
                    value = if (isDeafSilent) "SILENT" else "NORMAL",
                    bottomLabel = "DEAF DEVICE MODE",
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
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = DarkBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = 8.dp,
                    vertical = 8.dp
                )
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
                        .background(Color.White, CircleShape)
                )
                Icon(
                    imageVector = Icons.Default.SettingsInputAntenna,
                    contentDescription = null,
                    tint = DarkBlue,
                    modifier = Modifier.size(30.dp)
                )
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
                        text = "Connected to NABDA Device",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    )
                }
            }
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
    valueSize: TextUnit = 24.sp,
    containerColor: Color = Color.White,
    contentColor: Color = Color.Black,
) {
    Card(
        modifier = modifier.heightIn(min = 120.dp),
        shape = RoundedCornerShape(25),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            LabelGray.copy(0.25f)
        ),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .padding(
                    top = 16.dp,
                    bottom = 12.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                /*Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DarkBlue,
                    modifier = Modifier.size(24.dp)
                )*/

                Text(
                    text = bottomLabel,
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = LabelGray,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            Column {
                Text(
                    text = value,
                    style = TextStyle(
                        fontSize = valueSize,
                        fontWeight = FontWeight.Black,
                        color = topLabelColor
                    ),
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = valueSize,
                    ),
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (topLabel.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = topLabelBg,
                        shape = CircleShape
                    ) {
                        Text(
                            text = topLabel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = topLabelColor
                            )
                        )
                    }
                }
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

@Preview
@Composable
fun CaregiverDashboardPreview() {
    NabdaTheme {
        CaregiverDashboardContent(
            isConnected = true,
            notifGranted = false,
            isSilent = true,
            telemetryState = DeviceTelemetryUiState(
                deviceStatus = DeviceStatus.OFFLINE,
                batteryLevel = BatteryLevel.CRITICAL,
                batteryPercentage = 87,
                isCharging = true,
                connectivity = ConnectivitySource.WIFI,
                locationLabel = "Zagazig, Egypt",
                lastSeenLabel = "Last seen label",
                rawTimestamp = System.currentTimeMillis(),
                signalStrength = 3,
                isSilentMode = true
            ),
            onActionClick = {}
        )
    }
}
