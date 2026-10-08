package com.gaxim.myweather.data.repository

import com.gaxim.myweather.data.remote.ForecastApi
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DomainError
import java.time.LocalDate
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ForecastRepositoryImplTest {

    private val server = MockWebServer()
    private val dispatcher = UnconfinedTestDispatcher()
    private val city = City("Oslo", "Norway", null, 59.91, 10.75)

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.shutdown()

    private fun repository(readTimeoutMs: Long = 5_000) =
        ForecastRepositoryImpl(server.createApi<ForecastApi>(readTimeoutMs), dispatcher)

    private fun respond(code: Int = 200, body: String = "{}") =
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))

    @Test
    fun `given daily data, when fetching, then days are mapped including nulls`() = runTest {
        respond(
            body = """{"daily":{
                "time":["2026-01-15","2026-01-16"],
                "weather_code":[3,null],
                "temperature_2m_max":[10.5,11.0],
                "temperature_2m_min":[1.0,2.0],
                "precipitation_sum":[0.0,null],
                "snowfall_sum":[0.0,0.0],
                "wind_speed_10m_max":[12.0,15.0],
                "wind_gusts_10m_max":[25.0,30.0]}}""",
        )

        val days = repository().getForecast(city).getOrThrow()

        assertEquals(2, days.size)
        assertEquals(LocalDate.of(2026, 1, 15), days[0].date)
        assertEquals(3, days[0].weatherCode)
        assertEquals(10.5, days[0].temperatureMax)
        assertNull(days[1].weatherCode)
        assertNull(days[1].precipitationSum)
    }

    @Test
    fun `when fetching, then coordinates and daily query parameters are sent`() = runTest {
        respond(body = """{"daily":{"time":[]}}""")

        repository().getForecast(city)

        val url = server.takeRequest().requestUrl!!
        assertEquals("/v1/forecast", url.encodedPath)
        assertEquals("59.91", url.queryParameter("latitude"))
        assertEquals("10.75", url.queryParameter("longitude"))
        assertEquals(ForecastApi.DAILY_FIELDS, url.queryParameter("daily"))
        assertEquals("7", url.queryParameter("forecast_days"))
        assertEquals("auto", url.queryParameter("timezone"))
    }

    @Test
    fun `given response without daily block, when fetching, then failure is InvalidResponse`() =
        runTest {
            respond(body = "{}")

            assertTrue(repository().getForecast(city).exceptionOrNull() is DomainError.InvalidResponse)
        }

    @Test
    fun `given 4xx, when fetching, then failure is InvalidResponse`() = runTest {
        respond(code = 400, body = """{"reason":"bad"}""")

        assertTrue(repository().getForecast(city).exceptionOrNull() is DomainError.InvalidResponse)
    }

    @Test
    fun `given 5xx, when fetching, then failure is InvalidResponse`() = runTest {
        respond(code = 500)

        assertTrue(repository().getForecast(city).exceptionOrNull() is DomainError.InvalidResponse)
    }

    @Test
    fun `given malformed json, when fetching, then failure is InvalidResponse`() = runTest {
        respond(body = """{"daily": [""")

        assertTrue(repository().getForecast(city).exceptionOrNull() is DomainError.InvalidResponse)
    }

    @Test
    fun `given wrongly typed field, when fetching, then failure is InvalidResponse`() = runTest {
        respond(body = """{"daily":{"time":["2026-01-15"],"temperature_2m_max":["hot"]}}""")

        assertTrue(repository().getForecast(city).exceptionOrNull() is DomainError.InvalidResponse)
    }

    @Test
    fun `given server never answers, when fetching, then failure is Timeout`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val result = repository(readTimeoutMs = 200).getForecast(city)

        assertTrue(result.exceptionOrNull() is DomainError.Timeout)
    }

    @Test
    fun `given connection dropped, when fetching, then failure is Network`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertTrue(repository().getForecast(city).exceptionOrNull() is DomainError.Network)
    }
}
