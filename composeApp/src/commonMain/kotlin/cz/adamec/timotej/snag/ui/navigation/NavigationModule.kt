package cz.adamec.timotej.snag.ui.navigation

import cz.adamec.timotej.snag.projects.fe.driving.api.OnProjectClick
import org.koin.dsl.bind
import org.koin.dsl.module

internal val navigationModule = module {
    single {
        object : OnProjectClick {
            override fun invoke(projectId: Int) {
                TODO("Not yet implemented")
            }
        }
    } bind OnProjectClick::class
}
