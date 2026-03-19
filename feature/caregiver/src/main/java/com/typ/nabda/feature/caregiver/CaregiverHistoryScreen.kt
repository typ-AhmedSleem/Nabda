package com.typ.nabda.feature.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typ.nabda.core.model.ActionPriority
import com.typ.nabda.core.model.Alert
import com.typ.nabda.core.model.SupportedAction
import org.koin.compose.viewmodel.koinViewModel
import org.ocpsoft.prettytime.PrettyTime
import java.util.Date
import java.util.Locale

// Hardcoded colors for consistency
private val BackgroundColor = Color(0xFFFDF8E8)
private val PrimaryTextColor = Color(0xFF3C3228)
private val SecondaryTextColor = Color(0xFF7A8499)
private val FilterSelectedColor = Color(0xFF2E4532)
private val FilterUnselectedColor = Color.White
private val DividerColor = Color(0xFFE5E7EB)

@Composable
fun CaregiverHistoryScreen(
    viewModel: CaregiverViewModel = koinViewModel(),
) {
    val alerts by viewModel.filteredAlerts.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    CaregiverHistoryContent(
        alerts = alerts,
        selectedFilter = selectedFilter,
        onFilterSelected = viewModel::onFilterSelected
    )
}

@Composable
fun CaregiverHistoryContent(
    alerts: List<Alert>,
    selectedFilter: SupportedAction?,
    onFilterSelected: (SupportedAction?) -> Unit,
) {
    LocalContext.current
    val prettyTime = remember { PrettyTime(Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // App Title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.history),
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTextColor
                )
            )
        }

        // Filter Chips
        /*LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            // "All Alerts" Chip
            item {
                FilterChip(
                    label = "All Alerts",
                    isSelected = selectedFilter == null,
                    onClick = { onFilterSelected(null) }
                )
            }

            // Dynamic Action Chips
            items(SupportedAction.entries) { action ->
                // In a real app we'd use getString(id) but for now we follow the "id" or mapping
                val label = when(action) {
                    SupportedAction.HELP_REQUEST -> "Help Requests"
                    SupportedAction.FALL -> "Falls"
                }
                FilterChip(
                    label = label,
                    isSelected = selectedFilter == action,
                    onClick = { onFilterSelected(action) }
                )
            }
        }*/

        Spacer(modifier = Modifier.height(16.dp))

        // Alerts List
        if (alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.no_history_available),
                    style = TextStyle(color = SecondaryTextColor, fontSize = 16.sp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(alerts) { alert ->
                    HistoryItem(alert, prettyTime)
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CaregiverHistoryPreview() {
    MaterialTheme {
        CaregiverHistoryContent(
            alerts = listOf(
                Alert("help_request", "Help Requested", ActionPriority.NORMAL, System.currentTimeMillis() - 60000),
                Alert("fall", "Fall Detected", ActionPriority.EMERGENCY, System.currentTimeMillis() - 3600000)
            ),
            selectedFilter = SupportedAction.HELP_REQUEST,
            onFilterSelected = {}
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) FilterSelectedColor else FilterUnselectedColor,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else PrimaryTextColor
            )
        )
    }
}

@Composable
fun HistoryItem(alert: Alert, prettyTime: PrettyTime) {
    val icon = when (alert.actionId) {
        SupportedAction.HELP_REQUEST.id -> Icons.Default.BackHand
        SupportedAction.FALL.id -> Icons.AutoMirrored.Filled.DirectionsWalk
        else -> Icons.Default.Notifications // Default
    }

    val label = when (alert.actionId) {
        SupportedAction.HELP_REQUEST.id -> stringResource(R.string.help_requested)
        SupportedAction.FALL.id -> stringResource(R.string.fall_detected)
        else -> alert.actionName.uppercase()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        //.background(Color.White), // Usually list items have white bg but let's keep it transparent for now
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryTextColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryTextColor,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = prettyTime.format(Date(alert.timestamp)),
            style = TextStyle(
                fontSize = 16.sp,
                color = SecondaryTextColor.copy(alpha = 0.8f)
            )
        )
    }
}
