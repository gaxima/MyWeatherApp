package com.gaxim.myweather.presentation.search

import app.cash.turbine.test
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DomainError
import com.gaxim.myweather.domain.repository.CityRepository
import com.gaxim.myweather.domain.usecase.FakeCityRepository
import com.gaxim.myweather.domain.usecase.SearchCitiesUseCase
import com.gaxim.myweather.domain.usecase.city
import com.gaxim.myweather.presentation.common.ErrorKind
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeCityRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repository: CityRepository = this.repository) =
        SearchViewModel(SearchCitiesUseCase(repository))

    private suspend fun TestScope.pastDebounce() {
        advanceTimeBy(SearchViewModel.DEBOUNCE_MILLIS + 1)
        runCurrent()
    }

    @Test
    fun `given a new view model, then state is idle with an empty query`() = runTest(dispatcher) {
        assertEquals(SearchUiState.Idle(""), viewModel().uiState.value)
    }

    @Test
    fun `given a query, when typed, then the field text updates before the debounce elapses`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.onQueryChanged("Osl")

            assertEquals("Osl", viewModel.uiState.value.query)
            assertEquals(emptyList<String>(), repository.queries)
        }

    @Test
    fun `given cities match, when the debounce elapses, then state goes loading then success`() =
        runTest(dispatcher) {
            val oslo = city()
            val gate = CompletableDeferred<Result<List<City>>>()
            val viewModel = viewModel(GatedCityRepository(gate))

            viewModel.uiState.test {
                assertEquals(SearchUiState.Idle(""), awaitItem())

                viewModel.onQueryChanged("Oslo")
                assertEquals(SearchUiState.Idle("Oslo"), awaitItem())

                pastDebounce()
                assertEquals(SearchUiState.Loading("Oslo"), awaitItem())

                gate.complete(Result.success(listOf(oslo)))
                runCurrent()
                assertEquals(SearchUiState.Success("Oslo", listOf(oslo)), awaitItem())
            }
        }

    @Test
    fun `given no city matches, when searching, then state is empty`() = runTest(dispatcher) {
        repository.result = Result.success(emptyList())
        val viewModel = viewModel()

        viewModel.onQueryChanged("Zzzz")
        pastDebounce()

        assertEquals(SearchUiState.Empty("Zzzz"), viewModel.uiState.value)
    }

    @Test
    fun `given the repository fails, when searching, then state is error with the mapped kind`() =
        runTest(dispatcher) {
            repository.result = Result.failure(DomainError.Timeout())
            val viewModel = viewModel()

            viewModel.onQueryChanged("Oslo")
            pastDebounce()

            assertEquals(SearchUiState.Error("Oslo", ErrorKind.TIMEOUT), viewModel.uiState.value)
        }

    @Test
    fun `given rapid typing, when the user pauses, then only the final query is searched`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.onQueryChanged("O")
            advanceTimeBy(100)
            viewModel.onQueryChanged("Os")
            advanceTimeBy(100)
            viewModel.onQueryChanged("Osl")
            pastDebounce()

            assertEquals(listOf("Osl"), repository.queries)
        }

    @Test
    fun `given a slow search in flight, when a newer query is typed, then the stale result is dropped`() =
        runTest(dispatcher) {
            val slow = CompletableDeferred<Result<List<City>>>()
            val viewModel = viewModel(GatedCityRepository(slow))

            viewModel.onQueryChanged("Old")
            pastDebounce()
            viewModel.onQueryChanged("New")
            slow.complete(Result.success(listOf(city(name = "Old Town"))))
            runCurrent()

            // The stale success must not replace the state of the newer query.
            assertEquals(SearchUiState.Loading("New"), viewModel.uiState.value)
        }

    @Test
    fun `given results are shown, when the query is cleared, then state returns to idle`() =
        runTest(dispatcher) {
            repository.result = Result.success(listOf(city()))
            val viewModel = viewModel()
            viewModel.onQueryChanged("Oslo")
            pastDebounce()

            viewModel.onQueryChanged("")

            assertEquals(SearchUiState.Idle(""), viewModel.uiState.value)
        }

    @Test
    fun `given a blank query, when typed, then no search is made`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onQueryChanged("   ")
        pastDebounce()

        assertEquals(emptyList<String>(), repository.queries)
        assertEquals(SearchUiState.Idle("   "), viewModel.uiState.value)
    }

    @Test
    fun `given a city, when selected, then a navigate event is emitted once`() = runTest(dispatcher) {
        val viewModel = viewModel()
        val oslo = city()

        viewModel.events.test {
            viewModel.onCitySelected(oslo)
            runCurrent()

            assertEquals(SearchEvent.NavigateToRanking(oslo), awaitItem())
            expectNoEvents()
        }
    }

    private class GatedCityRepository(
        private val gate: CompletableDeferred<Result<List<City>>>,
    ) : CityRepository {
        override suspend fun search(query: String): Result<List<City>> = gate.await()
    }
}
