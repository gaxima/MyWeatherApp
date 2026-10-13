package com.gaxim.myweather.presentation.search

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.gaxim.myweather.R
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.presentation.common.ErrorKind
import com.gaxim.myweather.presentation.ui.theme.MyWeatherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val zurich = City("Zurich", "Switzerland", "Zurich", 47.37, 8.54)
    private val zurichIllinois = City("Zurich", "United States", "Illinois", 42.15, -88.13)

    private val queries = mutableListOf<String>()
    private val clickedCities = mutableListOf<City>()
    private var retries = 0

    private fun text(id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    private fun setScreen(state: SearchUiState) = composeRule.setContent {
        MyWeatherTheme {
            SearchScreen(
                state = state,
                onQueryChange = { queries += it },
                onCityClick = { clickedCities += it },
                onRetry = { retries++ },
            )
        }
    }

    @Test
    fun `given idle, when shown, then the hint to search is displayed`() {
        setScreen(SearchUiState.Idle())

        composeRule.onNodeWithText(text(R.string.search_idle)).assertIsDisplayed()
    }

    @Test
    fun `given an empty field, when text is typed, then the query is passed on`() {
        setScreen(SearchUiState.Idle())

        composeRule.onNode(hasSetTextAction()).performTextInput("Osl")

        assertEquals(listOf("Osl"), queries)
    }

    @Test
    fun `given a query, when clear is tapped, then an empty query is passed on`() {
        setScreen(SearchUiState.Success("zurich", listOf(zurich)))

        composeRule.onNodeWithText(text(R.string.search_clear)).performClick()

        assertEquals(listOf(""), queries)
    }

    @Test
    fun `given results, when a city is tapped, then that city is passed on`() {
        setScreen(SearchUiState.Success("zurich", listOf(zurich, zurichIllinois)))

        composeRule.onNodeWithText("Zurich, Illinois, United States").performClick()

        assertEquals(listOf(zurichIllinois), clickedCities)
    }

    @Test
    fun `given no match, when shown, then the empty message names the query`() {
        setScreen(SearchUiState.Empty("qwerty"))

        composeRule.onNodeWithText(text(R.string.search_empty, "qwerty")).assertIsDisplayed()
    }

    @Test
    fun `given an error, when shown, then the message for its kind is displayed`() {
        setScreen(SearchUiState.Error("zurich", ErrorKind.TIMEOUT))

        composeRule.onNodeWithText(text(R.string.error_timeout)).assertIsDisplayed()
    }

    @Test
    fun `given an error, when try again is tapped, then retry is requested once`() {
        setScreen(SearchUiState.Error("zurich", ErrorKind.NETWORK))

        composeRule.onNodeWithText(text(R.string.search_retry)).performClick()

        assertEquals(1, retries)
    }
}
