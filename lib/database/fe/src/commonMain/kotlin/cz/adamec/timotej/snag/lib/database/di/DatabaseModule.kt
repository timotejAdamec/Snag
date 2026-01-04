package cz.adamec.timotej.snag.lib.database.di

import org.koin.core.module.Module
import org.koin.dsl.module

val databaseModule = module {
    includes(platformModule)
}

internal expect val platformModule: Module
