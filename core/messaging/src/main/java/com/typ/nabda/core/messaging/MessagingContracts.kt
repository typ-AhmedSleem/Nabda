package com.typ.nabda.core.messaging

import com.typ.nabda.core.common.NabdaResult
import com.typ.nabda.core.model.Action

interface TokenRepository {
    suspend fun updateMyToken(token: String)
    suspend fun getRemoteToken(uuid: String): String?
}

interface MessageSender {
    suspend fun sendAction(
        targetToken: String,
        action: Action,
    ): NabdaResult<Unit>
}
