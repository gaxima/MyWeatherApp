package com.gaxim.myweather.presentation

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.presentation.settings.SettingsScreen
import com.gaxim.myweather.presentation.ui.theme.MyWeatherTheme

@Composable
private fun SettingsScreenshot(mode: ThemeMode) {
    MyWeatherTheme {
        // In the app the Scaffold paints the background; without it dark text sits on white.
        Surface {
            SettingsScreen(themeMode = mode, onThemeSelected = {})
        }
    }
}

@PreviewTest
@LightAndDarkPreviews
@Composable
fun SettingsThemeSystem() = SettingsScreenshot(ThemeMode.System)

@PreviewTest
@LightAndDarkPreviews
@Composable
fun SettingsThemeDark() = SettingsScreenshot(ThemeMode.Dark)
