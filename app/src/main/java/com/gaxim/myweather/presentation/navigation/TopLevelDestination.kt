package com.gaxim.myweather.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.gaxim.myweather.R

/** Destinations reachable from the bottom bar. */
enum class TopLevelDestination(
    val route: Any,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Search(SearchRoute, R.string.nav_search, Icons.Default.Search),
    Settings(SettingsRoute, R.string.nav_settings, Icons.Default.Settings),
}
