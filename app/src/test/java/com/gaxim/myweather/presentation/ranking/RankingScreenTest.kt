package com.gaxim.myweather.presentation.ranking

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.gaxim.myweather.R
import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.ActivityScore
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DayRanking
import com.gaxim.myweather.domain.model.ReasonKey
import com.gaxim.myweather.domain.model.WeatherFactor
import com.gaxim.myweather.presentation.common.ErrorKind
import com.gaxim.myweather.presentation.ui.theme.MyWeatherTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RankingScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val city = City("Zurich", "Switzerland", "Zurich", 47.37, 8.54)

    private val day = DayRanking(
        date = LocalDate.of(2026, 1, 15),
        scores = listOf(
            ActivityScore(Activity.INDOOR_SIGHTSEEING, 80, ReasonKey.INDOOR_ANY_WEATHER, emptySet()),
            ActivityScore(Activity.SKIING, 20, ReasonKey.SKI_TOO_WARM, setOf(WeatherFactor.SNOWFALL)),
            ActivityScore(Activity.SURFING, 5, ReasonKey.SURF_TOO_CALM, emptySet()),
        ),
    )

    private var backs = 0
    private var retries = 0
    private var refreshes = 0

    private fun text(id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    private fun setScreen(state: RankingUiState) = composeRule.setContent {
        MyWeatherTheme {
            RankingScreen(
                state = state,
                onBack = { backs++ },
                onRetry = { retries++ },
                onRefresh = { refreshes++ },
            )
        }
    }

    private fun topOf(label: String) =
        composeRule.onNodeWithText(label).fetchSemanticsNode().boundsInRoot.top

    @Test
    fun `given a ranking, when shown, then activities appear in the order of the state`() {
        setScreen(RankingUiState.Success(city, listOf(day)))

        val indoor = topOf(text(R.string.activity_indoor_sightseeing))
        val skiing = topOf(text(R.string.activity_skiing))
        val surfing = topOf(text(R.string.activity_surfing))

        assertTrue(indoor < skiing && skiing < surfing)
    }

    @Test
    fun `given a ranking, when shown, then score, reason and missing factors are displayed`() {
        setScreen(RankingUiState.Success(city, listOf(day)))

        composeRule.onNodeWithText(text(R.string.ranking_score, 80)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.reason_ski_too_warm)).assertIsDisplayed()
        val snowfall = text(R.string.factor_snowfall)
        composeRule.onNodeWithText(text(R.string.ranking_ignored_factors, snowfall)).assertIsDisplayed()
    }

    @Test
    fun `given a city, when shown, then the title names it`() {
        setScreen(RankingUiState.Success(city, listOf(day)))

        composeRule.onNodeWithText(text(R.string.ranking_title, "Zurich, Switzerland")).assertIsDisplayed()
    }

    @Test
    fun `given any state, when back is tapped, then back is requested once`() {
        setScreen(RankingUiState.Success(city, listOf(day)))

        composeRule.onNodeWithContentDescription(text(R.string.ranking_back)).performClick()

        assertEquals(1, backs)
    }

    @Test
    fun `given loading, when shown, then a progress indicator replaces the ranking`() {
        setScreen(RankingUiState.Loading(city))

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.activity_skiing)).assertDoesNotExist()
    }

    @Test
    fun `given an error for a city, when try again is tapped, then retry is requested once`() {
        setScreen(RankingUiState.Error(city, ErrorKind.SERVER))

        composeRule.onNodeWithText(text(R.string.error_server)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.ranking_retry)).performClick()

        assertEquals(1, retries)
    }

    @Test
    fun `given an error without a city, when shown, then there is nothing to retry`() {
        setScreen(RankingUiState.Error(null, ErrorKind.UNKNOWN))

        composeRule.onNodeWithText(text(R.string.ranking_retry)).assertDoesNotExist()
    }

    @Test
    fun `given a refresh in flight, when shown, then scores are replaced by a refreshing indicator`() {
        setScreen(RankingUiState.Refreshing(city, listOf(day)))

        composeRule.onNodeWithText(text(R.string.ranking_score, 80)).assertDoesNotExist()
        val indicators = composeRule
            .onAllNodes(hasContentDescription(text(R.string.ranking_refreshing)))
            .fetchSemanticsNodes()
        assertEquals(day.scores.size, indicators.size)
    }

    @Test
    fun `given a ranking, when pulled down, then a refresh is requested`() {
        setScreen(RankingUiState.Success(city, listOf(day)))

        // Swipe the whole list: a single row is too short to pass the refresh threshold.
        composeRule.onNode(hasScrollAction()).performTouchInput { swipeDown() }

        assertEquals(1, refreshes)
    }
}
