package com.typ.nabda.feature.caregiver.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.typ.nabda.designsystem.theme.NabdaTheme
import com.typ.nabda.feature.caregiver.R

@Composable
fun NotificationPermissionStartupScreen(
    onPermissionGranted: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionState by remember { mutableStateOf<PermissionState>(PermissionState.Checking) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            // Check if it's permanently denied
            permissionState = PermissionState.Denied
        }
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (status == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                permissionState = PermissionState.Denied
            }
        } else {
            // Before Android 13, permission is granted by default, but check App Settings just in case
            val areEnabled = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
            if (areEnabled) {
                onPermissionGranted()
            } else {
                permissionState = PermissionState.Denied
            }
        }
    }

    // React to lifecycle changes to re-check permission when user returns from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (status == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            val areEnabled = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
            if (areEnabled) {
                onPermissionGranted()
            } else {
                permissionState = PermissionState.Denied
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (permissionState) {
            PermissionState.Checking -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = stringResource(R.string.checking_permissions),
                        fontSize = 24.sp,
                    )
                }
            }

            PermissionState.Denied -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.notifications),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.notification_permission_denied),
                        fontSize = 20.sp,
                        lineHeight = 30.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = {
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text(
                            text = stringResource(R.string.open_notification_settings),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private sealed class PermissionState {
    object Checking : PermissionState()
    object Denied : PermissionState()
}

@Preview(locale = "ar")
@Composable
private fun Preview() {
    val permissionState = PermissionState.Denied
    NabdaTheme {
        Scaffold {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                when (permissionState) {
                    PermissionState.Checking -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(48.dp))
                            Text(
                                text = stringResource(R.string.checking_permissions),
                                fontSize = 24.sp,
                            )
                        }
                    }

                    PermissionState.Denied -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.notifications),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.notification_permission_denied),
                                fontSize = 20.sp,
                                lineHeight = 30.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(48.dp))

                            Button(
                                onClick = {
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Text(
                                    text = stringResource(R.string.open_notification_settings),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}