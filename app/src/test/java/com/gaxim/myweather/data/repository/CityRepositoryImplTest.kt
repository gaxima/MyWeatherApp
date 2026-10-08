package com.gaxim.myweather.data.repository

import com.gaxim.myweather.data.remote.GeocodingApi
import com.gaxim.myweather.data.remote.dto.GeocodingResponseDto
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DomainError
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CityRepositoryImplTest {

    private val server = MockWebServer()
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.shutdown()

    private fun repository(readTimeoutMs: Long = 5_000) =
        CityRepositoryImpl(server.createApi<GeocodingApi>(readTimeoutMs), dispatcher)

    private fun respond(code: Int = 200, body: String = "{}") =
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))

    @Test
    fun `given results, when searching, then cities are mapped`() = runTest {
        respond(
            body = """{"results":[{"id":1,"name":"Oslo","country":"Norway","admin1":"Oslo",
                "latitude":59.91,"longitude":10.75,"unknown":"x"}]}""",
        )

        val result = repository().search("Oslo")

        assertEquals(listOf(City("Oslo", "Norway", "Oslo", 59.91, 10.75)), result.getOrThrow())
    }

    @Test
    fun `given response without results, when searching, then it succeeds with empty list`() =
        runTest {
            respond(body = """{"generationtime_ms":0.5}""")

            val result = repository().search("zzzzzz")

            assertTrue(result.isSuccess)
            assertEquals(emptyList<City>(), result.getOrThrow())
        }

    @Test
    fun `when searching, then name and count query parameters are sent`() = runTest {
        respond()

        repository().search("São Paulo")

        val url = server.takeRequest().requestUrl!!
        assertEquals("/v1/search", url.encodedPath)
        assertEquals("São Paulo", url.queryParameter("name"))
        assertEquals("10", url.queryParameter("count"))
    }

    @Test
    fun `given 4xx, when searching, then failure is InvalidResponse`() = runTest {
        respond(code = 400, body = """{"error":true}""")

        assertTrue(repository().search("x").exceptionOrNull() is DomainError.InvalidResponse)
    }

    @Test
    fun `given 5xx, when searching, then failure is InvalidResponse`() = runTest {
        respond(code = 503)

        assertTrue(repository().search("x").exceptionOrNull() is DomainError.InvalidResponse)
    }

    @Test
    fun `given malformed json, when searching, then failure is InvalidResponse`() = runTest {
        respond(body = "{not json")

        assertTrue(repository().search("x").exceptionOrNull() is DomainError.InvalidResponse)
    }

    @Test
    fun `given server never answers, when searching, then failure is Timeout`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val result = repository(readTimeoutMs = 200).search("x")

        assertTrue(result.exceptionOrNull() is DomainError.Timeout)
    }

    @Test
    fun `given connection dropped, when searching, then failure is Network`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertTrue(repository().search("x").exceptionOrNull() is DomainError.Network)
    }

    @Test
    fun `given cancellation, when searching, then it is rethrown and not wrapped`() = runTest {
        val api = object : GeocodingApi {
            override suspend fun search(name: String, count: Int): GeocodingResponseDto =
                throw CancellationException("cancelled")
        }

        var thrown: CancellationException? = null
        try {
            CityRepositoryImpl(api, dispatcher).search("x")
        } catch (e: CancellationException) {
            thrown = e
        }

        assertNotNull(thrown)
    }
}
