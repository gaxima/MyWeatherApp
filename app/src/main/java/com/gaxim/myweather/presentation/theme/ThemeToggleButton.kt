package com.gaxim.myweather.presentation.theme

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gaxim.myweather.R
import com.gaxim.myweather.domain.model.ThemeMode

/** Shows the current [mode] and asks for the next one on click. */
@Composable
fun ThemeToggleButton(
    mode: ThemeMode,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(mode.labelRes())
    val description = stringResource(R.string.theme_toggle_description, label)
    TextButton(
        onClick = onToggle,
        modifier = modifier.semantics { contentDescription = description },
    ) {
        Text(label)
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.System -> R.string.theme_system
    ThemeMode.Light -> R.string.theme_light
    ThemeMode.Dark -> R.string.theme_dark
}
