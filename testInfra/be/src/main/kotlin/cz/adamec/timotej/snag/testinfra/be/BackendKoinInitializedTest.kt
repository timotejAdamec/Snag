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

package cz.adamec.timotej.snag.testinfra.be

import cz.adamec.timotej.snag.di.aggregate.be.backendModulesAggregate
import cz.adamec.timotej.snag.feat.shared.database.be.di.sharedDatabaseTestModule
import cz.adamec.timotej.snag.lib.storage.be.test.di.fakeStorageTestModule
import cz.adamec.timotej.snag.testinfra.KoinInitializedTest
import org.koin.core.module.Module

abstract class BackendKoinInitializedTest : KoinInitializedTest() {
    override fun koinModules(): List<Module> =
        listOf(
            backendModulesAggregate,
            sharedDatabaseTestModule,
            fakeStorageTestModule,
        )
}
