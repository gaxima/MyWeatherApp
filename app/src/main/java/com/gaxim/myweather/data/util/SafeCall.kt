package com.gaxim.myweather.data.util

import com.gaxim.myweather.domain.model.DomainError
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/**
 * Runs [block] on [dispatcher] and turns every failure into a [DomainError], so nothing
 * framework-specific reaches the domain. Cancellation is rethrown, never swallowed.
 */
internal suspend fun <T> safeCall(
    dispatcher: CoroutineDispatcher,
    block: suspend () -> T,
): Result<T> = withContext(dispatcher) {
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: DomainError) {
        Result.failure(e)
    } catch (e: SocketTimeoutException) {
        Result.failure(DomainError.Timeout(e))
    } catch (e: IOException) {
        Result.failure(DomainError.Network(e))
    } catch (e: HttpException) {
        Result.failure(e.toDomainError())
    } catch (e: SerializationException) {
        Result.failure(DomainError.InvalidResponse("Malformed response", e))
    } catch (e: Exception) {
        Result.failure(DomainError.Unknown(e))
    }
}

private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_SERVER_ERRORS = 500

/** 5xx and 429 mean the server cannot serve us right now; any other status means a bad request. */
private fun HttpException.toDomainError(): DomainError {
    val code = code()
    return if (code >= HTTP_SERVER_ERRORS || code == HTTP_TOO_MANY_REQUESTS) {
        DomainError.Server(code, this)
    } else {
        DomainError.InvalidResponse("HTTP $code", this)
    }
}
