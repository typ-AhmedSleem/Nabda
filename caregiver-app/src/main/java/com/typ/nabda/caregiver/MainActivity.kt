package com.typ.nabda.caregiver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.typ.nabda.caregiver.di.initKoin
import com.typ.nabda.designsystem.theme.NabdaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start connectivity service
        val intent = android.content.Intent(this, com.typ.nabda.caregiver.service.CaregiverService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        enableEdgeToEdge()
        setContent {
            NabdaTheme {
                AppNavigation()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NabdaTheme {
        initKoin(LocalContext.current)
        AppNavigation()
    }
}