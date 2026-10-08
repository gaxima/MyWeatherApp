package com.gaxim.myweather.data.remote

import com.gaxim.myweather.data.remote.dto.GeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = DEFAULT_RESULT_COUNT,
    ): GeocodingResponseDto

    companion object {
        const val DEFAULT_RESULT_COUNT = 10
    }
}
