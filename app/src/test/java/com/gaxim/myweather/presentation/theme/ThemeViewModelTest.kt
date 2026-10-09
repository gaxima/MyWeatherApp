package com.gaxim.myweather.presentation.theme

import app.cash.turbine.test
import com.gaxim.myweather.domain.model.DomainError
import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.domain.repository.ThemeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private class FakeThemeRepository(
        initial: ThemeMode = ThemeMode.System,
        private val failOnSave: Boolean = false,
    ) : ThemeRepository {
        private val mode = MutableStateFlow(initial)
        val saveAttempts = mutableListOf<ThemeMode>()

        override fun observeThemeMode(): Flow<ThemeMode> = mode

        override suspend fun setThemeMode(mode: ThemeMode): Result<Unit> {
            saveAttempts += mode
            if (failOnSave) return Result.failure(DomainError.Unknown())
            this.mode.value = mode
            return Result.success(Unit)
        }
    }

    @Test
    fun `given the store has not answered yet, then state is loading`() = runTest(dispatcher) {
        val repository = object : ThemeRepository {
            override fun observeThemeMode(): Flow<ThemeMode> = MutableSharedFlow()
            override suspend fun setThemeMode(mode: ThemeMode) = Result.success(Unit)
        }

        val viewModel = ThemeViewModel(repository)
        runCurrent()

        assertEquals(ThemeUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `given a saved mode, when loaded, then it is exposed`() = runTest(dispatcher) {
        val viewModel = ThemeViewModel(FakeThemeRepository(ThemeMode.Dark))

        viewModel.uiState.test {
            assertEquals(ThemeUiState.Loading, awaitItem())
            assertEquals(ThemeUiState.Loaded(ThemeMode.Dark), awaitItem())
        }
    }

    @Test
    fun `given system, when toggled three times, then it cycles light dark system and saves each`() =
        runTest(dispatcher) {
            val repository = FakeThemeRepository()
            val viewModel = ThemeViewModel(repository)
            viewModel.uiState.test {
                assertEquals(ThemeUiState.Loading, awaitItem())
                assertEquals(ThemeUiState.Loaded(ThemeMode.System), awaitItem())

                listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System).forEach { expected ->
                    viewModel.onToggleTheme()
                    runCurrent()
                    assertEquals(ThemeUiState.Loaded(expected), awaitItem())
                }
            }
            assertEquals(
                listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System),
                repository.saveAttempts,
            )
        }

    @Test
    fun `given saving fails, when toggled, then state keeps the stored mode and nothing crashes`() =
        runTest(dispatcher) {
            val repository = FakeThemeRepository(ThemeMode.Light, failOnSave = true)
            val viewModel = ThemeViewModel(repository)
            runCurrent()

            viewModel.onToggleTheme()
            runCurrent()

            assertEquals(listOf(ThemeMode.Dark), repository.saveAttempts)
            assertEquals(ThemeUiState.Loaded(ThemeMode.Light), viewModel.uiState.value)
        }

    @Test
    fun `given the mode is not loaded, when toggled, then nothing is saved`() = runTest(dispatcher) {
        val repository = FakeThemeRepository()
        val viewModel = ThemeViewModel(repository)

        viewModel.onToggleTheme()
        runCurrent()

        assertEquals(emptyList<ThemeMode>(), repository.saveAttempts)
    }
}
