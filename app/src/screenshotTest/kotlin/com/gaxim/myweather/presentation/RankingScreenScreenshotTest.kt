package com.gaxim.myweather.presentation

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.ActivityScore
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DayRanking
import com.gaxim.myweather.domain.model.ReasonKey
import com.gaxim.myweather.domain.model.WeatherFactor
import com.gaxim.myweather.presentation.common.ErrorKind
import com.gaxim.myweather.presentation.ranking.RankingScreen
import com.gaxim.myweather.presentation.ranking.RankingUiState
import com.gaxim.myweather.presentation.ui.theme.MyWeatherTheme
import java.time.LocalDate

private val city = City("Zurich", "Switzerland", "Zurich", 47.37, 8.54)

private val day = DayRanking(
    date = LocalDate.of(2026, 1, 15),
    scores = listOf(
        ActivityScore(Activity.INDOOR_SIGHTSEEING, 80, ReasonKey.INDOOR_ANY_WEATHER, emptySet()),
        ActivityScore(Activity.OUTDOOR_SIGHTSEEING, 55, ReasonKey.OUTDOOR_TOO_COLD, emptySet()),
        ActivityScore(Activity.SKIING, 20, ReasonKey.SKI_TOO_WARM, setOf(WeatherFactor.SNOWFALL)),
        ActivityScore(Activity.SURFING, 5, ReasonKey.SURF_TOO_CALM, emptySet()),
    ),
)

private val days = listOf(day, day.copy(date = day.date.plusDays(1)))

@Composable
private fun RankingScreenshot(state: RankingUiState) {
    MyWeatherTheme {
        RankingScreen(state = state, onBack = {}, onRetry = {}, onRefresh = {})
    }
}

@PreviewTest
@LightAndDarkPreviews
@Composable
fun RankingLoading() = RankingScreenshot(RankingUiState.Loading(city))

@PreviewTest
@LightAndDarkPreviews
@Composable
fun RankingSuccess() = RankingScreenshot(RankingUiState.Success(city, days))

@PreviewTest
@LightAndDarkPreviews
@Composable
fun RankingRefreshing() = RankingScreenshot(RankingUiState.Refreshing(city, days))

@PreviewTest
@LightAndDarkPreviews
@Composable
fun RankingError() = RankingScreenshot(RankingUiState.Error(city, ErrorKind.TIMEOUT))
