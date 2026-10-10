package com.gaxim.myweather.presentation.ui

import androidx.annotation.StringRes
import com.gaxim.myweather.R
import com.gaxim.myweather.domain.model.Activity
import com.gaxim.myweather.domain.model.City
import com.gaxim.myweather.domain.model.ReasonKey
import com.gaxim.myweather.domain.model.WeatherFactor
import com.gaxim.myweather.presentation.common.ErrorKind

/** Maps domain keys to string resources, so composables hold no `when` logic of their own. */

@StringRes
fun Activity.labelRes(): Int = when (this) {
    Activity.SKIING -> R.string.activity_skiing
    Activity.SURFING -> R.string.activity_surfing
    Activity.OUTDOOR_SIGHTSEEING -> R.string.activity_outdoor_sightseeing
    Activity.INDOOR_SIGHTSEEING -> R.string.activity_indoor_sightseeing
}

@StringRes
fun ReasonKey.textRes(): Int = when (this) {
    ReasonKey.SKI_FRESH_SNOW -> R.string.reason_ski_fresh_snow
    ReasonKey.SKI_NO_SNOW -> R.string.reason_ski_no_snow
    ReasonKey.SKI_LITTLE_FRESH_SNOW -> R.string.reason_ski_little_fresh_snow
    ReasonKey.SKI_TOO_WARM -> R.string.reason_ski_too_warm
    ReasonKey.SURF_RIDEABLE_WIND -> R.string.reason_surf_rideable_wind
    ReasonKey.SURF_TOO_CALM -> R.string.reason_surf_too_calm
    ReasonKey.STRONG_WIND -> R.string.reason_strong_wind
    ReasonKey.STORM -> R.string.reason_storm
    ReasonKey.RAIN -> R.string.reason_rain
    ReasonKey.OUTDOOR_PLEASANT -> R.string.reason_outdoor_pleasant
    ReasonKey.OUTDOOR_TOO_COLD -> R.string.reason_outdoor_too_cold
    ReasonKey.OUTDOOR_TOO_HOT -> R.string.reason_outdoor_too_hot
    ReasonKey.INDOOR_ANY_WEATHER -> R.string.reason_indoor_any_weather
    ReasonKey.INSUFFICIENT_DATA -> R.string.reason_insufficient_data
}

@StringRes
fun WeatherFactor.labelRes(): Int = when (this) {
    WeatherFactor.WEATHER_CODE -> R.string.factor_weather_code
    WeatherFactor.TEMPERATURE -> R.string.factor_temperature
    WeatherFactor.PRECIPITATION -> R.string.factor_precipitation
    WeatherFactor.SNOWFALL -> R.string.factor_snowfall
    WeatherFactor.WIND_SPEED -> R.string.factor_wind_speed
    WeatherFactor.WIND_GUSTS -> R.string.factor_wind_gusts
}

@StringRes
fun ErrorKind.messageRes(): Int = when (this) {
    ErrorKind.NETWORK -> R.string.error_network
    ErrorKind.TIMEOUT -> R.string.error_timeout
    ErrorKind.INVALID_RESPONSE -> R.string.error_invalid_response
    ErrorKind.UNKNOWN -> R.string.error_unknown
}

/** "Name, Region, Country" with absent or repeated parts left out. */
fun City.displayName(): String =
    listOfNotNull(name, region, country).distinct().joinToString(", ")
