package com.typ.nabda.feature.pairing.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Hardcoded Colors
private val BackgroundColor = Color(0xFFFDF8E8)
private val PrimaryTextColor = Color(0xFF3C3228)
private val SecondaryTextColor = Color(0xFF7A8499)
private val IconCircleColor = Color(0xFFF5E1D1)
private val IconColor = Color(0xFFDA7A5E)
private val InputBorderColor = Color(0xFF7A8499)
private val QrButtonColor = Color(0xFFE58367)
private val PairButtonColor = Color(0xFF3C3228)

@Composable
fun CaregiverPairingScreen(
    onPairManual: (String) -> Unit,
    onPairUsingQr: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pairingCode by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Pair Device",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTextColor
                )
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Signal Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(IconCircleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SettingsInputAntenna,
                contentDescription = null,
                tint = IconColor,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "Ready to pair ..",
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryTextColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtitle
        Text(
            text = "Position your device nearby or use the pairing code from the device screen.",
            style = TextStyle(
                fontSize = 16.sp,
                color = SecondaryTextColor,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Manual Entry
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .border(1.dp, InputBorderColor, RoundedCornerShape(32.dp))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (pairingCode.isEmpty()) {
                Text(
                    text = "Enter pairing code manually",
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = SecondaryTextColor
                    )
                )
            }
            BasicTextField(
                value = pairingCode,
                onValueChange = { pairingCode = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    color = PrimaryTextColor
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pair Button (User requested)
        Button(
            onClick = { onPairManual(pairingCode) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PairButtonColor),
            shape = RoundedCornerShape(28.dp),
            enabled = pairingCode.isNotBlank()
        ) {
            Text(
                text = "Pair",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // OR Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = SecondaryTextColor.copy(alpha = 0.2f)
            )
            Text(
                text = "OR",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryTextColor
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = SecondaryTextColor.copy(alpha = 0.2f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // QR Button
        Button(
            onClick = onPairUsingQr,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = QrButtonColor),
            shape = RoundedCornerShape(32.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Pair using QR",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CaregiverPairingScreenPreview() {
    MaterialTheme {
        CaregiverPairingScreen(
            onPairManual = {},
            onPairUsingQr = {}
        )
    }
}
