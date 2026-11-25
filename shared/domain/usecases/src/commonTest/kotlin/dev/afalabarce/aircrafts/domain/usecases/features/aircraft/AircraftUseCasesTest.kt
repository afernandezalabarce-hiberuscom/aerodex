package dev.afalabarce.aircrafts.domain.usecases.features.aircraft

import dev.afalabarce.aircrafts.domain.models.AircraftCategory
import dev.afalabarce.aircrafts.domain.models.AircraftDetailResult
import dev.afalabarce.aircrafts.domain.models.AircraftModelDetail
import dev.afalabarce.aircrafts.domain.models.AircraftModelSummary
import dev.afalabarce.aircrafts.domain.models.AircraftSearchResult
import dev.afalabarce.aircrafts.domain.models.DataSourceOrigin
import dev.afalabarce.aircrafts.domain.repository.features.aircraft.AircraftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private class FakeAircraftRepository : AircraftRepository {
    var lastQuery: String? = null
    var lastCategory: AircraftCategory? = null
    var lastDetailIcao: String? = null

    override suspend fun searchAircraftModels(
        query: String,
        category: AircraftCategory?,
        cursor: String?,
        limit: Int
    ): AircraftSearchResult {
        lastQuery = query
        lastCategory = category
        return AircraftSearchResult(
            items = listOf(
                AircraftModelSummary(
                    icaoTypeCode = "A320",
                    manufacturer = "Airbus",
                    model = "A320",
                    name = "Airbus A320",
                    category = AircraftCategory.COMMERCIAL
                )
            ),
            nextCursor = null,
            hasMore = false,
            origin = DataSourceOrigin.NETWORK
        )
    }

    override suspend fun getAircraftModelDetail(icaoTypeCode: String): AircraftDetailResult {
        lastDetailIcao = icaoTypeCode
        return AircraftDetailResult(
            detail = AircraftModelDetail(
                icaoTypeCode = icaoTypeCode,
                name = "Airbus A320",
                category = AircraftCategory.COMMERCIAL
            ),
            origin = DataSourceOrigin.NETWORK
        )
    }

    override fun observeFavoriteCodes(): Flow<Set<String>> = flowOf(emptySet())

    override fun observeFavorites(): Flow<List<AircraftModelSummary>> = flowOf(emptyList())

    override suspend fun toggleFavorite(aircraft: AircraftModelSummary): Boolean = true
}

class AircraftUseCasesTest {

    @Test
    fun searchUseCaseTrimsQueryAndPassesCategory() = runTest {
        val repository = FakeAircraftRepository()
        val useCase = SearchAircraftModelsUseCase(repository)

        val result = useCase(query = "  A320  ", category = AircraftCategory.COMMERCIAL)

        assertEquals("A320", repository.lastQuery)
        assertEquals(AircraftCategory.COMMERCIAL, repository.lastCategory)
        assertEquals(1, result.items.size)
        assertEquals("A320", result.items.first().icaoTypeCode)
    }

    @Test
    fun detailUseCaseNormalizesIcaoCodeToUppercase() = runTest {
        val repository = FakeAircraftRepository()
        val useCase = GetAircraftModelDetailUseCase(repository)

        val result = useCase("  a35k ")

        assertEquals("A35K", repository.lastDetailIcao)
        assertEquals("A35K", result.detail.icaoTypeCode)
    }

    @Test
    fun detailUseCaseRejectsBlankIcaoCode() = runTest {
        val repository = FakeAircraftRepository()
        val useCase = GetAircraftModelDetailUseCase(repository)

        assertFailsWith<IllegalArgumentException> {
            useCase("   ")
        }
    }
}
