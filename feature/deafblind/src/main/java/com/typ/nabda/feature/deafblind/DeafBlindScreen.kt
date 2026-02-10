package com.typ.nabda.feature.deafblind

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typ.nabda.core.gestures.GestureClassifier
import com.typ.nabda.core.model.GestureInput
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DeafBlindScreen(
    viewModel: DeafBlindViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectCustomGestures { input ->
                    viewModel.onGestureInput(input)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        ) {
            Text(
                text = state.feedbackMessage,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (state.lastAction != null) {
                Text(
                    text = "Priority: ${state.lastAction?.priority}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

suspend fun PointerInputScope.detectCustomGestures(onGesture: (GestureInput) -> Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        mutableListOf(down.id)
        var maxPointerCount = 1

        down.position.x
        down.position.y

        // Accumulate movement? Or just end - start?
        // Simpler: Track start of primary pointer and end of primary pointer.
        // But multi-touch swipe implies all fingers move.
        // Let's track the centroid.

        var dragSumX = 0f
        var dragSumY = 0f

        do {
            val event = awaitPointerEvent()
            val currentCount = event.changes.size
            if (currentCount > maxPointerCount) {
                maxPointerCount = currentCount
                // Reset start if fingers added?
                // No, keep tracking.
            }

            // Calculate movement
            val dragChange = event.calculatePan()
            dragSumX += dragChange.x
            dragSumY += dragChange.y

            // Consume? No, let standard gestures work if needed?
            // But we are full screen custom. Consume everything.
            event.changes.forEach { it.consume() }

        } while (event.changes.any { it.pressed })

        // Gesture Ended.
        // Analyze dragSum
        val direction = GestureClassifier.calculateDirection(0f, 0f, dragSumX, dragSumY)

        // Filter based on existing pointers count?
        // maxPointerCount gives us the number of fingers used.

        onGesture(GestureInput(maxPointerCount, direction))
    }
}
