package cz.adamec.timotej.snag.projects.fe.driven.di

import cz.adamec.timotej.snag.projects.fe.driven.InMemoryProjectsLocalDataSource
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsLocalDataSource
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val projectsDrivenModule = module {
    singleOf(::InMemoryProjectsLocalDataSource) {
        bind<ProjectsLocalDataSource>()
    }
}
