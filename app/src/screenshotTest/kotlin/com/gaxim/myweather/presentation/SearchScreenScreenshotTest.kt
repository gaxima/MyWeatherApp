package com.gaxim.myweather.presentation

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.presentation.common.ErrorKind
import com.gaxim.myweather.presentation.search.SearchScreen
import com.gaxim.myweather.presentation.search.SearchUiState
import com.gaxim.myweather.presentation.ui.theme.MyWeatherTheme

private val cities = listOf(
    City("Zurich", "Switzerland", "Zurich", 47.37, 8.54),
    City("Zurich", "United States", "Illinois", 42.15, -88.13),
)

@Composable
private fun SearchScreenshot(state: SearchUiState) {
    MyWeatherTheme {
        SearchScreen(state = state, onQueryChange = {}, onCityClick = {})
    }
}

@PreviewTest
@LightAndDarkPreviews
@Composable
fun SearchIdle() = SearchScreenshot(SearchUiState.Idle())

@PreviewTest
@LightAndDarkPreviews
@Composable
fun SearchLoading() = SearchScreenshot(SearchUiState.Loading("zur"))

@PreviewTest
@LightAndDarkPreviews
@Composable
fun SearchSuccess() = SearchScreenshot(SearchUiState.Success("zurich", cities))

@PreviewTest
@LightAndDarkPreviews
@Composable
fun SearchEmpty() = SearchScreenshot(SearchUiState.Empty("qwertyuiop"))

@PreviewTest
@LightAndDarkPreviews
@Composable
fun SearchError() = SearchScreenshot(SearchUiState.Error("zurich", ErrorKind.NETWORK))
