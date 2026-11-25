package dev.afalabarce.aircrafts.data.repository.di

import dev.afalabarce.aircrafts.core.common.di.KoinModuleLoader
import dev.afalabarce.aircrafts.data.repository.features.aircraft.AircraftRepositoryImpl
import dev.afalabarce.aircrafts.data.repository.features.aircraft.cache.AircraftMemoryCache
import dev.afalabarce.aircrafts.data.repository.features.example.EntityRepositoryImpl
import dev.afalabarce.aircrafts.data.repository.features.preferences.PreferencesRepositoryImpl
import dev.afalabarce.aircrafts.domain.repository.features.aircraft.AircraftRepository
import dev.afalabarce.aircrafts.domain.repository.features.example.EntityRepository
import dev.afalabarce.aircrafts.domain.repository.features.preferences.PreferencesRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

object DataRepositoryDependencyInjector : KoinModuleLoader {
    override val modules: List<Module>
        get() = listOf(
            module {
                single { AircraftMemoryCache() }
                single<AircraftRepository> {
                    AircraftRepositoryImpl(
                        remoteDataSource = get(),
                        localDataSource = get(),
                        memoryCache = get()
                    )
                }
                factoryOf(::PreferencesRepositoryImpl) bind PreferencesRepository::class
                factoryOf(::EntityRepositoryImpl) bind EntityRepository::class
            }
        )
}
