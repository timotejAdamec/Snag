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

package cz.adamec.timotej.snag.projects.fe.app.impl.di

import cz.adamec.timotej.snag.projects.fe.app.api.DeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.RealDeleteProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.RealGetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.RealGetProjectsUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.RealSaveProjectUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val projectsAppModule =
    module {
        factoryOf(::RealGetProjectsUseCase) bind GetProjectsUseCase::class
        factoryOf(::RealGetProjectUseCase) bind GetProjectUseCase::class
        factoryOf(::RealSaveProjectUseCase) bind SaveProjectUseCase::class
        factoryOf(::RealDeleteProjectUseCase) bind DeleteProjectUseCase::class
    }
