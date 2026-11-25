package dev.afalabarce.aircrafts.presentation.ui.di

import dev.afalabarce.aircrafts.core.common.di.KoinModuleLoader
import dev.afalabarce.aircrafts.core.di.CoreDiDependencyInjector
import org.koin.core.module.Module

object PresentationUiDependencyInjector : KoinModuleLoader {
    override val modules: List<Module>
        get() = listOf(
            CoreDiDependencyInjector.modules,
            listOf(

            )
        ).flatten()

}
