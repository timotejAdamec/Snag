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
import cz.adamec.timotej.snag.projects.fe.driven.internal.ProjectStore
import cz.adamec.timotej.snag.projects.fe.driven.internal.ProjectStoreFactory
import cz.adamec.timotej.snag.projects.fe.driven.internal.ProjectsStore
import cz.adamec.timotej.snag.projects.fe.driven.internal.ProjectsStoreFactory
import cz.adamec.timotej.snag.projects.fe.driven.internal.StoreProjectsRepository
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.ProjectsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val projectsDrivenModule =
    module {
        factory {
            ProjectsDb(
                projectEntityQueries = get(),
                projectBookkeepingQueries = get(),
                timestampProvider = get(),
                ioDispatcher = getIoDispatcher(),
            )
        }
        factoryOf(::ProjectsApi)
        factory {
            ProjectStoreFactory(
                projectsApi = get(),
                projectsDb = get(),
            ).create()
        } bind ProjectStore::class
        factory {
            ProjectsStoreFactory(
                projectsApi = get(),
                projectsDb = get(),
            ).create()
        } bind ProjectsStore::class
        factoryOf(::StoreProjectsRepository) bind ProjectsRepository::class
    }
