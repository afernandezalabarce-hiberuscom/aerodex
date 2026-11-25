package dev.afalabarce.aircrafts.presentation.viewmodels.di

import dev.afalabarce.aircrafts.core.common.di.KoinModuleLoader
import dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft.AircraftViewModel
import dev.afalabarce.aircrafts.presentation.viewmodels.features.example.EntityExampleViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object PresentationViewModelsDependencyInjector : KoinModuleLoader {
    override val modules: List<Module>
        get() = listOf(
            module {
                viewModelOf(::EntityExampleViewModel)
                viewModel {
                    AircraftViewModel(
                        searchAircraftModelsUseCase = get(),
                        getAircraftModelDetailUseCase = get(),
                        observeFavoriteCodesUseCase = get(),
                        observeFavoritesUseCase = get(),
                        toggleFavoriteAircraftUseCase = get()
                    )
                }
            }
        )
}
