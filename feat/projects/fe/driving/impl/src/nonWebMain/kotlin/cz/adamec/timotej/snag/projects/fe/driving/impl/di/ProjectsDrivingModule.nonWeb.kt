package cz.adamec.timotej.snag.projects.fe.driving.impl.di

import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRouteImpl
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

internal actual val platformModule = module {
    navigation<ProjectsRouteImpl> { _ ->
        projectsScreenInjection()
    }
}
