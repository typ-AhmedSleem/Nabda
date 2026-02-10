package com.typ.nabda.core.actions

import com.typ.nabda.core.model.Action
import com.typ.nabda.core.model.ActionPriority
import com.typ.nabda.core.model.GestureType

object ActionMapper {

    private val gestureActionMap: Map<GestureType, Action> = mapOf(
        GestureType.TWO_FINGER_SWIPE_UP to Action(
            id = "FOOD_REQUEST",
            name = "Food Request",
            description = "User is requesting food",
            priority = ActionPriority.NORMAL
        ),
        GestureType.TWO_FINGER_SWIPE_DOWN to Action(
            id = "WATER_REQUEST",
            name = "Water Request",
            description = "User is requesting water",
            priority = ActionPriority.NORMAL
        ),
        GestureType.TWO_FINGER_SWIPE_RIGHT to Action(
            id = "BATHROOM_REQUEST",
            name = "Bathroom Request",
            description = "User needs to use the bathroom",
            priority = ActionPriority.NORMAL
        ),
        GestureType.THREE_FINGER_SWIPE_UP to Action(
            id = "NEED_HELP",
            name = "Need Assistance",
            description = "User needs general assistance",
            priority = ActionPriority.ASSISTANCE
        ),
        GestureType.THREE_FINGER_SWIPE_DOWN to Action(
            id = "PAIN_ALERT",
            name = "Pain Alert",
            description = "User is experiencing pain",
            priority = ActionPriority.ASSISTANCE
        ),
        GestureType.THREE_FINGER_SWIPE_LEFT to Action(
            id = "EMERGENCY_ALERT",
            name = "EMERGENCY",
            description = "Critical Emergency Alert",
            priority = ActionPriority.EMERGENCY
        )
    )

    fun getActionForGesture(gesture: GestureType): Action? {
        return gestureActionMap[gesture]
    }

    fun getAllActions(): List<Action> = gestureActionMap.values.toList()
}
