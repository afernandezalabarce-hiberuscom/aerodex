package dev.afalabarce.aircrafts.domain.usecases.di

import dev.afalabarce.aircrafts.core.common.di.KoinModuleLoader
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.GetAircraftModelDetailUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.ObserveFavoriteCodesUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.ObserveFavoritesUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.SearchAircraftModelsUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.aircraft.ToggleFavoriteAircraftUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.example.GetEntitiesUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.preferences.GetDeviceIdUseCase
import dev.afalabarce.aircrafts.domain.usecases.features.preferences.SetDeviceIdUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

object DomainUseCasesDependencyInjector : KoinModuleLoader {
    override val modules: List<Module>
        get() = listOf(
            module {
                factoryOf(::GetDeviceIdUseCase)
                factoryOf(::SetDeviceIdUseCase)
                factoryOf(::GetEntitiesUseCase)
                factoryOf(::SearchAircraftModelsUseCase)
                factoryOf(::GetAircraftModelDetailUseCase)
                factoryOf(::ObserveFavoriteCodesUseCase)
                factoryOf(::ObserveFavoritesUseCase)
                factoryOf(::ToggleFavoriteAircraftUseCase)
            }
        )
}
