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
            name = "عاوز الحمام",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.AssistanceRequest
        ),
        GestureType.ONE_FINGER_SWIPE_DOWN to RequestedAction(
            id = "NEED_CHANGE_CLOTH",
            name = "عاوز أغير هدومي",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.AssistanceRequest
        ),
        GestureType.ONE_FINGER_SWIPE_RIGHT to RequestedAction(
            id = "NEED_EAT",
            name = "عاوز أكل",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.EmergencyRequest
        ),
        GestureType.ONE_FINGER_SWIPE_LEFT to RequestedAction(
            id = "NEED_DRINK",
            name = "عاوز أشرب",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.AssistanceRequest
        ),

        // Region: 2 FINGERS * //
        GestureType.TWO_FINGER_SWIPE_UP to RequestedAction(
            id = "NEED_REST",
            name = "أنا تعبان",
            description = "",
            priority = ActionPriority.EMERGENCY,
            hapticPattern = HapticEnginePattern.AssistanceRequest
        ),
        GestureType.TWO_FINGER_SWIPE_DOWN to RequestedAction(
            id = "NEED_SLEEP",
            name = "عاوز أنام",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.AssistanceRequest
        ),
        GestureType.TWO_FINGER_SWIPE_RIGHT to RequestedAction(
            id = "NEED_WASH_HANDS",
            name = "عاوز أغسل إيدي",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.EmergencyRequest
        ),
        GestureType.TWO_FINGER_SWIPE_LEFT to RequestedAction(
            id = "NEED_WASH_FACE",
            name = "عاوز أغسل وشي",
            description = "",
            priority = ActionPriority.ASSISTANCE,
            hapticPattern = HapticEnginePattern.AssistanceRequest
        ),

        // Region: 3 FINGERS * //
        GestureType.THREE_FINGER_SWIPE_UP to RequestedAction(
            id = "FEELING_COLD",
            name = "أنا بردان",
            description = "",
            priority = ActionPriority.EMERGENCY,
            hapticPattern = HapticEnginePattern.AssistanceRequest
        ),
        GestureType.THREE_FINGER_SWIPE_DOWN to RequestedAction(
            id = "FEELING_HOT",
            name = "أنا حران",
            description = "",
            priority = ActionPriority.EMERGENCY,
            hapticPattern = HapticEnginePattern.AssistanceRequest
        ),
        GestureType.THREE_FINGER_SWIPE_RIGHT to RequestedAction(
            id = "PAIN_IN_BELLY",
            name = "عندي ألم في بطني",
            description = "",
            priority = ActionPriority.EMERGENCY,
            hapticPattern = HapticEnginePattern.EmergencyRequest
        ),
        GestureType.THREE_FINGER_SWIPE_LEFT to RequestedAction(
            id = "PAIN_IN_HEAD",
            name = "عندي ألم في راسي",
            description = "",
            priority = ActionPriority.EMERGENCY,
            hapticPattern = HapticEnginePattern.AssistanceRequest
        ),

        // Region: 4 FINGERS * //
        GestureType.FOUR_FINGER_SWIPE_UP to RequestedAction(
            id = "WANNA_WALK",
            name = "عاوز أتمشي شوية",
            description = "",
            priority = ActionPriority.NORMAL,
            hapticPattern = HapticEnginePattern.NormalRequest
        ),
        GestureType.FOUR_FINGER_SWIPE_DOWN to RequestedAction(
            id = "",
            name = "",
            description = "",
            priority = ActionPriority.NORMAL,
            hapticPattern = HapticEnginePattern.NormalRequest
        ),
        GestureType.FOUR_FINGER_SWIPE_RIGHT to RequestedAction(
            id = "OKAY",
            name = "أيوا! أنا موافق",
            description = "",
            priority = ActionPriority.NORMAL,
            hapticPattern = HapticEnginePattern.NormalRequest
        ),
        GestureType.FOUR_FINGER_SWIPE_LEFT to RequestedAction(
            id = "NO",
            name = "لا! أنا مش موافق",
            description = "",
            priority = ActionPriority.NORMAL,
            hapticPattern = HapticEnginePattern.NormalRequest
        ),
    )

    fun getActionForGesture(gesture: GestureType): RequestedAction? {
        return gestureRequestedActionMap[gesture]
    }

    fun getAllActions(): List<RequestedAction> = gestureRequestedActionMap.values.toList()
}
