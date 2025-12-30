package cz.adamec.timotej.snag.projects.fe.driving.impl.di

import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.ui.ProjectsScreen
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.vm.ProjectsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

val projectsModule = module {
    viewModelOf(::ProjectsViewModel)

    navigation<ProjectsRoute> { route ->
        ProjectsScreen(viewModel = koinViewModel())
    }
}
