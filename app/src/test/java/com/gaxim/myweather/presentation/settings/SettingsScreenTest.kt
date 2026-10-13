package com.gaxim.myweather.presentation.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gaxim.myweather.R
import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.presentation.ui.theme.MyWeatherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun text(id: Int) = composeRule.activity.getString(id)

    private fun setScreen(mode: ThemeMode, onThemeSelected: (ThemeMode) -> Unit = {}) =
        composeRule.setContent {
            MyWeatherTheme { SettingsScreen(themeMode = mode, onThemeSelected = onThemeSelected) }
        }

    @Test
    fun `given a stored mode, when shown, then that mode is selected`() {
        setScreen(ThemeMode.Light)

        composeRule.onNodeWithText(text(R.string.theme_light)).assertIsSelected()
    }

    @Test
    fun `given a mode, when another one is tapped, then it is passed to the callback`() {
        val selected = mutableListOf<ThemeMode>()
        setScreen(ThemeMode.System) { selected += it }

        composeRule.onNodeWithText(text(R.string.theme_dark)).performClick()

        assertEquals(listOf(ThemeMode.Dark), selected)
    }
}
