package com.gaxim.myweather.presentation.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gaxim.myweather.R
import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.presentation.navigation.TopLevelDestination
import com.gaxim.myweather.presentation.theme.ThemeEvent
import com.gaxim.myweather.presentation.ui.theme.MyWeatherTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsDestinationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val events = Channel<ThemeEvent>(Channel.BUFFERED)
    private val topLevelSelections = mutableListOf<TopLevelDestination>()

    private fun text(id: Int) = composeRule.activity.getString(id)

    private fun setDestination() = composeRule.setContent {
        MyWeatherTheme {
            SettingsDestination(
                themeMode = ThemeMode.Light,
                onThemeSelected = {},
                themeEvents = events.receiveAsFlow(),
                onSelectTopLevel = { topLevelSelections += it },
            )
        }
    }

    @Test
    fun `given no event, when shown, then no error is displayed`() {
        setDestination()

        composeRule.onNodeWithText(text(R.string.error_theme_save)).assertDoesNotExist()
    }

    @Test
    fun `given saving failed, when the event arrives, then a snackbar explains it`() {
        setDestination()

        events.trySend(ThemeEvent.SaveFailed)

        composeRule.onNodeWithText(text(R.string.error_theme_save)).assertIsDisplayed()
    }

    @Test
    fun `given settings, when search is tapped in the bottom bar, then search is selected`() {
        setDestination()

        composeRule.onNodeWithText(text(R.string.nav_search)).performClick()

        assertEquals(listOf(TopLevelDestination.Search), topLevelSelections)
    }
}
