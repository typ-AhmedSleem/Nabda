package com.typ.nabda.deaf.helpers

import com.typ.nabda.core.model.CaregiverAction
import com.typ.nabda.core.model.HapticEnginePattern

object HapticPatternRetriever {

    private val actionHapticMap = mapOf(
        CaregiverAction.FOOD_READY.name to HapticEnginePattern.FoodReady,
        CaregiverAction.COME_CLOSER.name to HapticEnginePattern.ComeCloser,
        CaregiverAction.SLEEP_TIME.name to HapticEnginePattern.SleepTime,
        CaregiverAction.ARE_YOU_SICK.name to HapticEnginePattern.AreYouSick,
        CaregiverAction.DO_WANT_THIS.name to HapticEnginePattern.DoWantThis,
        CaregiverAction.IM_COMING.name to HapticEnginePattern.ImComing,
        CaregiverAction.HELP_REQUEST.name to HapticEnginePattern.AssistanceRequest,
        CaregiverAction.FALL.name to HapticEnginePattern.EmergencyRequest,
    )

    fun retrievePatternForAction(actionId: String): HapticEnginePattern {
        return actionHapticMap[actionId] ?: HapticEnginePattern.NormalRequest
    }

}