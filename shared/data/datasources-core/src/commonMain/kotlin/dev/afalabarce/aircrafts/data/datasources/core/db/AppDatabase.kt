@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package dev.afalabarce.aircrafts.data.datasources.core.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.afalabarce.aircrafts.data.datasources.core.features.aircraft.AircraftDao
import dev.afalabarce.aircrafts.data.datasources.core.features.example.ExampleDao
import dev.afalabarce.aircrafts.models.features.aircraft.local.CachedAircraftDetailEntity
import dev.afalabarce.aircrafts.models.features.aircraft.local.CachedAircraftSummaryEntity
import dev.afalabarce.aircrafts.models.features.aircraft.local.FavoriteAircraftEntity
import dev.afalabarce.aircrafts.models.features.example.local.CacheExampleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

@Database(
    entities = [
        CacheExampleEntity::class,
        CachedAircraftSummaryEntity::class,
        CachedAircraftDetailEntity::class,
        FavoriteAircraftEntity::class
    ],
    version = 3,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exampleDao(): ExampleDao
    abstract fun aircraftDao(): AircraftDao

    companion object {
        const val DATABASE_NAME = "AppDatabase.db"
    }
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .addMigrations()
        .fallbackToDestructiveMigration(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

fun getExampleDao(appDatabase: AppDatabase): ExampleDao = appDatabase.exampleDao()

fun getAircraftDao(appDatabase: AppDatabase): AircraftDao = appDatabase.aircraftDao()
