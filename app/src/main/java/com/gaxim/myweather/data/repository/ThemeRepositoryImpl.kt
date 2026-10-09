package com.gaxim.myweather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gaxim.myweather.di.IoDispatcher
import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.domain.repository.ThemeRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Persists the theme choice by enum name. A missing or unrecognised value (for example from a
 * renamed enum entry) reads as [ThemeMode.System], and an unreadable store is treated as empty,
 * so a bad preference can never stop the app from launching.
 */
class ThemeRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ThemeRepository {

    override fun observeThemeMode(): Flow<ThemeMode> =
        dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { it[THEME_MODE_KEY].toThemeMode() }
            .distinctUntilChanged()
            .flowOn(dispatcher)

    override suspend fun setThemeMode(mode: ThemeMode) {
        withContext(dispatcher) {
            dataStore.edit { it[THEME_MODE_KEY] = mode.name }
        }
    }

    private fun String?.toThemeMode(): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.System

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
}
