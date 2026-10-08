package com.gaxim.myweather.domain.usecase

import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DailyForecast
import com.gaxim.myweather.domain.model.DomainError
import com.gaxim.myweather.domain.repository.ForecastRepository
import com.gaxim.myweather.domain.scoring.ActivityRanker
import com.gaxim.myweather.domain.scoring.forecast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.LocalDate

class GetActivityRankingUseCaseTest {

    private val repository = FakeForecastRepository()
    private val useCase = GetActivityRankingUseCase(repository, ActivityRanker())
    private val oslo = city()

    @Test
    fun `given a forecast, when ranking, then requests it for the selected city`() = runTest {
        useCase(oslo)

        assertEquals(listOf(oslo), repository.requestedCities)
    }

    @Test
    fun `given seven days, when ranking, then each day lists all four activities`() = runTest {
        val start = LocalDate.of(2026, 1, 15)
        repository.result = Result.success((0L..6L).map { forecast(date = start.plusDays(it)) })

        val rankings = useCase(oslo).getOrThrow()

        assertEquals(7, rankings.size)
        rankings.forEach { day ->
            assertEquals(Activity.entries.toSet(), day.scores.map { it.activity }.toSet())
            assertEquals(day.scores.map { it.score }.sortedDescending(), day.scores.map { it.score })
        }
    }

    @Test
    fun `given days out of calendar order, when ranking, then repository order is kept`() = runTest {
        val dates = listOf(LocalDate.of(2026, 1, 16), LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 17))
        repository.result = Result.success(dates.map { forecast(date = it) })

        assertEquals(dates, useCase(oslo).getOrThrow().map { it.date })
    }

    @Test
    fun `given rain, when ranking, then indoor beats outdoor`() = runTest {
        repository.result = Result.success(listOf(forecast(weatherCode = 63, precipitationSum = 12.0)))

        val order = useCase(oslo).getOrThrow().single().scores.map { it.activity }

        assertTrue(order.indexOf(Activity.INDOOR_SIGHTSEEING) < order.indexOf(Activity.OUTDOOR_SIGHTSEEING))
    }

    @Test
    fun `given missing measurements, when ranking, then still ranks every activity`() = runTest {
        repository.result = Result.success(
            listOf(
                forecast(
                    weatherCode = null,
                    temperatureMax = null,
                    temperatureMin = null,
                    precipitationSum = null,
                    snowfallSum = null,
                    windSpeedMax = null,
                    windGustsMax = null,
                ),
            ),
        )

        assertEquals(Activity.entries.size, useCase(oslo).getOrThrow().single().scores.size)
    }

    @Test
    fun `given empty forecast, when ranking, then returns success with empty list`() = runTest {
        repository.result = Result.success(emptyList())

        val result = useCase(oslo)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `given repository failure, when ranking, then the domain error passes through`() = runTest {
        val error = DomainError.Network()
        repository.result = Result.failure(error)

        assertSame(error, useCase(oslo).exceptionOrNull())
    }

    @Test
    fun `given cancellation in the repository, when ranking, then it propagates`() = runTest {
        val cancelling = object : ForecastRepository {
            override suspend fun getForecast(city: City): Result<List<DailyForecast>> =
                throw CancellationException("cancelled")
        }

        try {
            GetActivityRankingUseCase(cancelling, ActivityRanker())(oslo)
            fail("expected CancellationException")
        } catch (_: CancellationException) {
            // expected
        }
    }
}
