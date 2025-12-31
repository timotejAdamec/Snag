package cz.adamec.timotej.snag.ui.di

import cz.adamec.timotej.snag.projects.fe.app.di.projectsAppModule
import cz.adamec.timotej.snag.projects.fe.driven.di.projectsDrivenModule
import cz.adamec.timotej.snag.projects.fe.driving.impl.di.projectsDrivingModule
import cz.adamec.timotej.snag.ui.navigation.navigationModule
import org.koin.dsl.module

val appModule = module {
    includes(
        navigationModule,
        projectsDrivingModule,
        projectsDrivenModule,
        projectsAppModule
    )
}
