package dev.afalabarce.aircrafts.data.datasources.core.di

import de.jensklingenberg.ktorfit.Ktorfit
import dev.afalabarce.aircrafts.core.common.di.KoinModuleLoader
import dev.afalabarce.aircrafts.data.datasources.core.db.getAircraftDao
import dev.afalabarce.aircrafts.data.datasources.core.db.getExampleDao
import dev.afalabarce.aircrafts.data.datasources.core.db.getRoomDatabase
import dev.afalabarce.aircrafts.data.datasources.core.features.aircraft.AircraftLocalDataSourceImpl
import dev.afalabarce.aircrafts.data.datasources.core.features.aircraft.AircraftRemoteDataSourceImpl
import dev.afalabarce.aircrafts.data.datasources.core.features.example.EntityDataStoreImpl
import dev.afalabarce.aircrafts.data.datasources.core.features.preferences.AppPreferencesImpl
import dev.afalabarce.aircrafts.data.datasources.core.remote.ApiService
import dev.afalabarce.aircrafts.data.datasources.core.remote.createApiService
import dev.afalabarce.aircrafts.data.datasources.features.aircraft.AircraftLocalDataSource
import dev.afalabarce.aircrafts.data.datasources.features.aircraft.AircraftRemoteDataSource
import dev.afalabarce.aircrafts.data.datasources.features.example.EntityDataStore
import dev.afalabarce.aircrafts.data.datasources.features.preferences.AppPreferences
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect fun getPlatformInjects(): List<Module>

object DataSourceCoreDependencyInjector : KoinModuleLoader {
    override val modules: List<Module>
        get() = getPlatformInjects().union(
            listOf(
                module {
                    single {
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            encodeDefaults = true
                        }
                    }
                    single {
                        val jsonSerializer = get<Json>()
                        HttpClient {
                            install(ContentNegotiation) {
                                json(jsonSerializer)
                            }
                            install(HttpTimeout) {
                                requestTimeoutMillis = 15_000
                                connectTimeoutMillis = 10_000
                                socketTimeoutMillis = 15_000
                            }
                            install(Logging) {
                                logger = object : Logger {
                                    override fun log(message: String) {
                                        Napier.d(message = message, tag = "FlightAPI")
                                    }
                                }
                                level = LogLevel.INFO
                            }
                        }
                    }
                    single<ApiService> {
                        Ktorfit
                            .Builder()
                            .baseUrl(ApiService.API_URL)
                            .httpClient(get<HttpClient>())
                            .build()
                            .createApiService()
                    }
                    singleOf(::getRoomDatabase)
                    singleOf(::getExampleDao)
                    singleOf(::getAircraftDao)
                    singleOf(::AppPreferencesImpl) bind AppPreferences::class
                    factoryOf(::EntityDataStoreImpl) bind EntityDataStore::class
                    factoryOf(::AircraftRemoteDataSourceImpl) bind AircraftRemoteDataSource::class
                    factory<AircraftLocalDataSource> {
                        AircraftLocalDataSourceImpl(
                            aircraftDao = get(),
                            json = get()
                        )
                    }
                }
            )
        ).toList()
}
