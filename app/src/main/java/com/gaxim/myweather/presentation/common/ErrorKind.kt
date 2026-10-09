package com.gaxim.myweather.presentation.common

import com.gaxim.myweather.domain.model.DomainError

/** What went wrong, in terms the UI can word for the user. */
enum class ErrorKind {
    NETWORK,
    TIMEOUT,
    INVALID_RESPONSE,
    UNKNOWN,
}

/** Maps any failure to an [ErrorKind]; anything that is not a [DomainError] is [ErrorKind.UNKNOWN]. */
fun Throwable.toErrorKind(): ErrorKind = when (this) {
    is DomainError.Network -> ErrorKind.NETWORK
    is DomainError.Timeout -> ErrorKind.TIMEOUT
    is DomainError.InvalidResponse -> ErrorKind.INVALID_RESPONSE
    else -> ErrorKind.UNKNOWN
}
