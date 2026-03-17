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

import cz.adamec.timotej.snag.core.foundation.common.di.getIoDispatcher
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.RealProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.ProjectsSqlDelightDbOps
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.RealProjectsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val projectsDrivenModule =
    module {
        factory {
            ProjectsSqlDelightDbOps(
                queries = get(),
                ioDispatcher = getIoDispatcher(),
            )
        }
        factory { RealProjectsDb(ops = get()) } bind ProjectsDb::class
        factoryOf(::RealProjectsApi) bind ProjectsApi::class
    }
