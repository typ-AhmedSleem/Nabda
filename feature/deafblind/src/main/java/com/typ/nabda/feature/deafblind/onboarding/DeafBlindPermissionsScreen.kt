package com.typ.nabda.feature.deafblind.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.typ.nabda.designsystem.theme.NabdaTheme
import com.typ.nabda.feature.deafblind.R

@Composable
fun DeafBlindPermissionsScreen(
    onContinue: () -> Unit,
) {
    val context = LocalContext.current

    var notificationsEnabled by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        )
    }

    var locationEnabled by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        notificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: notificationsEnabled
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
        locationEnabled = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: locationEnabled
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                text = stringResource(R.string.permissions),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Headline
        Text(
            text = stringResource(R.string.enable_permissions),
            fontWeight = FontWeight.Bold,
            color = Color(0xFF001F35),
            fontSize = 28.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtitle
        Text(
            text = stringResource(R.string.permissions_subtitle),
            color = Color(0xFF74777F),
            lineHeight = 24.sp,
            fontSize = 16.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Permission Cards
        PermissionCard(
            title = stringResource(R.string.notifications_title),
            description = stringResource(R.string.notifications_desc),
            isEnabled = notificationsEnabled,
            onToggle = {
                if (!it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Cannot easily un-grant from app, but we can show state
                } else if (it) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        launcher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionCard(
            title = stringResource(R.string.location_title),
            description = stringResource(R.string.location_desc),
            isEnabled = locationEnabled,
            onToggle = {
                if (it) {
                    launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Continue Button
        Button(
            onClick = onContinue,
            enabled = notificationsEnabled && locationEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text(
                text = stringResource(R.string.continue_label),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (notificationsEnabled && locationEnabled) {
                    Color.White
                } else {
                    Color(0xFF74777F)
                }
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
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF74777F),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
            )
        }
    }
}

@Composable
@Preview(locale = "ar")
fun DeafBlindPermissionsScreenPreview() {
    NabdaTheme {
        DeafBlindPermissionsScreen(onContinue = {})
    }
}
