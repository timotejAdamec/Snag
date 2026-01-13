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

package cz.adamec.timotej.snag.projects.be.app.di

import cz.adamec.timotej.snag.projects.be.app.GetProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.GetProjectsUseCase
import cz.adamec.timotej.snag.projects.be.app.SaveProjectUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val projectsAppModule =
    module {
        factoryOf(::GetProjectsUseCase)
        factoryOf(::GetProjectUseCase)
        factoryOf(::SaveProjectUseCase)
    }
