package cz.adamec.timotej.snag.ui

import cz.adamec.timotej.snag.projects.fe.driving.impl.di.projectsModule
import org.koin.dsl.module

val appModule = module {
    includes(projectsModule)
}
