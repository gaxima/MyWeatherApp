package com.gaxim.myweather.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.gaxim.myweather.R
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.presentation.common.ErrorKind
import com.gaxim.myweather.presentation.ui.displayName
import com.gaxim.myweather.presentation.ui.messageRes
import com.gaxim.myweather.presentation.ui.theme.MyWeatherTheme

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onCityClick: (City) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            label = { Text(stringResource(R.string.search_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    TextButton(onClick = { onQueryChange("") }) {
                        Text(stringResource(R.string.search_clear))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        SearchContent(
            state = state,
            onCityClick = onCityClick,
            modifier = Modifier.fillMaxSize().padding(top = 16.dp),
        )
    }
}

@Composable
private fun SearchContent(
    state: SearchUiState,
    onCityClick: (City) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is SearchUiState.Idle -> CenteredMessage(stringResource(R.string.search_idle), modifier)
        is SearchUiState.Loading -> Box(modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is SearchUiState.Empty ->
            CenteredMessage(stringResource(R.string.search_empty, state.query.trim()), modifier)
        is SearchUiState.Error ->
            CenteredMessage(stringResource(state.kind.messageRes()), modifier)
        is SearchUiState.Success -> LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(state.cities) { city ->
                ListItem(
                    headlineContent = { Text(city.displayName()) },
                    modifier = Modifier.clickable { onCityClick(city) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun CenteredMessage(text: String, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val previewCities = listOf(
    City("Zurich", "Switzerland", "Zurich", 47.37, 8.54),
    City("Zurich", "United States", "Illinois", 42.15, -88.13),
)

private class SearchUiStateProvider : PreviewParameterProvider<SearchUiState> {
    override val values = sequenceOf(
        SearchUiState.Idle(),
        SearchUiState.Loading("zur"),
        SearchUiState.Success("zurich", previewCities),
        SearchUiState.Empty("qwertyuiop"),
        SearchUiState.Error("zurich", ErrorKind.NETWORK),
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview(
    @PreviewParameter(SearchUiStateProvider::class) state: SearchUiState,
) {
    MyWeatherTheme {
        SearchScreen(state = state, onQueryChange = {}, onCityClick = {})
    }
}
