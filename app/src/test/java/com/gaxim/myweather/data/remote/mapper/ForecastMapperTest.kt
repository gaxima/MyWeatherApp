package com.gaxim.myweather.data.remote.mapper

import com.gaxim.myweather.data.remote.dto.DailyDto
import com.gaxim.myweather.data.remote.dto.ForecastResponseDto
import com.gaxim.myweather.domain.model.DailyForecast
import com.gaxim.myweather.domain.model.DomainError
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ForecastMapperTest {

    @Test
    fun `given complete arrays, when mapping, then each day gets its own values`() {
        val dto = ForecastResponseDto(
            DailyDto(
                time = listOf("2026-01-15", "2026-01-16"),
                weatherCode = listOf(0, 61),
                temperatureMax = listOf(10.0, 12.0),
                temperatureMin = listOf(1.0, 2.0),
                precipitationSum = listOf(0.0, 5.5),
                snowfallSum = listOf(0.0, 0.0),
                windSpeedMax = listOf(10.0, 20.0),
                windGustsMax = listOf(20.0, 40.0),
            ),
        )

        assertEquals(
            listOf(
                DailyForecast(LocalDate.of(2026, 1, 15), 0, 10.0, 1.0, 0.0, 0.0, 10.0, 20.0),
                DailyForecast(LocalDate.of(2026, 1, 16), 61, 12.0, 2.0, 5.5, 0.0, 20.0, 40.0),
            ),
            dto.toDomain(),
        )
    }

    @Test
    fun `given null values in arrays, when mapping, then those fields are null`() {
        val dto = ForecastResponseDto(
            DailyDto(
                time = listOf("2026-01-15"),
                weatherCode = listOf(null),
                temperatureMax = listOf(null),
                snowfallSum = listOf(null),
            ),
        )

        val day = dto.toDomain().single()

        assertNull(day.weatherCode)
        assertNull(day.temperatureMax)
        assertNull(day.snowfallSum)
    }

    @Test
    fun `given missing arrays, when mapping, then fields are null but days are kept`() {
        val dto = ForecastResponseDto(DailyDto(time = listOf("2026-01-15", "2026-01-16")))

        val days = dto.toDomain()

        assertEquals(2, days.size)
        assertNull(days[1].windGustsMax)
    }

    @Test
    fun `given array shorter than time, when mapping, then trailing days are null`() {
        val dto = ForecastResponseDto(
            DailyDto(
                time = listOf("2026-01-15", "2026-01-16"),
                temperatureMax = listOf(9.0),
            ),
        )

        val days = dto.toDomain()

        assertEquals(9.0, days[0].temperatureMax)
        assertNull(days[1].temperatureMax)
    }

    @Test
    fun `given no daily block, when mapping, then it is an invalid response`() {
        assertThrows(DomainError.InvalidResponse::class.java) {
            ForecastResponseDto(daily = null).toDomain()
        }
    }

    @Test
    fun `given unparsable date, when mapping, then it is an invalid response`() {
        assertThrows(DomainError.InvalidResponse::class.java) {
            ForecastResponseDto(DailyDto(time = listOf("not-a-date"))).toDomain()
        }
    }

    @Test
    fun `given empty time array, when mapping, then result is empty`() {
        assertEquals(emptyList<DailyForecast>(), ForecastResponseDto(DailyDto()).toDomain())
    }
}
