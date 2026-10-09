package com.gaxim.myweather.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `given system, when advancing, then light follows`() {
        assertEquals(ThemeMode.Light, ThemeMode.System.next())
    }

    @Test
    fun `given light, when advancing, then dark follows`() {
        assertEquals(ThemeMode.Dark, ThemeMode.Light.next())
    }

    @Test
    fun `given dark, when advancing, then it wraps back to system`() {
        assertEquals(ThemeMode.System, ThemeMode.Dark.next())
    }
}
