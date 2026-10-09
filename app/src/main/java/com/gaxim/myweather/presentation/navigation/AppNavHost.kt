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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gaxim.myweather.R
import com.gaxim.myweather.presentation.ranking.RankingScreen
import com.gaxim.myweather.presentation.ranking.RankingViewModel
import com.gaxim.myweather.presentation.search.SearchEvent
import com.gaxim.myweather.presentation.search.SearchScreen
import com.gaxim.myweather.presentation.search.SearchViewModel

/** Wires the two screens to their ViewModels and to each other. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
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
                    TopAppBar(title = { Text(stringResource(R.string.search_title)) })
                },
            ) { innerPadding ->
                SearchScreen(
                    state = state,
                    onQueryChange = viewModel::onQueryChanged,
                    onCityClick = viewModel::onCitySelected,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
        composable<RankingRoute> {
            val viewModel = hiltViewModel<RankingViewModel>()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            RankingScreen(
                state = state,
                onBack = navController::popBackStack,
                onRetry = viewModel::retry,
            )
        }
    }
}
