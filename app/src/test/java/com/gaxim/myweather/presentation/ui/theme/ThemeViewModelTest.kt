package com.gaxim.myweather.presentation.ui.theme

import app.cash.turbine.test
import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.domain.repository.ThemeRepository
import com.gaxim.myweather.domain.usecase.ObserveThemeModeUseCase
import com.gaxim.myweather.domain.usecase.SetThemeModeUseCase
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private class FakeThemeRepository(initial: ThemeMode = ThemeMode.System) : ThemeRepository {
        val mode = MutableStateFlow(initial)
        val saved = mutableListOf<ThemeMode>()

        override fun observeThemeMode(): Flow<ThemeMode> = mode

        override suspend fun setThemeMode(mode: ThemeMode) {
            saved += mode
            this.mode.value = mode
        }
    }

    private fun viewModel(repository: ThemeRepository) =
        ThemeViewModel(ObserveThemeModeUseCase(repository), SetThemeModeUseCase(repository))

    @Test
    fun `given the store has not answered yet, then mode is null`() = runTest(dispatcher) {
        val repository = object : ThemeRepository {
            override fun observeThemeMode(): Flow<ThemeMode> = MutableSharedFlow()
            override suspend fun setThemeMode(mode: ThemeMode) = Unit
        }

        val viewModel = viewModel(repository)
        runCurrent()

        assertNull(viewModel.themeMode.value)
    }

    @Test
    fun `given a saved mode, when loaded, then it is exposed`() = runTest(dispatcher) {
        val viewModel = viewModel(FakeThemeRepository(ThemeMode.Dark))

        viewModel.themeMode.test {
            assertNull(awaitItem())
            assertEquals(ThemeMode.Dark, awaitItem())
        }
    }

    @Test
    fun `given system, when toggled three times, then it cycles light dark system and saves each`() =
        runTest(dispatcher) {
            val repository = FakeThemeRepository()
            val viewModel = viewModel(repository)
            viewModel.themeMode.test {
                assertNull(awaitItem())
                assertEquals(ThemeMode.System, awaitItem())

                listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System).forEach { expected ->
                    viewModel.onToggleTheme()
                    runCurrent()
                    assertEquals(expected, awaitItem())
                }
            }
            assertEquals(listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System), repository.saved)
        }

    @Test
    fun `given the mode is not loaded, when toggled, then nothing is saved`() = runTest(dispatcher) {
        val repository = FakeThemeRepository()
        val viewModel = viewModel(repository)

        viewModel.onToggleTheme()
        runCurrent()

        assertEquals(emptyList<ThemeMode>(), repository.saved)
    }
}
