package com.gaxim.myweather.domain.usecase

import com.gaxim.myweather.domain.model.DomainError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchCitiesUseCaseTest {

    private val repository = FakeCityRepository()
    private val useCase = SearchCitiesUseCase(repository)

    @Test
    fun `given blank query, when searching, then returns empty without calling repository`() = runTest {
        val result = useCase("   ")

        assertEquals(emptyList<Any>(), result.getOrThrow())
        assertTrue(repository.queries.isEmpty())
    }

    @Test
    fun `given empty query, when searching, then returns empty without calling repository`() = runTest {
        val result = useCase("")

        assertEquals(emptyList<Any>(), result.getOrThrow())
        assertTrue(repository.queries.isEmpty())
    }

    @Test
    fun `given padded query, when searching, then repository receives trimmed query`() = runTest {
        useCase("  Oslo \n")

        assertEquals(listOf("Oslo"), repository.queries)
    }

    @Test
    fun `given matches, when searching, then returns the cities`() = runTest {
        val cities = listOf(city("Oslo"), city("Osaka"))
        repository.result = Result.success(cities)

        assertEquals(cities, useCase("Os").getOrThrow())
    }

    @Test
    fun `given no matches, when searching, then returns success with empty list`() = runTest {
        repository.result = Result.success(emptyList())

        val result = useCase("zzzz")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `given repository failure, when searching, then the domain error passes through`() = runTest {
        val error = DomainError.Timeout()
        repository.result = Result.failure(error)

        val result = useCase("Oslo")

        assertSame(error, result.exceptionOrNull())
    }
}
