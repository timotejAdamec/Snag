package cz.adamec.timotej.snag.di

import cz.adamec.timotej.snag.lib.database.fe.di.databaseModule
import cz.adamec.timotej.snag.projects.fe.app.di.projectsAppModule
import cz.adamec.timotej.snag.projects.fe.driven.di.projectsDrivenModule
import cz.adamec.timotej.snag.projects.fe.driving.api.di.projectsDrivingApiModule
import cz.adamec.timotej.snag.projects.fe.driving.impl.di.projectsDrivingImplModule
import cz.adamec.timotej.snag.logging.loggerModule
import cz.adamec.timotej.snag.ui.navigation.navigationModule
import org.koin.dsl.module

val appModule = module {
    includes(
        loggerModule,
        navigationModule,
        databaseModule,
        mainModule,
        projectsDrivingApiModule,
        projectsDrivingImplModule,
        projectsDrivenModule,
        projectsAppModule,
    )
}
