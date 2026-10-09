package com.gaxim.myweather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gaxim.myweather.di.IoDispatcher
import com.gaxim.myweather.domain.model.DomainError
import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.domain.repository.ThemeRepository
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

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

    // Not safeCall: its IOException -> DomainError.Network mapping would mislabel a disk failure.
    override suspend fun setThemeMode(mode: ThemeMode): Result<Unit> = withContext(dispatcher) {
        try {
            dataStore.edit { it[THEME_MODE_KEY] = mode.name }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(DomainError.Unknown(e))
        }
    }

    private fun String?.toThemeMode(): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.System

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
}
