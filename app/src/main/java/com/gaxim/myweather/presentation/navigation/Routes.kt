package com.gaxim.myweather.presentation.navigation

import androidx.lifecycle.SavedStateHandle
import com.gaxim.myweather.domain.model.City
import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute

/**
 * Navigation destination for a city's ranking. Property names are the keys the navigation
 * library stores in the destination's [SavedStateHandle], which [cityFrom] reads back.
 */
@Serializable
data class RankingRoute(
    val name: String,
    val country: String?,
    val region: String?,
    val latitude: Double,
    val longitude: Double,
) {
    companion object {
        fun from(city: City) = RankingRoute(
            name = city.name,
            country = city.country,
            region = city.region,
            latitude = city.latitude,
            longitude = city.longitude,
        )

        /** The city carried by [handle], or null if the required arguments are missing. */
        fun cityFrom(handle: SavedStateHandle): City? {
            val name = handle.get<String>("name") ?: return null
            val latitude = handle.get<Double>("latitude") ?: return null
            val longitude = handle.get<Double>("longitude") ?: return null
            return City(
                name = name,
                country = handle["country"],
                region = handle["region"],
                latitude = latitude,
                longitude = longitude,
            )
        }
    }
}
