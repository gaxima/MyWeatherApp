package com.gaxim.myweather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import com.gaxim.myweather.domain.model.ThemeMode
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeRepositoryImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dispatcher = UnconfinedTestDispatcher()

    private fun dataStore(file: File = temporaryFolder.newFile("settings.preferences_pb")): DataStore<Preferences> {
        file.delete() // DataStore creates the file itself; an existing empty file is not a valid store.
        return PreferenceDataStoreFactory.create(
            scope = CoroutineScope(dispatcher + SupervisorJob()),
            produceFile = { file },
        )
    }

    private fun repository(dataStore: DataStore<Preferences> = dataStore()) =
        ThemeRepositoryImpl(dataStore, dispatcher)

    @Test
    fun `given nothing saved, when observing, then mode is system`() = runTest(dispatcher) {
        assertEquals(ThemeMode.System, repository().observeThemeMode().first())
    }

    @Test
    fun `given a saved mode, when observing from a new repository, then it survives`() =
        runTest(dispatcher) {
            val store = dataStore()
            repository(store).setThemeMode(ThemeMode.Dark)

            assertEquals(ThemeMode.Dark, repository(store).observeThemeMode().first())
        }

    @Test
    fun `given an observer, when the mode changes, then each new mode is emitted`() =
        runTest(dispatcher) {
            val repository = repository()

            repository.observeThemeMode().test {
                assertEquals(ThemeMode.System, awaitItem())
                repository.setThemeMode(ThemeMode.Light)
                assertEquals(ThemeMode.Light, awaitItem())
                repository.setThemeMode(ThemeMode.Dark)
                assertEquals(ThemeMode.Dark, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given an unrecognised stored value, when observing, then mode falls back to system`() =
        runTest(dispatcher) {
            val store = dataStore()
            store.edit { it[stringPreferencesKey("theme_mode")] = "Sepia" }

            assertEquals(ThemeMode.System, repository(store).observeThemeMode().first())
        }
}
