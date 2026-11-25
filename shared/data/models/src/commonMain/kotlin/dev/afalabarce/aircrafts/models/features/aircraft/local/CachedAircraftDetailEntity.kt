package dev.afalabarce.aircrafts.models.features.aircraft.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_aircraft_detail")
data class CachedAircraftDetailEntity(
    @PrimaryKey
    val icaoTypeCode: String,
    val payloadJson: String,
    val updatedAtMs: Long = 0L,
)
