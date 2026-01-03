package cz.adamec.timotej.snag.projects.fe.driving.impl.di

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.adamec.timotej.snag.projects.fe.driving.api.ProjectsRoute
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.ui.ProjectsScreen
import cz.adamec.timotej.snag.projects.fe.driving.impl.internal.vm.ProjectsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.scope.Scope
import org.koin.dsl.module

@Composable
fun Scope.projectsScreenInjection() {
    ProjectsScreen(
        modifier = Modifier.fillMaxSize(),
        viewModel = koinViewModel(),
        onProjectClick = get(),
    )
}

val projectsDrivingImplModule = module {
    viewModelOf(::ProjectsViewModel)
    includes(platformModule)
}

internal expect val platformModule: Module
