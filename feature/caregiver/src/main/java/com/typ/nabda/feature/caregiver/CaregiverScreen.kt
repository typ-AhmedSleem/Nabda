package com.typ.nabda.feature.caregiver

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
import org.ocpsoft.prettytime.PrettyTime
import java.util.Date

enum class CaregiverTab {
    METRICS, ACTIONS, HISTORY
}

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
    val connectedHost by viewModel.connectedHost.collectAsStateWithLifecycle()
    val isConnected by viewModel.isInternetConnected.collectAsStateWithLifecycle()
    val notifGranted by viewModel.isNotificationPermissionGranted.collectAsStateWithLifecycle()
    val telemetryState by viewModel.telemetryUiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(CaregiverTab.METRICS) }
    val alerts by viewModel.filteredAlerts.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

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

// ... helper to format relative time
@Composable
fun formatRelativeTime(timestamp: Long): String {
    val prettyTime = remember { PrettyTime() }
    return prettyTime.format(Date(timestamp))
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
        containerColor = BackgroundColor,
        topBar = {
            if (selectedTab != CaregiverTab.ACTIONS) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(
                                when (selectedTab) {
                                    CaregiverTab.METRICS -> R.string.nabda_caregiver
                                    CaregiverTab.ACTIONS -> R.string.caregiver_actions
                                    CaregiverTab.HISTORY -> R.string.history
                                }
                            ),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                )
            }
        },
        bottomBar = {
            CaregiverBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            targetState = selectedTab,
        ) { caregiverTab ->
            when (caregiverTab) {
                CaregiverTab.METRICS -> {
                    MetricsScreen(
                        isConnected = isConnected,
                        notifGranted = notifGranted,
                        isSilent = isSilent,
                        connectedHost = connectedHost,
                        telemetryState = telemetryState
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
    selectedTab: CaregiverTab,
    onTabSelected: (CaregiverTab) -> Unit,
) {
    NavigationBar(
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
    isConnected: Boolean,
    notifGranted: Boolean,
    isSilent: Boolean,
    connectedHost: String?,
    telemetryState: DeviceTelemetryUiState?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // System Active Card
        SystemActiveCard(
            isConnected = isConnected,
            connectedHost = connectedHost,
            localStatus = telemetryState?.deviceStatus ?: DeviceStatus.OFFLINE
        )

        // Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.paired_device_metrics),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = LabelGray
                )
            )
            StatusBadge(text = if (telemetryState != null) formatRelativeTime(telemetryState.rawTimestamp) else stringResource(R.string.offline))
        }

        val isCharging = telemetryState?.isCharging == true
        MetricGridCard(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.BatteryStd,
            topLabel = if (isCharging) stringResource(R.string.charging) else stringResource(R.string.discharging),
            topLabelColor = if (isCharging) PillGreenText else Color.Red,
            topLabelBg = if (isCharging) PillGreenBg else Color(0xFFFFEBEE),
            value = "${telemetryState?.batteryPercentage ?: 0}%",
            bottomLabel = stringResource(R.string.battery_life),
            valueSize = 34.sp
        )

        // Row 2: Signal & Location
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val signalBars = (telemetryState?.signalStrength ?: 0)
            val signalLabel = when (signalBars) {
                4 -> stringResource(R.string.excellent)
                3 -> stringResource(R.string.good)
                2 -> stringResource(R.string.fair)
                1 -> stringResource(R.string.poor)
                else -> stringResource(R.string.none)
            }

            MetricGridCard(
                modifier = Modifier.weight(1f),
                icon = if (telemetryState?.connectivity == ConnectivitySource.WIFI) Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
                topLabelColor = PillBlueText,
                topLabelBg = PillBlueBg,
                value = telemetryState?.connectivity?.name ?: stringResource(R.string.none),
                bottomLabel = stringResource(R.string.network_source),
                valueSize = 34.sp
            )

            MetricGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.SignalCellularAlt,
                topLabelColor = IconColor,
                topLabelBg = IconCircleColor,
                value = signalLabel,
                bottomLabel = stringResource(R.string.signal_strength),
                valueSize = 26.sp
            )
        }

        MetricGridCard(
            icon = Icons.Default.LocationOn,
            modifier = Modifier.fillMaxWidth(),
            value = telemetryState?.locationLabel ?: stringResource(R.string.unknown),
            topLabelColor = DarkBlue,
            topLabel = if (telemetryState != null) formatRelativeTime(telemetryState.rawTimestamp) else stringResource(R.string.offline),
            topLabelBg = DarkBlue.copy(0.1f),
            bottomLabel = stringResource(R.string.location),
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
                value = if (notifGranted) stringResource(R.string.allowed) else stringResource(R.string.denied),
                bottomLabel = stringResource(R.string.notifications),
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
                value = if (isDeafSilent) stringResource(R.string.silent) else stringResource(R.string.normal),
                bottomLabel = stringResource(R.string.deaf_device_mode),
                valueSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ActionsScreenContent(
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
                icon = Icons.Default.Help,
                iconBg = Color(0xFFF3E5F5),
                iconColor = Color(0xFF7B1FA2)
            ),
            QuickActionItem(
                action = CaregiverAction.IM_COMING,
                descResId = R.string.action_im_coming_desc,
                icon = Icons.Default.DirectionsRun,
                iconBg = Color(0xFFE8F5E9),
                iconColor = Color(0xFF388E3C)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stringResource(R.string.quick_actions_title),
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF3C3228)
            )
        )
        Text(
            text = stringResource(R.string.quick_actions_subtitle),
            style = TextStyle(
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
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
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
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3C3228)
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(item.descResId),
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF7A8499)
                    )
                )
            }
        }
    }
}

@Composable
fun SystemActiveCard(
    isConnected: Boolean,
    localStatus: DeviceStatus,
    connectedHost: String?,
) {
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
                .padding(end = 8.dp)
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

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(),
                    text = if (localStatus == DeviceStatus.ONLINE || isConnected) stringResource(R.string.connected_to_nabda_device) else stringResource(
                        R.string.system_offline
                    ),
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
                if (connectedHost != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(Color.White.copy(alpha = 0.5f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            maxLines = 1,
                            text = connectedHost,
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

@Preview(locale = "ar")
@Composable
fun CaregiverDashboardPreview() {
    var selectedTab by remember { mutableStateOf(CaregiverTab.METRICS) }

    NabdaTheme {
        CaregiverDashboardContent(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
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
                    connectedHost = "192.168.1.6",
                )
            }
        }
    }
}

@Preview
@Composable
fun ActionsScreenPreview() {
    NabdaTheme {
        ActionsScreenContent(onActionClick = {})
    }
}
