package com.gaxim.myweather.presentation.ranking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.gaxim.myweather.R
import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.ActivityScore
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.DayRanking
import com.gaxim.myweather.domain.model.ReasonKey
import com.gaxim.myweather.domain.model.WeatherFactor
import com.gaxim.myweather.presentation.common.ErrorKind
import com.gaxim.myweather.presentation.ui.displayName
import com.gaxim.myweather.presentation.ui.labelRes
import com.gaxim.myweather.presentation.ui.messageRes
import com.gaxim.myweather.presentation.ui.textRes
import com.gaxim.myweather.presentation.ui.theme.MyWeatherTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    state: RankingUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.city?.let { stringResource(R.string.ranking_title, it.displayName()) }
                            ?: stringResource(R.string.app_name),
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.ranking_back)) }
                },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier.fillMaxSize().padding(innerPadding)
        when (state) {
            is RankingUiState.Loading -> Box(contentModifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is RankingUiState.Error -> Column(
                modifier = contentModifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(state.kind.messageRes()))
                if (state.city != null) {
                    TextButton(onClick = onRetry) { Text(stringResource(R.string.ranking_retry)) }
                }
            }
            is RankingUiState.Success -> RankingList(state.days, false, onRefresh, contentModifier)
            is RankingUiState.Refreshing -> RankingList(state.days, true, onRefresh, contentModifier)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RankingList(
    days: List<DayRanking>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(days, key = { it.date.toString() }) { day -> DayRankingCard(day) }
        }
    }
}

@Composable
fun DayRankingCard(day: DayRanking, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = day.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                style = MaterialTheme.typography.titleMedium,
            )
            day.scores.forEach { ActivityScoreRow(it) }
        }
    }
}

@Composable
fun ActivityScoreRow(score: ActivityScore, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(score.activity.labelRes()),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.ranking_score, score.score),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Text(
            text = stringResource(score.reason.textRes()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (score.missingFactors.isNotEmpty()) {
            val factors = score.missingFactors.map { stringResource(it.labelRes()) }.joinToString()
            Text(
                text = stringResource(R.string.ranking_ignored_factors, factors),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val previewCity = City("Zurich", "Switzerland", "Zurich", 47.37, 8.54)

private val previewDay = DayRanking(
    date = LocalDate.of(2026, 1, 15),
    scores = listOf(
        ActivityScore(Activity.INDOOR_SIGHTSEEING, 80, ReasonKey.INDOOR_ANY_WEATHER, emptySet()),
        ActivityScore(Activity.OUTDOOR_SIGHTSEEING, 55, ReasonKey.OUTDOOR_TOO_COLD, emptySet()),
        ActivityScore(Activity.SKIING, 20, ReasonKey.SKI_TOO_WARM, setOf(WeatherFactor.SNOWFALL)),
        ActivityScore(Activity.SURFING, 5, ReasonKey.SURF_TOO_CALM, emptySet()),
    ),
)

private class RankingUiStateProvider : PreviewParameterProvider<RankingUiState> {
    override val values = sequenceOf(
        RankingUiState.Loading(previewCity),
        RankingUiState.Success(
            previewCity,
            listOf(previewDay, previewDay.copy(date = previewDay.date.plusDays(1))),
        ),
        RankingUiState.Refreshing(previewCity, listOf(previewDay)),
        RankingUiState.Error(previewCity, ErrorKind.TIMEOUT),
    )
}

@Preview(showBackground = true)
@Composable
private fun RankingScreenPreview(
    @PreviewParameter(RankingUiStateProvider::class) state: RankingUiState,
) {
    MyWeatherTheme {
        RankingScreen(state = state, onBack = {}, onRetry = {}, onRefresh = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DayRankingCardPreview() {
    MyWeatherTheme {
        DayRankingCard(previewDay)
    }
}
