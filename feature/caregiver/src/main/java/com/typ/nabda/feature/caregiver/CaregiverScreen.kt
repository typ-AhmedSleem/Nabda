package com.typ.nabda.feature.caregiver

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typ.nabda.core.model.Alert
import com.typ.nabda.core.model.CaregiverAction
import com.typ.nabda.core.model.ConnectivitySource
import com.typ.nabda.designsystem.theme.NabdaTheme
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
    val isConnected by viewModel.isInternetConnected.collectAsStateWithLifecycle()
    val notifGranted by viewModel.isNotificationPermissionGranted.collectAsStateWithLifecycle()
    val telemetryState by viewModel.telemetryUiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(CaregiverTab.METRICS) }
    val alerts by viewModel.filteredAlerts.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

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
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(TargetGreen, CircleShape)
                )
                Text(
                    text = stringResource(R.string.connected_to_nabda_device),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TargetGreen,
                        letterSpacing = 1.sp
                    )
                )
            }
            Text(
                text = if (isConnected) {
                    stringResource(R.string.welcome_to_nabda)
                } else {
                    stringResource(R.string.system_offline)
                },
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    color = TargetBlack,
                )
            )
        }

        MetricSection(title = stringResource(R.string.permissions_and_security)) {
            MetricListItem(
                label = stringResource(R.string.notifications),
                text = if (notifGranted) stringResource(R.string.notifications_are_enabled) else stringResource(R.string.notifications_are_disabled),
                fontSize = 28.sp
            )

            MetricListItem(
                label = stringResource(R.string.alert_profile),
                text = if (!isSilent) stringResource(R.string.phone_is_not_silent) else stringResource(R.string.phone_is_silent),
                fontSize = 28.sp
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
                icon = if (telemetryState?.isCharging == true) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
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
                fontSize = 28.sp
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
                color = TargetGray,
                letterSpacing = 1.sp
            )
        )
        HorizontalDivider(color = TargetGray.copy(alpha = 0.2f), thickness = 1.dp)
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
                    color = TargetGray,
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
                    tint = TargetBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = TargetBlack,
                    lineHeight = fontSize * 1.1f
                )
            )
        }
    }
}

@Composable
fun ActionsScreenContent(
    modifier: Modifier = Modifier,
    onActionClick: (CaregiverAction) -> Unit,
) {
    val actions = remember {
        listOf(
            QuickActionItem(
                action = CaregiverAction.FOOD_READY,
                descResId = R.string.action_food_ready_desc,
                icon = Icons.Default.Fastfood,
                iconBg = Color(0xFFFFE0B2),
                iconColor = Color(0xFFE65100)
            ),
            QuickActionItem(
                action = CaregiverAction.COME_CLOSER,
                descResId = R.string.action_come_closer_desc,
                icon = Icons.Default.Person,
                iconBg = Color(0xFFE3F2FD),
                iconColor = Color(0xFF1976D2)
            ),
            QuickActionItem(
                action = CaregiverAction.ARE_YOU_SICK,
                descResId = R.string.action_are_you_sick_desc,
                icon = Icons.Default.MedicalServices,
                iconBg = Color(0xFFFFEBEE),
                iconColor = Color(0xFFD32F2F)
            ),
            QuickActionItem(
                action = CaregiverAction.DO_WANT_THIS,
                descResId = R.string.action_do_you_want_this_desc,
                icon = Icons.AutoMirrored.Filled.Help,
                iconBg = Color(0xFFF3E5F5),
                iconColor = Color(0xFF7B1FA2)
            ),
            QuickActionItem(
                action = CaregiverAction.IM_COMING,
                descResId = R.string.action_im_coming_desc,
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                iconBg = Color(0xFFE8F5E9),
                iconColor = Color(0xFF388E3C)
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stringResource(R.string.quick_actions_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF3C3228)
            )
        )
        Text(
            text = stringResource(R.string.quick_actions_subtitle),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 16.sp,
                color = Color(0xFF7A8499)
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(actions) { item ->
                QuickActionCard(
                    item = item,
                    onClick = { onActionClick(item.action) }
                )
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

data class QuickActionItem(
    val action: CaregiverAction,
    val descResId: Int,
    val icon: ImageVector,
    val iconBg: Color,
    val iconColor: Color,
)

@Composable
fun QuickActionCard(
    item: QuickActionItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(item.iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                val title = when (item.action) {
                    CaregiverAction.FOOD_READY -> stringResource(R.string.action_food_ready_title)
                    CaregiverAction.COME_CLOSER -> stringResource(R.string.action_come_closer_title)
                    CaregiverAction.ARE_YOU_SICK -> stringResource(R.string.action_are_you_sick_title)
                    CaregiverAction.DO_WANT_THIS -> stringResource(R.string.action_do_you_want_this_title)
                    CaregiverAction.IM_COMING -> stringResource(R.string.action_im_coming_title)
                    else -> item.action.name
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3C3228)
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(item.descResId),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 14.sp,
                        color = Color(0xFF7A8499)
                    )
                )
            }
        }
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
                locationLabel = "Brooklyn, NY",
                lastSeenLabel = "Last seen label",
                rawTimestamp = System.currentTimeMillis(),
                signalStrength = 4,
                isSilentMode = false
            ),
            connectedHost = "192.168.1.6",
            alerts = emptyList(),
            selectedFilter = null,
            onFilterSelected = {},
            onActionClick = {},
        )
    }
}

@Preview(locale = "ar")
@Composable
fun MetricsScreenPreview() {
    NabdaTheme {
        Scaffold {
            Box(Modifier.padding(it)) {
                MetricsScreen(
                    isConnected = true,
                    notifGranted = true,
                    isSilent = false,
                    connectedHost = "192.168.1.6",
                    telemetryState = DeviceTelemetryUiState(
                        deviceStatus = DeviceStatus.ONLINE,
                        batteryLevel = BatteryLevel.NORMAL,
                        batteryPercentage = 84,
                        isCharging = true,
                        connectivity = ConnectivitySource.WIFI,
                        locationLabel = "Brooklyn, NY",
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
@Preview
@Composable
fun ActionsScreenPreview() {
    NabdaTheme {
        Scaffold {
            ActionsScreenContent(onActionClick = {})
        }
    }
}
