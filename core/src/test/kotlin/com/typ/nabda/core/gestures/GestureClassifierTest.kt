package com.typ.nabda.core.gestures

import com.typ.nabda.core.model.GestureDirection
import com.typ.nabda.core.model.GestureInput
import com.typ.nabda.core.model.GestureType
import org.junit.Assert.assertEquals
import org.junit.Test

class GestureClassifierTest {

    @Test
    fun classify_oneFingerSwipeUp_returnsOneFingerSwipeUp() {
        val input = GestureInput(1, GestureDirection.UP)
        val result = GestureClassifier.classify(input)
        assertEquals(GestureType.ONE_FINGER_SWIPE_UP, result)
    }

    @Test
    fun classify_twoFingerSwipeDown_returnsTwoFingerSwipeDown() {
        val input = GestureInput(2, GestureDirection.DOWN)
        val result = GestureClassifier.classify(input)
        assertEquals(GestureType.TWO_FINGER_SWIPE_DOWN, result)
    }

    @Test
    fun classify_threeFingerSwipeRight_returnsThreeFingerSwipeRight() {
        val input = GestureInput(3, GestureDirection.RIGHT)
        val result = GestureClassifier.classify(input)
        assertEquals(GestureType.THREE_FINGER_SWIPE_RIGHT, result)
    }

    @Test
    fun calculateDirection_positiveY_dominates_returnsDown() {
        // Drag (+10, +100) -> Down
        val result = GestureClassifier.calculateDirection(0f, 0f, 10f, 100f)
        assertEquals(GestureDirection.DOWN, result)
    }

    @Test
    fun calculateDirection_negativeX_dominates_returnsLeft() {
        // Drag (-100, +10) -> Left
        val result = GestureClassifier.calculateDirection(0f, 0f, -100f, 10f)
        assertEquals(GestureDirection.LEFT, result)
    }

    @Test
    fun calculateDirection_smallMovement_returnsNone() {
        // Drag (10, 10) -> None (Threshold 50)
        val result = GestureClassifier.calculateDirection(0f, 0f, 10f, 10f)
        assertEquals(GestureDirection.NONE, result)
    }
}
