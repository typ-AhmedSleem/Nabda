package com.typ.nabda.feature.deafblind

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.typ.nabda.core.haptic.AndroidHapticEngine
import com.typ.nabda.core.haptic.HapticEngine
import com.typ.nabda.core.model.HapticEnginePattern
import com.typ.nabda.designsystem.theme.NabdaTheme
import kotlinx.coroutines.launch

@Composable
fun VibratorTestScreen(
    hapticEngine: HapticEngine = AndroidHapticEngine(LocalContext.current),
) {
    val scope = rememberCoroutineScope()

    val customAmplitudes = remember { mutableStateListOf(0, 255) }
    val customDurations = remember { mutableStateListOf(100L, 200L) }

    val preDefinedPatterns = remember {
        listOf(
            HapticEnginePattern.NotConnected,
            HapticEnginePattern.ConfirmActionAgain,
            HapticEnginePattern.ActionSent,
            HapticEnginePattern.ActionNotConfirmed,
            HapticEnginePattern.NormalRequest,
            HapticEnginePattern.AssistanceRequest,
            HapticEnginePattern.EmergencyRequest,
        )
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    val pattern = HapticEnginePattern.Custom(
                        customAmplitudes = customAmplitudes.toIntArray(),
                        customDurations = customDurations.toLongArray()
                    )
                    hapticEngine.performHaptic(pattern)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Vibrate Pattern")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Pre-defined Patterns",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(preDefinedPatterns) { pattern ->
                Button(
                    onClick = {
                        scope.launch {
                            hapticEngine.performHaptic(pattern)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(pattern::class.simpleName ?: "Unknown Pattern")
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Custom Waveform Pattern",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(customAmplitudes) { index, amplitude ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amplitude.toString(),
                        onValueChange = { newValue ->
                            val value = newValue.toIntOrNull() ?: 0
                            customAmplitudes[index] = value.coerceIn(0, 255)
                        },
                        label = { Text("Amp $index") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = customDurations[index].toString(),
                        onValueChange = { newValue ->
                            val value = newValue.toLongOrNull() ?: 0L
                            customDurations[index] = value
                        },
                        label = { Text("Dur $index (ms)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        customAmplitudes.add(0)
                        customDurations.add(100L)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Amplitude Item")
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    NabdaTheme {
        VibratorTestScreen()
    }
}