package cz.adamec.timotej.snag.projects.fe.app.di

import cz.adamec.timotej.snag.projects.fe.app.GetProjectsUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val projectsAppModule = module {
    singleOf(::GetProjectsUseCase)
}
