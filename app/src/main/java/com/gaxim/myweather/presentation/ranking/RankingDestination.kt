package com.gaxim.myweather.presentation.ranking

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import com.gaxim.myweather.presentation.ui.messageRes
import kotlinx.coroutines.flow.Flow

/** The Ranking destination: [RankingScreen] plus the snackbar for [events]. */
@Composable
fun RankingDestination(
    state: RankingUiState,
    events: Flow<RankingEvent>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is RankingEvent.RefreshFailed ->
                    snackbarHostState.showSnackbar(resources.getString(event.kind.messageRes()))
            }
        }
    }

    RankingScreen(
        state = state,
        onBack = onBack,
        onRetry = onRetry,
        onRefresh = onRefresh,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}
