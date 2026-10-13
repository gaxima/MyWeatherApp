package com.gaxim.myweather.presentation.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import com.gaxim.myweather.R
import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.presentation.navigation.AppBottomBar
import com.gaxim.myweather.presentation.navigation.TopLevelDestination
import com.gaxim.myweather.presentation.theme.ThemeEvent
import kotlinx.coroutines.flow.Flow

/** The Settings destination: screen, top and bottom bars, and the snackbar for [themeEvents]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDestination(
    themeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    themeEvents: Flow<ThemeEvent>,
    onSelectTopLevel: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(themeEvents) {
        themeEvents.collect { event ->
            when (event) {
                ThemeEvent.SaveFailed ->
                    snackbarHostState.showSnackbar(resources.getString(R.string.error_theme_save))
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        },
        bottomBar = {
            AppBottomBar(selected = TopLevelDestination.Settings, onSelect = onSelectTopLevel)
        },
    ) { innerPadding ->
        SettingsScreen(
            themeMode = themeMode,
            onThemeSelected = onThemeSelected,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
