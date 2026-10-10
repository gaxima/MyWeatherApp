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
    fun `given a loaded mode, when another mode is selected, then it is saved and exposed`() =
        runTest(dispatcher) {
            val repository = FakeThemeRepository(ThemeMode.Light)
            val viewModel = ThemeViewModel(repository)
            viewModel.uiState.test {
                assertEquals(ThemeUiState.Loading, awaitItem())
                assertEquals(ThemeUiState.Loaded(ThemeMode.Light), awaitItem())

                viewModel.onThemeSelected(ThemeMode.Dark)
                runCurrent()

                assertEquals(ThemeUiState.Loaded(ThemeMode.Dark), awaitItem())
            }
            assertEquals(listOf(ThemeMode.Dark), repository.saveAttempts)
        }

    @Test
    fun `given the selected mode, when it is selected again, then it is saved and state is unchanged`() =
        runTest(dispatcher) {
            val repository = FakeThemeRepository(ThemeMode.Dark)
            val viewModel = ThemeViewModel(repository)
            runCurrent()

            viewModel.onThemeSelected(ThemeMode.Dark)
            runCurrent()

            assertEquals(listOf(ThemeMode.Dark), repository.saveAttempts)
            assertEquals(ThemeUiState.Loaded(ThemeMode.Dark), viewModel.uiState.value)
        }

    @Test
    fun `given saving fails, when a mode is selected, then state keeps the stored mode`() =
        runTest(dispatcher) {
            val repository = FakeThemeRepository(ThemeMode.Light, failOnSave = true)
            val viewModel = ThemeViewModel(repository)
            runCurrent()

            viewModel.onThemeSelected(ThemeMode.Dark)
            runCurrent()

            assertEquals(listOf(ThemeMode.Dark), repository.saveAttempts)
            assertEquals(ThemeUiState.Loaded(ThemeMode.Light), viewModel.uiState.value)
        }

    @Test
    fun `given the mode is not loaded, when a mode is selected, then it is still saved`() =
        runTest(dispatcher) {
            val saved = mutableListOf<ThemeMode>()
            val repository = object : ThemeRepository {
                override fun observeThemeMode(): Flow<ThemeMode> = MutableSharedFlow()
                override suspend fun setThemeMode(mode: ThemeMode): Result<Unit> {
                    saved += mode
                    return Result.success(Unit)
                }
            }
            val viewModel = ThemeViewModel(repository)

            viewModel.onThemeSelected(ThemeMode.Dark)
            runCurrent()

            assertEquals(listOf(ThemeMode.Dark), saved)
        }
}
