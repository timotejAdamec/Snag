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

package cz.adamec.timotej.snag.projects.fe.driven.test

import cz.adamec.timotej.snag.projects.fe.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val projectsFeDrivenTestModule =
    module {
        singleOf(::FakeProjectsDb) bind ProjectsDb::class
        singleOf(::FakeProjectsApi) bind ProjectsApi::class
        singleOf(::FakeProjectAssignmentsDb) bind ProjectAssignmentsDb::class
        singleOf(::FakeProjectPhotosDb) bind ProjectPhotosDb::class
        singleOf(::FakeProjectPhotosApi) bind ProjectPhotosApi::class
    }
