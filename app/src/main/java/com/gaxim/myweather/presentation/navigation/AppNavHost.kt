package com.gaxim.myweather.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gaxim.myweather.R
import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.presentation.ranking.RankingDestination
import com.gaxim.myweather.presentation.ranking.RankingViewModel
import com.gaxim.myweather.presentation.search.SearchEvent
import com.gaxim.myweather.presentation.search.SearchScreen
import com.gaxim.myweather.presentation.search.SearchViewModel
import com.gaxim.myweather.presentation.settings.SettingsDestination
import com.gaxim.myweather.presentation.theme.ThemeEvent
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    themeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    themeEvents: Flow<ThemeEvent>,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = SearchRoute, modifier = modifier) {
        composable<SearchRoute> {
            val viewModel = hiltViewModel<SearchViewModel>()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(viewModel) {
                viewModel.events.collect { event ->
                    when (event) {
                        is SearchEvent.NavigateToRanking ->
                            navController.navigate(RankingRoute.from(event.city))
                    }
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.search_title)) },
                    )
                },
                bottomBar = {
                    AppBottomBar(
                        selected = TopLevelDestination.Search,
                        onSelect = navController::navigateToTopLevel,
                    )
                },
            ) { innerPadding ->
                SearchScreen(
                    state = state,
                    onQueryChange = viewModel::onQueryChanged,
                    onCityClick = viewModel::onCitySelected,
                    onRetry = viewModel::retry,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
        composable<SettingsRoute> {
            SettingsDestination(
                themeMode = themeMode,
                onThemeSelected = onThemeSelected,
                themeEvents = themeEvents,
                onSelectTopLevel = navController::navigateToTopLevel,
            )
        }
        composable<RankingRoute> {
            val viewModel = hiltViewModel<RankingViewModel>()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            RankingDestination(
                state = state,
                events = viewModel.events,
                onBack = navController::popBackStack,
                onRetry = viewModel::retry,
                onRefresh = viewModel::refresh,
            )
        }
    }
}

private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
