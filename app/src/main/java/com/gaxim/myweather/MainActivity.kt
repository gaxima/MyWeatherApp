package com.gaxim.myweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaxim.myweather.presentation.navigation.AppNavHost
import com.gaxim.myweather.presentation.theme.ThemeUiState
import com.gaxim.myweather.presentation.theme.ThemeViewModel
import com.gaxim.myweather.presentation.ui.theme.MyWeatherTheme
import com.gaxim.myweather.presentation.ui.theme.isDark
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState by themeViewModel.uiState.collectAsStateWithLifecycle()
            when (val state = themeState) {
                ThemeUiState.Loading -> Unit
                is ThemeUiState.Loaded -> MyWeatherTheme(darkTheme = state.mode.isDark()) {
                    AppNavHost(
                        themeMode = state.mode,
                        onToggleTheme = themeViewModel::onToggleTheme,
                    )
                }
            }
        }
    }
}
