package com.typ.nabda.feature.deafblind

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typ.nabda.core.common.NabdaResult
import com.typ.nabda.core.dispatcher.SignalDispatcher
import com.typ.nabda.core.gestures.GestureClassifier
import com.typ.nabda.core.model.Action
import com.typ.nabda.core.model.GestureInput
import org.koin.compose.viewmodel.koinViewModel

// Design colors
private val BackgroundColor = Color(0xFFFDF8E8) // Creamy color from design
private val PointerColor = Color(0xFFFFA000).copy(alpha = 0.6f) // Orange glow
private val IconTint = Color(0xFF3C3228)

@Composable
fun DeafBlindScreen(
    viewModel: DeafBlindViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .pointerInput(Unit) {
                detectCustomGestures(
                    onPointersChanged = { viewModel.onPointersChanged(it) },
                    onGesture = { viewModel.onGestureInput(it) }
                )
            }
    ) {
        // App Title at the top center
        Text(
            text = "Nabda",
            style = MaterialTheme.typography.headlineMedium,
            color = IconTint,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        // Action Feedback
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.feedbackMessage,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                color = IconTint,
                textAlign = TextAlign.Center
            )
        }

        // Pointer Visuals (Circles under fingers)
        Canvas(modifier = Modifier.fillMaxSize()) {
            state.pointers.values.forEach { offset ->
                drawCircle(
                    color = PointerColor,
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
                .padding(bottom = 32.dp)
                .padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomIcon(Icons.Default.Swipe, "SWIPE")
            BottomIcon(Icons.Default.TouchApp, "TAP")
        }
    }
}

@Composable
fun BottomIcon(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = IconTint,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = IconTint.copy(alpha = 0.6f)
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

@Preview
@Composable
private fun DeafBlindScreenPreview() {
    MaterialTheme {
        val vm = remember {
            DeafBlindViewModel(
                object : SignalDispatcher {
                    override suspend fun dispatchAction(action: Action): NabdaResult<Unit> {
                        return NabdaResult.Success(Unit)
                    }
                }
            )
        }
        DeafBlindScreen(vm)
    }
}