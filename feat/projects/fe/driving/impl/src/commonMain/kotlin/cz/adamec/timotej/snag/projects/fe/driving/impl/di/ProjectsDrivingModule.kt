package cz.adamec.timotej.snag.projects.fe.driving.impl.di

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.ui.ProjectsScreen
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.vm.ProjectsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

val projectsDrivingModule = module {
    viewModelOf(::ProjectsViewModel)

    navigation<ProjectsRoute> { _ ->
        ProjectsScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = koinViewModel(),
            onProjectClick = get(),
        )
    }
}
