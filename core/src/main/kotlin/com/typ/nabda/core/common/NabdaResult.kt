package com.typ.nabda.core.common

sealed class NabdaResult<out T> {
    data class Success<T>(val data: T) : NabdaResult<T>()
    data class Error(val exception: Throwable) : NabdaResult<Nothing>()
    object Loading : NabdaResult<Nothing>()
}

inline fun <T> NabdaResult<T>.onSuccess(action: (T) -> Unit): NabdaResult<T> {
    if (this is NabdaResult.Success) action(data)
    return this
}

inline fun <T> NabdaResult<T>.onError(action: (Throwable) -> Unit): NabdaResult<T> {
    if (this is NabdaResult.Error) action(exception)
    return this
}
