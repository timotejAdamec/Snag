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

package cz.adamec.timotej.snag.findings.be.driven.impl.di

import cz.adamec.timotej.snag.findings.be.driven.impl.internal.InMemoryFindingsLocalDataSource
import cz.adamec.timotej.snag.findings.be.ports.FindingsLocalDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val findingsDrivenModule =
    module {
        singleOf(::InMemoryFindingsLocalDataSource) bind FindingsLocalDataSource::class
    }
