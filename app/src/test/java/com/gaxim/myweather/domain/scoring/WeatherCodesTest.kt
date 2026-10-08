package com.gaxim.myweather.domain.scoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherCodesTest {

    @Test
    fun `given clear sky code, when rating outdoor friendliness, then it is perfect`() {
        assertEquals(1.0, WeatherCodes.outdoorFriendliness(0)!!, 0.0)
    }

    @Test
    fun `given thunderstorm codes, when rating, then friendliness is zero`() {
        listOf(95, 96, 99).forEach {
            assertEquals("code $it", 0.0, WeatherCodes.outdoorFriendliness(it)!!, 0.0)
        }
    }

    @Test
    fun `given worsening conditions, when rating, then friendliness decreases`() {
        val clear = WeatherCodes.outdoorFriendliness(0)!!
        val overcast = WeatherCodes.outdoorFriendliness(3)!!
        val drizzle = WeatherCodes.outdoorFriendliness(53)!!
        val rain = WeatherCodes.outdoorFriendliness(63)!!

        assertTrue(clear > overcast && overcast > drizzle && drizzle > rain)
    }

    @Test
    fun `given unknown code, when rating, then friendliness is neutral`() {
        assertEquals(WeatherCodes.UNKNOWN_FRIENDLINESS, WeatherCodes.outdoorFriendliness(123)!!, 0.0)
    }

    @Test
    fun `given missing code, when rating, then result is null`() {
        assertNull(WeatherCodes.outdoorFriendliness(null))
    }

    @Test
    fun `given thunderstorm code, when checking, then isThunderstorm is true`() {
        assertTrue(WeatherCodes.isThunderstorm(95))
        assertTrue(!WeatherCodes.isThunderstorm(63))
        assertTrue(!WeatherCodes.isThunderstorm(null))
    }
}
