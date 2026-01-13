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

package cz.adamec.timotej.snag.projects.be.driving.impl.di

import cz.adamec.timotej.snag.projects.be.driving.impl.internal.ProjectsRoute
import cz.adamec.timotej.snag.routing.be.AppRoute
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val projectsDrivingModule =
    module {
        singleOf(::ProjectsRoute) bind AppRoute::class
    }
