package com.gaxim.myweather.data.remote

import kotlinx.serialization.json.Json

/** The one JSON configuration shared by the app and the tests. Extra provider fields are ignored. */
val ApiJson: Json = Json { ignoreUnknownKeys = true }
