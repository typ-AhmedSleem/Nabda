package com.typ.nabda.feature.deafblind

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typ.nabda.core.common.NabdaResult
import com.typ.nabda.core.dispatcher.SignalDispatcher
import com.typ.nabda.core.gestures.GestureClassifier
import com.typ.nabda.core.haptic.AndroidHapticEngine
import com.typ.nabda.core.messaging.IncomingActionDispatcher
import com.typ.nabda.core.model.GestureInput
import com.typ.nabda.core.model.RequestedAction
import com.typ.nabda.designsystem.theme.NabdaTheme
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeafBlindScreen(
    viewModel: DeafBlindViewModel = koinViewModel(),
    onNavigateToTests: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hasConnectedClients by remember(uiState.connectedClientsCount) {
        derivedStateOf { uiState.connectedClientsCount > 0 }
    }
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (hasConnectedClients) {
                        MaterialTheme.colorScheme.background
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    },
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                title = {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            alignment = Alignment.CenterVertically,
                            space = 8.dp,
                        )
                    ) {
                        Text(
                            modifier = Modifier.clickable { onNavigateToTests() },
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            text = stringResource(R.string.nabda),
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
            )
        },
        containerColor = if (hasConnectedClients) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.errorContainer
        }
    ) { insetPaddings ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(insetPaddings)
                .pointerInput(Unit) {
//                    if (!hasConnectedClients) return@pointerInput
                    detectCustomGestures(
                        onPointersChanged = { viewModel.onPointersChanged(it) },
                        onGesture = { viewModel.onGestureInput(it) }
                    )
                }
//                .then(if (uiState.connectedClientsCount == 0) Modifier.background(Color.Black.copy(alpha = 0.3f)) else Modifier)
        ) {
            // Action Feedback
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(hasConnectedClients) {
                    Text(
                        text = uiState.feedbackMessage.asString(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = if (uiState.isWaitingForConfirmation) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }

                if (uiState.isWaitingForConfirmation) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.width(200.dp),
                        trackColor = primaryColor.copy(0.1f),
                        color = primaryColor,
                    )
                }
            }

            // Pointer Visuals (Circles under fingers)
            Canvas(modifier = Modifier.fillMaxSize()) {
                uiState.pointers.values.forEach { offset ->
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.6f),
                        radius = 20.dp.toPx(),
                        center = offset
                    )
                }
            }

            // Bottom Navigation Icons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(
                    alignment = Alignment.CenterHorizontally,
                    space = 16.dp,
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
//                BottomIcon(Icons.Default.Swipe, stringResource(R.string.swipe))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 36.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(
                            color = if (hasConnectedClients) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                ) {
                    Text(
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        text = if (hasConnectedClients) stringResource(
                            R.string.clients_connected_format,
                            uiState.connectedClientsCount
                        ) else stringResource(R.string.no_clients_connected),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (hasConnectedClients) {
                            MaterialTheme.colorScheme.background
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
//                BottomIcon(Icons.Default.TouchApp, stringResource(R.string.tap))
            }
        }
    }
}

@Composable
fun BottomIcon(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

suspend fun PointerInputScope.detectCustomGestures(
    onPointersChanged: (Map<Int, Offset>) -> Unit,
    onGesture: (GestureInput) -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val activePointers = mutableMapOf<Int, Offset>()
        activePointers[down.id.value.toInt()] = down.position
        onPointersChanged(activePointers.toMap())

        var maxPointerCount = 1
        var dragSumX = 0f
        var dragSumY = 0f

        do {
            val event = awaitPointerEvent()
            val currentCount = event.changes.size
            if (currentCount > maxPointerCount) {
                maxPointerCount = currentCount
            }

            // Update pointers for visual feedback
            activePointers.clear()
            event.changes.forEach { change ->
                if (change.pressed) {
                    activePointers[change.id.value.toInt()] = change.position
                }
            }
            onPointersChanged(activePointers.toMap())

            // Calculate movement
            val dragChange = event.calculatePan()
            dragSumX += dragChange.x
            dragSumY += dragChange.y

            event.changes.forEach { it.consume() }

        } while (event.changes.any { it.pressed })

        // Clear pointers when finished
        onPointersChanged(emptyMap())

        // Gesture Ended.
        val direction = GestureClassifier.calculateDirection(0f, 0f, dragSumX, dragSumY)
        onGesture(GestureInput(maxPointerCount, direction))
    }
}

@Preview(locale = "ar", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun DeafBlindScreenPreviewNoClients() {
    NabdaTheme {
        val ctx = LocalContext.current
        val vm = remember {
            DeafBlindViewModel(
                signalDispatcher = object : SignalDispatcher {
                    override suspend fun dispatchAction(requestedAction: RequestedAction): NabdaResult<Unit> {
                        return NabdaResult.Success(Unit)
                    }
                },
                incomingActionDispatcher = IncomingActionDispatcher(),
                hapticEngine = AndroidHapticEngine(ctx),
            )
        }
        DeafBlindScreen(vm)
    }
}