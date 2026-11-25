package dev.afalabarce.aircrafts.core.di

import dev.afalabarce.aircrafts.core.common.di.KoinModuleLoader
import dev.afalabarce.aircrafts.core.ui.di.CoreUiDependencyInjector
import dev.afalabarce.aircrafts.data.datasources.core.di.DataSourceCoreDependencyInjector
import dev.afalabarce.aircrafts.data.repository.di.DataRepositoryDependencyInjector
import dev.afalabarce.aircrafts.domain.usecases.di.DomainUseCasesDependencyInjector
import dev.afalabarce.aircrafts.presentation.viewmodels.di.PresentationViewModelsDependencyInjector
import org.koin.core.module.Module

object CoreDiDependencyInjector : KoinModuleLoader {
    override val modules: List<Module>
        get() = listOf(
            DataSourceCoreDependencyInjector.modules,
            DataRepositoryDependencyInjector.modules,
            DomainUseCasesDependencyInjector.modules,
            CoreUiDependencyInjector.modules,
            PresentationViewModelsDependencyInjector.modules,
        ).flatten()
}
