package com.typ.nabda.core.actions

import com.typ.nabda.core.model.ActionPriority
import com.typ.nabda.core.model.GestureType
import com.typ.nabda.core.model.HapticEnginePattern
import com.typ.nabda.core.model.RequestedAction

object RequestedActionMapper {

    private val gestureRequestedActionMap: Map<GestureType, RequestedAction> = mapOf(
        // Region: 1 FINGER * //
        GestureType.ONE_FINGER_SWIPE_UP to RequestedAction(
            id = "NEED_BATHROOM",
            name = "",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),
        GestureType.ONE_FINGER_SWIPE_DOWN to RequestedAction(
            id = "NEED_CHANGE_CLOTH",
            name = "",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),
        GestureType.ONE_FINGER_SWIPE_RIGHT to RequestedAction(
            id = "NEED_EAT",
            name = "",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.EmergencyAlert
        ),
        GestureType.ONE_FINGER_SWIPE_LEFT to RequestedAction(
            id = "NEED_DRINK",
            name = "",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),

        // Region: 2 FINGERS * //
        GestureType.TWO_FINGER_SWIPE_UP to RequestedAction(
            id = "NEED_REST",
            name = "",
            description = "",
            priority = ActionPriority.EMERGENCY,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),
        GestureType.TWO_FINGER_SWIPE_DOWN to RequestedAction(
            id = "NEED_SLEEP",
            name = "",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),
        GestureType.TWO_FINGER_SWIPE_RIGHT to RequestedAction(
            id = "NEED_WASH_HANDS",
            name = "",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.EmergencyAlert
        ),
        GestureType.TWO_FINGER_SWIPE_LEFT to RequestedAction(
            id = "NEED_WASH_FACE",
            name = "",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),

        // Region: 3 FINGERS * //
        GestureType.THREE_FINGER_SWIPE_UP to RequestedAction(
            id = "FEELING_COLD",
            name = "",
            description = "",
            priority = ActionPriority.EMERGENCY,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),
        GestureType.THREE_FINGER_SWIPE_DOWN to RequestedAction(
            id = "FEELING_HOT",
            name = "",
            description = "",
            priority = ActionPriority.EMERGENCY,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),
        GestureType.THREE_FINGER_SWIPE_RIGHT to RequestedAction(
            id = "PAIN_IN_BELLY",
            name = "",
            description = "",
            priority = ActionPriority.EMERGENCY,
            hapticPattern = HapticEnginePattern.EmergencyAlert
        ),
        GestureType.THREE_FINGER_SWIPE_LEFT to RequestedAction(
            id = "PAIN_IN_HEAD",
            name = "",
            description = "",
            priority = ActionPriority.EMERGENCY,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),

        // Region: 4 FINGERS * //
        GestureType.FOUR_FINGER_SWIPE_UP to RequestedAction(
            id = "WANNA_WALK",
            name = "",
            description = "",
            priority = ActionPriority.NORMAL,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),
        GestureType.FOUR_FINGER_SWIPE_DOWN to RequestedAction(
            id = "",
            name = "",
            description = "",
            priority = ActionPriority.NORMAL,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),
        GestureType.FOUR_FINGER_SWIPE_RIGHT to RequestedAction(
            id = "OKAY",
            name = "",
            description = "",
            priority = ActionPriority.NORMAL,
            hapticPattern = HapticEnginePattern.EmergencyAlert
        ),
        GestureType.FOUR_FINGER_SWIPE_LEFT to RequestedAction(
            id = "NO",
            name = "",
            description = "",
            priority = ActionPriority.NORMAL,
            hapticPattern = HapticEnginePattern.HelpRequest
        ),
    )

    fun getActionForGesture(gesture: GestureType): RequestedAction? {
        return gestureRequestedActionMap[gesture]
    }

    fun getAllActions(): List<RequestedAction> = gestureRequestedActionMap.values.toList()
}
