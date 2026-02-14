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

package cz.adamec.timotej.snag.testinfra.fe

import cz.adamec.timotej.snag.di.aggregate.fe.frontendModulesAggregate
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.inspectionsFeDrivenTestModule
import cz.adamec.timotej.snag.feat.shared.database.fe.test.sharedDatabaseFeTestModule
import cz.adamec.timotej.snag.testinfra.KoinInitializedTest
import org.koin.core.module.Module

abstract class FrontendKoinInitializedTest : KoinInitializedTest() {
    override fun koinModules(): List<Module> =
        listOf(
            frontendModulesAggregate,
            sharedDatabaseFeTestModule,
            inspectionsFeDrivenTestModule,
        )
}
