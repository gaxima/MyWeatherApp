package com.gaxim.myweather.presentation

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

/** Renders each screenshot test in light and dark theme. */
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
annotation class LightAndDarkPreviews
