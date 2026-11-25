package dev.afalabarce.aircrafts.models.features.aircraft.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_aircraft")
data class FavoriteAircraftEntity(
    @PrimaryKey
    val icaoTypeCode: String,
    val iataTypeCode: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val name: String,
    val category: String,
    val photoUrl: String? = null,
    val photoAuthor: String? = null,
    val firstFlightYear: Int? = null,
    val engineCount: Int? = null,
    val engineType: String? = null,
    val wakeTurbulenceCategory: String? = null,
    val maxPassengers: Int? = null,
    val cruiseSpeedKt: Int? = null,
    val maxSpeedKt: Int? = null,
    val rangeNm: Int? = null,
    val rangeKm: Int? = null,
    val savedAtMs: Long = 0L,
)
