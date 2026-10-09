package com.gaxim.myweather.presentation.common

import com.gaxim.myweather.domain.model.DomainError
import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorKindTest {

    @Test
    fun `given each domain error, when mapped, then it has its own kind`() {
        assertEquals(ErrorKind.NETWORK, DomainError.Network().toErrorKind())
        assertEquals(ErrorKind.TIMEOUT, DomainError.Timeout().toErrorKind())
        assertEquals(ErrorKind.INVALID_RESPONSE, DomainError.InvalidResponse("bad").toErrorKind())
        assertEquals(ErrorKind.UNKNOWN, DomainError.Unknown().toErrorKind())
    }

    @Test
    fun `given a non-domain exception, when mapped, then it is unknown`() {
        assertEquals(ErrorKind.UNKNOWN, IllegalStateException("boom").toErrorKind())
    }
}
