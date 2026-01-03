package cz.adamec.timotej.snag.projects.fe.driving.api.di

import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectsRouteImpl
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule = module {
    single { WebProjectsRouteImpl } bind ProjectsRoute::class
}
