/*
 * Copyright (c) 2026 Timotej Adamec
 * SPDX-License-Identifier: MIT
 *
 * This file is part of the thesis:
 * "Multiplatform snagging system with code sharing maximisation"
 *
 * Czech Technical University in Prague
 * Faculty of Information Technology
 * Department of Software Engineering
 */

package cz.adamec.timotej.snag.projects.fe.driven.di

import cz.adamec.timotej.snag.lib.core.di.getIoDispatcher
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.ProjectsDb
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val projectsDrivenModule =
    module {
        factory {
            ProjectsDb(
                projectEntityQueries = get(),
                ioDispatcher = getIoDispatcher(),
            )
        }
        factoryOf(::ProjectsApi)
    }
