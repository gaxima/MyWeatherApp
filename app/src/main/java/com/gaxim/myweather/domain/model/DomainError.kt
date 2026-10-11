package com.gaxim.myweather.domain.model

/**
 * Failures a repository can report. Carried as the exception of a failed [Result], so callers
 * branch on the type instead of inspecting framework exceptions.
 *
 * An empty search result is not an error: it is a successful, empty list.
 */
sealed class DomainError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Network(cause: Throwable? = null) : DomainError("Network unavailable", cause)

    class Timeout(cause: Throwable? = null) : DomainError("Request timed out", cause)

    /** The server answered, but with a client error status or a body we cannot interpret. */
    class InvalidResponse(message: String, cause: Throwable? = null) : DomainError(message, cause)

    /** The server is failing or rate limiting (5xx or 429): our request was fine, retry later. */
    class Server(val code: Int, cause: Throwable? = null) : DomainError("Server error $code", cause)

    class Unknown(cause: Throwable? = null) : DomainError("Unexpected error", cause)
}
