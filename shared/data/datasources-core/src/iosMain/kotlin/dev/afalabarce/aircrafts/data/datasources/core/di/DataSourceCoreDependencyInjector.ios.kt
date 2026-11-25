package dev.afalabarce.aircrafts.data.datasources.core.di

import dev.afalabarce.aircrafts.data.datasources.core.db.getDatabaseBuilder
import dev.afalabarce.aircrafts.data.datasources.core.features.preferences.dataStore
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import dev.afalabarce.aircrafts.data.datasources.core.db.getDatabaseBuilder

actual fun getPlatformInjects(): List<Module> = listOf(
    module {
        singleOf(::getDatabaseBuilder)
        singleOf(::dataStore)
    }
)
