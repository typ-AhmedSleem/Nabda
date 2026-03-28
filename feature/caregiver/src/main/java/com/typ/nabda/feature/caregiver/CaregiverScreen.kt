package com.typ.nabda.feature.caregiver

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.typ.nabda.core.model.Alert
import com.typ.nabda.core.model.CaregiverAction
import com.typ.nabda.core.model.ConnectivitySource
import com.typ.nabda.designsystem.theme.NabdaTheme
import com.typ.nabda.feature.caregiver.actions.CaregiverQuickActions
import com.typ.nabda.feature.caregiver.models.QuickActionItem
import com.typ.nabda.infrastructure.localnetwork.LocalNetworkConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.compose.viewmodel.koinViewModel

enum class CaregiverTab {
    METRICS, ACTIONS, HISTORY
}

private val DarkBlue = Color(0xFF326680)
private val LabelGray = Color(0xff858383)
private val BackgroundColor = Color(0xFFFDFBF0)
private val TargetBlack = Color(0xFF1C1C1C)
private val TargetGreen = Color(0xFF66BB6A)
private val TargetGray = Color(0xFFAAAAAA)
private val PillBlueBg = Color(0xFFE3F2FD)

@Composable
fun CaregiverScreen(
    viewModel: CaregiverViewModel = koinViewModel(),
    onNavigateToDiscovery: () -> Unit = {},
) {
    val isSilent by viewModel.isPhoneSilent.collectAsStateWithLifecycle()
    val connectedHost by viewModel.connectedHost.collectAsStateWithLifecycle()
    val notifGranted by viewModel.isNotificationPermissionGranted.collectAsStateWithLifecycle()
    val telemetryState by viewModel.telemetryUiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(CaregiverTab.METRICS) }
    val alerts by viewModel.filteredAlerts.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val isConnected by remember(telemetryState) {
        derivedStateOf {
            telemetryState != null && telemetryState?.deviceStatus == DeviceStatus.ONLINE
        }
    }

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            delay(LocalNetworkConstants.CONNECT_TIMEOUT_MS)
            if (isActive and !isConnected) {
                viewModel.emitEvent(CaregiverViewModel.CaregiverNavigationEvent.NavigateToDiscovery)
            }
        }
    }

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
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        connectedHost = connectedHost,
        isConnected = isConnected,
        notifGranted = notifGranted,
        isSilent = isSilent,
        telemetryState = telemetryState,
        alerts = alerts,
        selectedFilter = selectedFilter,
        onFilterSelected = viewModel::onFilterSelected,
        onActionClick = viewModel::sendAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverDashboardContent(
    modifier: Modifier = Modifier,
    selectedTab: CaregiverTab,
    onTabSelected: (CaregiverTab) -> Unit,
    isConnected: Boolean,
    notifGranted: Boolean,
    isSilent: Boolean,
    connectedHost: String?,
    telemetryState: DeviceTelemetryUiState?,
    alerts: List<Alert>,
    selectedFilter: CaregiverAction?,
    onFilterSelected: (CaregiverAction?) -> Unit,
    onActionClick: (CaregiverAction) -> Unit,
) {
    Scaffold(
        modifier = modifier,
//        containerColor = BackgroundColor,
        bottomBar = {
            CaregiverBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                targetState = selectedTab,
                label = "TabAnimation"
            ) { caregiverTab ->
                when (caregiverTab) {
                    CaregiverTab.METRICS -> {
                        MetricsScreen(
                            isConnected = isConnected,
                            notifGranted = notifGranted,
                            isSilent = isSilent,
                            telemetryState = telemetryState,
                            connectedHost = connectedHost
                        )
                    }

                    CaregiverTab.ACTIONS -> {
                        ActionsScreenContent(
                            onActionClick = onActionClick
                        )
                    }

                    CaregiverTab.HISTORY -> {
                        CaregiverHistoryContent(
                            alerts = alerts,
                            selectedFilter = selectedFilter,
                            onFilterSelected = onFilterSelected
                        )
                    }
                }
            }

            // Disconnected overlay
            val isDisconnected by remember(telemetryState) {
                derivedStateOf {
                    telemetryState == null
                            || telemetryState.deviceStatus != DeviceStatus.ONLINE
                }
            }

            if (isDisconnected) {
                DisconnectedFromServerOverlay()
            }
        }
    }
}

@Composable
fun CaregiverBottomNavigation(
    modifier: Modifier = Modifier,
    selectedTab: CaregiverTab,
    onTabSelected: (CaregiverTab) -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        contentColor = DarkBlue,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == CaregiverTab.METRICS,
            onClick = { onTabSelected(CaregiverTab.METRICS) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_metrics)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DarkBlue,
                selectedTextColor = DarkBlue,
                unselectedIconColor = LabelGray,
                unselectedTextColor = LabelGray,
                indicatorColor = PillBlueBg
            )
        )
        NavigationBarItem(
            selected = selectedTab == CaregiverTab.ACTIONS,
            onClick = { onTabSelected(CaregiverTab.ACTIONS) },
            icon = { Icon(Icons.Default.FlashOn, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_actions)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DarkBlue,
                selectedTextColor = DarkBlue,
                unselectedIconColor = LabelGray,
                unselectedTextColor = LabelGray,
                indicatorColor = PillBlueBg
            )
        )
        NavigationBarItem(
            selected = selectedTab == CaregiverTab.HISTORY,
            onClick = { onTabSelected(CaregiverTab.HISTORY) },
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_history)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DarkBlue,
                selectedTextColor = DarkBlue,
                unselectedIconColor = LabelGray,
                unselectedTextColor = LabelGray,
                indicatorColor = PillBlueBg
            )
        )
    }
}

@Composable
fun MetricsScreen(
    modifier: Modifier = Modifier,
    isConnected: Boolean,
    notifGranted: Boolean,
    isSilent: Boolean,
    connectedHost: String?,
    telemetryState: DeviceTelemetryUiState?,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 42.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        Column {
            if (connectedHost != null) {
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer, CircleShape)
                    )

                    Text(
                        text = connectedHost.removePrefix("http://"),
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            Text(
                text = if (isConnected) {
                    stringResource(R.string.welcome_to_nabda)
                } else {
                    stringResource(R.string.system_offline)
                },
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Justify,
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium,
                )
            )
        }

        MetricSection(title = stringResource(R.string.device_metrics)) {
            MetricListItem(
                label = stringResource(R.string.power_level),
                text = stringResource(R.string.battery_percentage_format, telemetryState?.batteryPercentage ?: 0),
                icon = Icons.Default.BatteryStd,
                fontSize = 20.sp
            )
            MetricListItem(
                label = stringResource(R.string.power_source),
                text = if (telemetryState?.isCharging == true) stringResource(R.string.device_is_charging) else stringResource(R.string.device_is_discharging),
                icon = if (telemetryState?.isCharging == true) Icons.Default.BatteryChargingFull else Icons.Default.BatteryAlert,
                fontSize = 20.sp
            )
            MetricListItem(
                label = stringResource(R.string.infrastructure),
                text = stringResource(R.string.connected_to_wifi),
                icon = Icons.Default.Wifi,
                fontSize = 20.sp
            )
            val signalBars = telemetryState?.signalStrength ?: 0
            val signalLabel = when (signalBars) {
                4 -> stringResource(R.string.excellent)
                3 -> stringResource(R.string.good)
                2 -> stringResource(R.string.fair)
                1 -> stringResource(R.string.poor)
                else -> stringResource(R.string.none)
            }
            MetricListItem(
                label = stringResource(R.string.data_strength),
                text = stringResource(R.string.signal_strength_format, signalLabel),
                icon = Icons.Default.SignalCellularAlt,
                fontSize = 20.sp
            )
        }

        MetricSection(title = stringResource(R.string.geofence_position)) {
            MetricListItem(
                text = telemetryState?.locationLabel ?: stringResource(R.string.location_unavailable),
                icon = Icons.Default.LocationOn,
                fontSize = 22.sp
            )
            if (telemetryState?.latitude != null && telemetryState.longitude != null) {
                Spacer(Modifier.height(8.dp))
                TelemetryMap(
                    latitude = telemetryState.latitude,
                    longitude = telemetryState.longitude
                )
            }
        }

        MetricSection(title = stringResource(R.string.permissions_and_security)) {
            MetricListItem(
                label = stringResource(R.string.notifications),
                text = if (notifGranted) stringResource(R.string.notifications_are_enabled) else stringResource(R.string.notifications_are_disabled),
                fontSize = 20.sp
            )

            MetricListItem(
                label = stringResource(R.string.alert_profile),
                text = if (!isSilent) stringResource(R.string.phone_is_not_silent) else stringResource(R.string.phone_is_silent),
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun MetricSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit) = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title.uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.65f),
                letterSpacing = 1.sp
            )
        )
        HorizontalDivider(thickness = 1.dp)
        content()
    }
}

@Composable
fun MetricListItem(
    text: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    icon: ImageVector? = null,
    fontSize: TextUnit = 28.sp,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (label != null) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = label.uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    letterSpacing = 1.sp
                )
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = text,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = fontSize
                )
            )
        }
    }
}

@Composable
fun TelemetryMap(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val location = remember(latitude, longitude) { LatLng(latitude, longitude) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 15f)
    }
    val markerState = rememberMarkerState(
        position = location
    )

    LaunchedEffect(latitude, longitude) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    runCatching {
                        val uri = "geo:$latitude,$longitude?q=$latitude,$longitude".toUri()
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    }.onFailure {
                        Log.w("MetricsScreen", "Failed to open map", it)
                    }
                },
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                zoomGesturesEnabled = false,
                scrollGesturesEnabled = false,
                rotationGesturesEnabled = false,
                tiltGesturesEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            Marker(
                state = markerState
            )
        }
    }
}

@Composable
fun ActionsScreenContent(
    modifier: Modifier = Modifier,
    onActionClick: (CaregiverAction) -> Unit,
) {
    val actions = remember { CaregiverQuickActions.allActions }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.quick_actions_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.quick_actions_subtitle),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.inverseSurface
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        FlowRow(
            modifier = Modifier.fillMaxSize(),
            itemVerticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                alignment = Alignment.CenterHorizontally,
                space = 16.dp,
            ),
            maxItemsInEachRow = 2,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            actions.fastForEach { item ->
                QuickActionCard(
                    item = item,
                    modifier = Modifier.weight(1f),
                    onClick = { onActionClick(item.action) }
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    item: QuickActionItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .heightIn(min = 164.dp)
            .clip(RoundedCornerShape(25))
            .background(item.iconBg)
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(40))
                .background(item.iconColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.iconBg,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            textAlign = TextAlign.Center,
            text = stringResource(item.titleResId),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = item.iconColor
            )
        )
    }
}

@Preview(locale = "ar")
@Composable
fun CaregiverDashboardPreview() {
    var selectedTab by remember { mutableStateOf(CaregiverTab.METRICS) }

    NabdaTheme {
        CaregiverDashboardContent(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            isConnected = true,
            notifGranted = true,
            isSilent = false,
            telemetryState = DeviceTelemetryUiState(
                deviceStatus = DeviceStatus.ONLINE,
                batteryLevel = BatteryLevel.NORMAL,
                batteryPercentage = 84,
                isCharging = true,
                connectivity = ConnectivitySource.WIFI,
                locationLabel = "الزقازيق، مصر",
                lastSeenLabel = "Last seen label",
                rawTimestamp = System.currentTimeMillis(),
                signalStrength = 4,
                isSilentMode = false
            ),
            connectedHost = "192.168.1.254:2001",
            alerts = emptyList(),
            selectedFilter = null,
            onFilterSelected = {},
            onActionClick = {},
        )
    }
}

@Composable
@Preview(locale = "ar")
fun MetricsScreenPreview() {
    NabdaTheme {
        Scaffold {
            Box(Modifier.padding(it)) {
                MetricsScreen(
                    isConnected = true,
                    notifGranted = true,
                    isSilent = false,
                    connectedHost = null,
                    telemetryState = DeviceTelemetryUiState(
                        deviceStatus = DeviceStatus.ONLINE,
                        batteryLevel = BatteryLevel.NORMAL,
                        batteryPercentage = 84,
                        isCharging = true,
                        connectivity = ConnectivitySource.WIFI,
                        locationLabel = "الزقازيق، مصر",
                        lastSeenLabel = "Last seen label",
                        rawTimestamp = System.currentTimeMillis(),
                        signalStrength = 4,
                        isSilentMode = false
                    ),
                )
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(locale = "ar")
@Composable
fun ActionsScreenPreview() {
    NabdaTheme {
        Scaffold {
            ActionsScreenContent(onActionClick = {})
        }
    }
}
