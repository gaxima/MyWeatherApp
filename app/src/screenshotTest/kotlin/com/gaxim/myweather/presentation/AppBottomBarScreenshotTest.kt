package com.gaxim.myweather.presentation

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.gaxim.myweather.presentation.navigation.AppBottomBar
import com.gaxim.myweather.presentation.navigation.TopLevelDestination
import com.gaxim.myweather.presentation.ui.theme.MyWeatherTheme

@Composable
private fun BottomBarScreenshot(selected: TopLevelDestination) {
    MyWeatherTheme {
        AppBottomBar(selected = selected, onSelect = {})
    }
}

@PreviewTest
@LightAndDarkPreviews
@Composable
fun BottomBarSearchSelected() = BottomBarScreenshot(TopLevelDestination.Search)

@PreviewTest
@LightAndDarkPreviews
@Composable
fun BottomBarSettingsSelected() = BottomBarScreenshot(TopLevelDestination.Settings)
