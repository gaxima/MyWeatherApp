package com.gaxim.myweather.data.repository

import com.gaxim.myweather.data.remote.ApiJson
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Builds a real Retrofit service talking to this server, with a short timeout for timeout tests. */
internal inline fun <reified T> MockWebServer.createApi(readTimeoutMs: Long = 5_000): T {
    val client = OkHttpClient.Builder()
        .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
        .build()
    return Retrofit.Builder()
        .baseUrl(url("/"))
        .client(client)
        .addConverterFactory(ApiJson.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(T::class.java)
}
