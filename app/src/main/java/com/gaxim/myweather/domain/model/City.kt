package com.gaxim.myweather.domain.model

data class City(
    val name: String,
    val country: String?,
    val region: String?,
    val latitude: Double,
    val longitude: Double,
)
