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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gaxim.myweather.R
import com.gaxim.myweather.domain.model.ActivityScore
import com.gaxim.myweather.domain.model.DayRanking
import com.gaxim.myweather.presentation.ui.displayName
import com.gaxim.myweather.presentation.ui.labelRes
import com.gaxim.myweather.presentation.ui.messageRes
import com.gaxim.myweather.presentation.ui.textRes
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    state: RankingUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
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
            is RankingUiState.Success -> LazyColumn(
                modifier = contentModifier,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.days, key = { it.date.toString() }) { day -> DayRankingCard(day) }
            }
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
