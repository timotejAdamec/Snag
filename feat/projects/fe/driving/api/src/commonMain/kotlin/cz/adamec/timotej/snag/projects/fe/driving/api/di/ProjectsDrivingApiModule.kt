package cz.adamec.timotej.snag.projects.fe.driving.api.di

import org.koin.core.module.Module
import org.koin.dsl.module

val projectsDrivingApiModule = module {
    includes(platformModule)
}

internal expect val platformModule: Module
