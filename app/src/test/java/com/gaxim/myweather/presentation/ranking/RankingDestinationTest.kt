package com.gaxim.myweather.presentation.ranking

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.gaxim.myweather.R
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.presentation.common.ErrorKind
import com.gaxim.myweather.presentation.ui.theme.MyWeatherTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RankingDestinationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val city = City("Zurich", "Switzerland", "Zurich", 47.37, 8.54)
    private val events = Channel<RankingEvent>(Channel.BUFFERED)

    private fun text(id: Int) = composeRule.activity.getString(id)

    private fun setDestination() = composeRule.setContent {
        MyWeatherTheme {
            RankingDestination(
                state = RankingUiState.Success(city, emptyList()),
                events = events.receiveAsFlow(),
                onBack = {},
                onRetry = {},
                onRefresh = {},
            )
        }
    }

    @Test
    fun `given a refresh failed, when the event arrives, then a snackbar shows the error for its kind`() {
        setDestination()

        events.trySend(RankingEvent.RefreshFailed(ErrorKind.TIMEOUT))

        composeRule.onNodeWithText(text(R.string.error_timeout)).assertIsDisplayed()
    }
}
