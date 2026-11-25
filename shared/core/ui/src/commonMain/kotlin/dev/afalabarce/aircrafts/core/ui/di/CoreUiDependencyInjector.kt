package dev.afalabarce.aircrafts.core.ui.di

import dev.afalabarce.aircrafts.core.common.di.KoinModuleLoader
import org.koin.core.module.Module

object CoreUiDependencyInjector : KoinModuleLoader {
    override val modules: List<Module>
        get() = getPlatformDependencyInjects().union(
            listOf()
        ).toList()
}

expect fun getPlatformDependencyInjects(): List<Module>
