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

package cz.adamec.timotej.snag.featShared.database.be.driven.test.di

import cz.adamec.timotej.snag.featShared.database.be.driven.test.internal.TestDatabaseCleaner
import cz.adamec.timotej.snag.featShared.database.be.driven.test.internal.TestDatabaseFactory
import cz.adamec.timotej.snag.featShared.database.be.driven.test.internal.TestSchemaInitializer
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.dsl.module

val sharedDatabaseTestModule =
    module {
        single<Database> {
            TestDatabaseFactory.create().also { database ->
                TestSchemaInitializer.ensureCreated(database)
                TestDatabaseCleaner.cleanAll(database)
            }
        }
    }
