package cz.adamec.timotej.snag.ui.navigation

import cz.adamec.timotej.snag.projects.fe.driving.api.OnProjectClick
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.uuid.Uuid

internal val navigationModule =
    module {
        single {
            object : OnProjectClick {
                override fun invoke(projectId: Uuid) {
                    // TODO
                }
            }
        } bind OnProjectClick::class
    }
