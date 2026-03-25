package com.typ.nabda.core.gestures

import com.typ.nabda.core.model.GestureDirection
import com.typ.nabda.core.model.GestureInput
import com.typ.nabda.core.model.GestureType
import kotlin.math.abs

object GestureClassifier {

    private const val DRAG_THRESHOLD = 350

    fun classify(input: GestureInput): GestureType {
        val fingers = input.fingerCount
        val direction = input.direction

        return when (fingers) {
            1 -> when (direction) {
                GestureDirection.UP -> GestureType.ONE_FINGER_SWIPE_UP
                GestureDirection.DOWN -> GestureType.ONE_FINGER_SWIPE_DOWN
                GestureDirection.LEFT -> GestureType.ONE_FINGER_SWIPE_LEFT
                GestureDirection.RIGHT -> GestureType.ONE_FINGER_SWIPE_RIGHT
                GestureDirection.NONE -> GestureType.ONE_FINGER_TAP // Simplification for now
            }

            2 -> when (direction) {
                GestureDirection.UP -> GestureType.TWO_FINGER_SWIPE_UP
                GestureDirection.DOWN -> GestureType.TWO_FINGER_SWIPE_DOWN
                GestureDirection.LEFT -> GestureType.TWO_FINGER_SWIPE_LEFT
                GestureDirection.RIGHT -> GestureType.TWO_FINGER_SWIPE_RIGHT
                GestureDirection.NONE -> GestureType.TWO_FINGER_TAP
            }

            3 -> when (direction) {
                GestureDirection.UP -> GestureType.THREE_FINGER_SWIPE_UP
                GestureDirection.DOWN -> GestureType.THREE_FINGER_SWIPE_DOWN
                GestureDirection.LEFT -> GestureType.THREE_FINGER_SWIPE_LEFT
                GestureDirection.RIGHT -> GestureType.THREE_FINGER_SWIPE_RIGHT
                GestureDirection.NONE -> GestureType.UNKNOWN
            }

            4 -> when (direction) {
                GestureDirection.UP -> GestureType.FOUR_FINGER_SWIPE_UP
                GestureDirection.DOWN -> GestureType.FOUR_FINGER_SWIPE_DOWN
                GestureDirection.LEFT -> GestureType.FOUR_FINGER_SWIPE_LEFT
                GestureDirection.RIGHT -> GestureType.FOUR_FINGER_SWIPE_RIGHT
                GestureDirection.NONE -> GestureType.UNKNOWN
            }

            else -> GestureType.UNKNOWN
        }
    }

    fun calculateDirection(startX: Float, startY: Float, endX: Float, endY: Float): GestureDirection {
        val dx = endX - startX
        val dy = endY - startY
        val absDx = abs(dx)
        val absDy = abs(dy)

        if (absDx < DRAG_THRESHOLD && absDy < DRAG_THRESHOLD) return GestureDirection.NONE // Tap threshold check

        return if (absDx > absDy) {
            if (dx > 0) GestureDirection.RIGHT else GestureDirection.LEFT
        } else {
            if (dy > 0) GestureDirection.DOWN else GestureDirection.UP
        }
    }
}
