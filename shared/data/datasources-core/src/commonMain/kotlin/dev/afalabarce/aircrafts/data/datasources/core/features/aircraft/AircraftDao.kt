package dev.afalabarce.aircrafts.data.datasources.core.features.aircraft

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.afalabarce.aircrafts.models.features.aircraft.local.CachedAircraftDetailEntity
import dev.afalabarce.aircrafts.models.features.aircraft.local.CachedAircraftSummaryEntity
import dev.afalabarce.aircrafts.models.features.aircraft.local.FavoriteAircraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AircraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSummaries(items: List<CachedAircraftSummaryEntity>)

    @Query(
        """
        SELECT * FROM cached_aircraft_summary
        WHERE (:category IS NULL OR category = :category)
          AND (
            :query = '' OR
            icaoTypeCode LIKE '%' || :query || '%' OR
            IFNULL(iataTypeCode, '') LIKE '%' || :query || '%' OR
            IFNULL(manufacturer, '') LIKE '%' || :query || '%' OR
            IFNULL(model, '') LIKE '%' || :query || '%' OR
            name LIKE '%' || :query || '%'
          )
        ORDER BY icaoTypeCode ASC
        LIMIT :limit
        """
    )
    suspend fun searchSummaries(
        query: String,
        category: String?,
        limit: Int
    ): List<CachedAircraftSummaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDetail(entity: CachedAircraftDetailEntity)

    @Query("SELECT * FROM cached_aircraft_detail WHERE icaoTypeCode = :icaoTypeCode LIMIT 1")
    suspend fun getDetail(icaoTypeCode: String): CachedAircraftDetailEntity?

    @Query("SELECT icaoTypeCode FROM favorite_aircraft")
    fun observeFavoriteCodes(): Flow<List<String>>

    @Query("SELECT * FROM favorite_aircraft ORDER BY savedAtMs DESC")
    fun observeFavorites(): Flow<List<FavoriteAircraftEntity>>

    @Query("SELECT COUNT(*) FROM favorite_aircraft WHERE icaoTypeCode = :icaoTypeCode")
    suspend fun isFavorite(icaoTypeCode: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteAircraftEntity)

    @Query("DELETE FROM favorite_aircraft WHERE icaoTypeCode = :icaoTypeCode")
    suspend fun deleteFavorite(icaoTypeCode: String)
}
