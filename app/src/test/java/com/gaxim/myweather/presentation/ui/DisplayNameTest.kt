package com.gaxim.myweather.presentation.ui

import com.gaxim.myweather.domain.usecase.city
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayNameTest {

    @Test
    fun `given all parts, when formatted, then name region and country are joined`() {
        assertEquals("Bergen, Vestland, Norway", city("Bergen", "Norway", "Vestland").displayName())
    }

    @Test
    fun `given missing region and country, when formatted, then only the name remains`() {
        assertEquals("Oslo", city("Oslo", null, null).displayName())
    }

    @Test
    fun `given region equal to name, when formatted, then it is not repeated`() {
        assertEquals("Oslo, Norway", city("Oslo", "Norway", "Oslo").displayName())
    }
}
