package com.typ.nabda.core.actions

import com.typ.nabda.core.model.ActionPriority
import com.typ.nabda.core.model.GestureType
import com.typ.nabda.core.model.HapticEnginePattern
import com.typ.nabda.core.model.RequestedAction

object ActionMapper {

    private val gestureRequestedActionMap: Map<GestureType, RequestedAction> = mapOf(
        GestureType.TWO_FINGER_SWIPE_UP to RequestedAction(
            id = "FOOD_REQUEST",
            name = "Food Request",
            description = "User is requesting food",
            priority = ActionPriority.NORMAL,
            hapticPattern = HapticEnginePattern.NormalRequest
        ),
        GestureType.ONE_FINGER_SWIPE_DOWN to RequestedAction(
            id = "WATER_REQUEST",
            name = "عاوز اشرب",
            description = "User is requesting water",
            priority = ActionPriority.NORMAL,
            hapticPattern = HapticEnginePattern.NormalRequest
        ),
        GestureType.TWO_FINGER_SWIPE_RIGHT to RequestedAction(
            id = "BATHROOM_REQUEST",
            name = "Bathroom Request",
            description = "User needs to use the bathroom",
            priority = ActionPriority.NORMAL,
            hapticPattern = HapticEnginePattern.NormalRequest
        ),
        GestureType.ONE_FINGER_SWIPE_UP to RequestedAction(
            id = "NEED_HELP",
            name = "أحتاج إلى مساعدة",
            description = "User needs general assistance",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),
        GestureType.THREE_FINGER_SWIPE_DOWN to RequestedAction(
            id = "PAIN_ALERT",
            name = "Pain Alert",
            description = "User is experiencing pain",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),
        GestureType.ONE_FINGER_SWIPE_RIGHT to RequestedAction(
            id = "EMERGENCY_ALERT",
            name = "حاله طادئه !",
            description = "Critical Emergency Alert",
            priority = ActionPriority.EMERGENCY,
            hapticPattern = HapticEnginePattern.EmergencyAlert
        )
    )

    fun getActionForGesture(gesture: GestureType): RequestedAction? {
        return gestureRequestedActionMap[gesture]
    }

    fun getAllActions(): List<RequestedAction> = gestureRequestedActionMap.values.toList()
}
