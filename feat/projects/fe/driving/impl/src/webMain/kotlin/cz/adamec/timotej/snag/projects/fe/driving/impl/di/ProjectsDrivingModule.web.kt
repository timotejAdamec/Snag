package cz.adamec.timotej.snag.projects.fe.driving.impl.di

import cz.adamec.timotej.snag.projects.fe.driving.api.WebProjectsRouteImpl
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

internal actual val platformModule = module {
    navigation<WebProjectsRouteImpl> { _ ->
        projectsScreenInjection()
    }
}
