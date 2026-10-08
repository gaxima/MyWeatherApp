package com.gaxim.myweather.domain.model

/**
 * Failures a repository can report. Carried as the exception of a failed [Result], so callers
 * branch on the type instead of inspecting framework exceptions.
 *
 * An empty search result is not an error: it is a successful, empty list.
 */
sealed class DomainError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** The server could not be reached or the connection dropped. */
    class Network(cause: Throwable? = null) : DomainError("Network unavailable", cause)

    /** The request took too long. */
    class Timeout(cause: Throwable? = null) : DomainError("Request timed out", cause)

    /** The server answered, but with an error status or a body we cannot interpret. */
    class InvalidResponse(message: String, cause: Throwable? = null) : DomainError(message, cause)

    /** Anything not covered above. */
    class Unknown(cause: Throwable? = null) : DomainError("Unexpected error", cause)
}
