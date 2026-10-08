package com.gaxim.myweather.presentation.ranking

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DailyForecast
import com.gaxim.myweather.domain.model.DomainError
import com.gaxim.myweather.domain.repository.ForecastRepository
import com.gaxim.myweather.domain.scoring.ActivityRanker
import com.gaxim.myweather.domain.scoring.forecast
import com.gaxim.myweather.domain.usecase.FakeForecastRepository
import com.gaxim.myweather.domain.usecase.GetActivityRankingUseCase
import com.gaxim.myweather.domain.usecase.city
import com.gaxim.myweather.presentation.common.ErrorKind
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RankingViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeForecastRepository()
    private val oslo = city()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun handleFor(city: City?) = SavedStateHandle(
        if (city == null) emptyMap() else mapOf(
            "name" to city.name,
            "country" to city.country,
            "region" to city.region,
            "latitude" to city.latitude,
            "longitude" to city.longitude,
        ),
    )

    private fun viewModel(
        city: City? = oslo,
        repository: ForecastRepository = this.repository,
    ) = RankingViewModel(handleFor(city), GetActivityRankingUseCase(repository, ActivityRanker()))

    @Test
    fun `given a city argument, when created, then state starts loading for that city`() =
        runTest(dispatcher) {
            assertEquals(RankingUiState.Loading(oslo), viewModel().uiState.value)
        }

    @Test
    fun `given a forecast, when loaded, then state is success with one ranking per day`() =
        runTest(dispatcher) {
            val days: List<DailyForecast> = listOf(forecast(), forecast())
            repository.result = Result.success(days)
            val viewModel = viewModel()

            runCurrent()

            val state = viewModel.uiState.value as RankingUiState.Success
            assertEquals(oslo, state.city)
            assertEquals(2, state.days.size)
            assertEquals(listOf(oslo), repository.requestedCities)
        }

    @Test
    fun `given the repository fails, when loaded, then state is error with the mapped kind`() =
        runTest(dispatcher) {
            repository.result = Result.failure(DomainError.Network())
            val viewModel = viewModel()

            runCurrent()

            assertEquals(RankingUiState.Error(oslo, ErrorKind.NETWORK), viewModel.uiState.value)
        }

    @Test
    fun `given an error, when retried, then state goes loading then success`() = runTest(dispatcher) {
        repository.result = Result.failure(DomainError.Timeout())
        val viewModel = viewModel()
        runCurrent()

        viewModel.uiState.test {
            assertEquals(RankingUiState.Error(oslo, ErrorKind.TIMEOUT), awaitItem())

            repository.result = Result.success(listOf(forecast()))
            viewModel.retry()
            runCurrent()

            assertEquals(RankingUiState.Loading(oslo), awaitItem())
            assertTrue(awaitItem() is RankingUiState.Success)
        }
        assertEquals(listOf(oslo, oslo), repository.requestedCities)
    }

    @Test
    fun `given a slow request, when loading, then state stays loading until it answers`() =
        runTest(dispatcher) {
            val gate = CompletableDeferred<Result<List<DailyForecast>>>()
            val viewModel = viewModel(repository = GatedForecastRepository(gate))

            runCurrent()
            assertEquals(RankingUiState.Loading(oslo), viewModel.uiState.value)

            gate.complete(Result.success(listOf(forecast())))
            runCurrent()
            assertTrue(viewModel.uiState.value is RankingUiState.Success)
        }

    @Test
    fun `given missing city arguments, when created, then state is error and nothing is fetched`() =
        runTest(dispatcher) {
            val viewModel = viewModel(city = null)

            runCurrent()

            assertEquals(RankingUiState.Error(null, ErrorKind.UNKNOWN), viewModel.uiState.value)
            assertEquals(emptyList<City>(), repository.requestedCities)
        }

    @Test
    fun `given optional fields are absent, when created, then the city is rebuilt without them`() =
        runTest(dispatcher) {
            val bare = city(country = null, region = null)

            val viewModel = viewModel(city = bare)

            assertEquals(bare, viewModel.uiState.value.city)
        }

    private class GatedForecastRepository(
        private val gate: CompletableDeferred<Result<List<DailyForecast>>>,
    ) : ForecastRepository {
        override suspend fun getForecast(city: City): Result<List<DailyForecast>> = gate.await()
    }
}
