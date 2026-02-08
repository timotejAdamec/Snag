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

package cz.adamec.timotej.snag.lib.database.be.di

import cz.adamec.timotej.snag.lib.database.be.internal.DatabaseFactory
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.dsl.module

val backendDatabaseModule =
    module {
        single<Database> { DatabaseFactory.create() }
    }
