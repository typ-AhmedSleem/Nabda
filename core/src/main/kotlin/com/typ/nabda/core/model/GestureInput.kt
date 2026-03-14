package com.typ.nabda.core.model

enum class GestureDirection {
    UP, DOWN, LEFT, RIGHT, NONE
}

data class GestureInput(
    val fingerCount: Int,
    val direction: GestureDirection,
    val items: List<GestureInputItem> = emptyList(),
)

data class GestureInputItem(
    val x: Float,
    val y: Float,
)
