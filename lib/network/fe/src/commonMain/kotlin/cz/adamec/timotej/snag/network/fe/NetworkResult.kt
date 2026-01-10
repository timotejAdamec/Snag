package cz.adamec.timotej.snag.network.fe

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class NetworkResult<out T> {

    data class Success<out T>(val data: T) : NetworkResult<T>()

    sealed class Failure : NetworkResult<Nothing>() {
        abstract val exception: NetworkException

        data class Connectivity(override val exception: NetworkException.NetworkUnavailable) : Failure()

        data class Unauthorized(override val exception: NetworkException.ClientError.Unauthorized) : Failure()

        data class NotFound(override val exception: NetworkException.ClientError.NotFound) : Failure()

        data class UserError(
            override val exception: NetworkException.ClientError.OtherClientError,
            val message: String? = null
        ) : Failure()

        data class ServerError(override val exception: NetworkException.ServerError) : Failure()

        data class Programmer(override val exception: NetworkException.ProgrammerError) : Failure()
    }

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.data
    fun exceptionOrNull(): NetworkException? = (this as? Failure)?.exception

    inline fun <R> map(transform: (T) -> R): NetworkResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Failure -> this
        }
    }

    companion object {
        inline fun <T> runSafelyOnNetwork(block: () -> T): NetworkResult<T> {
            return try {
                Success(block())
            } catch (e: NetworkException) {
                when (e) {
                    is NetworkException.NetworkUnavailable -> Failure.Connectivity(e)
                    is NetworkException.ClientError.Unauthorized -> Failure.Unauthorized(e)
                    is NetworkException.ClientError.NotFound -> Failure.NotFound(e)
                    is NetworkException.ClientError.OtherClientError -> Failure.UserError(e, e.message)
                    is NetworkException.ServerError -> Failure.ServerError(e)
                    is NetworkException.ProgrammerError -> Failure.Programmer(e)
                }
            }
        }
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> NetworkResult<T>.onSuccess(action: (value: T) -> Unit): NetworkResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (this is NetworkResult.Success) {
        action(data)
    }
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> NetworkResult<T>.onFailure(action: (exception: NetworkException) -> Unit): NetworkResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (this is NetworkResult.Failure) {
        action(exception)
    }
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> NetworkResult<T>.onConnectivityError(action: (NetworkException.NetworkUnavailable) -> Unit): NetworkResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (this is NetworkResult.Failure.Connectivity) {
        action(exception)
    }
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> NetworkResult<T>.onUnauthorized(action: (NetworkException.ClientError.Unauthorized) -> Unit): NetworkResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (this is NetworkResult.Failure.Unauthorized) {
        action(exception)
    }
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> NetworkResult<T>.onNotFound(action: (NetworkException.ClientError.NotFound) -> Unit): NetworkResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (this is NetworkResult.Failure.NotFound) {
        action(exception)
    }
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> NetworkResult<T>.onClientError(action: (exception: NetworkException.ClientError.OtherClientError, message: String?) -> Unit): NetworkResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (this is NetworkResult.Failure.UserError) {
        action(exception, message)
    }
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> NetworkResult<T>.onServerError(action: (exception: NetworkException.ServerError) -> Unit): NetworkResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (this is NetworkResult.Failure.ServerError) {
        action(exception)
    }
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> NetworkResult<T>.onProgrammerError(action: (NetworkException.ProgrammerError) -> Unit): NetworkResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (this is NetworkResult.Failure.Programmer) {
        action(exception)
    }
    return this
}
