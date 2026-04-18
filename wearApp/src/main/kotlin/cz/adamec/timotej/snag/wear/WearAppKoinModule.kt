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

package cz.adamec.timotej.snag.wear

import cz.adamec.timotej.snag.di.aggregate.fe.common.frontendModulesCommonAggregate
import cz.adamec.timotej.snag.di.aggregate.fe.wear.frontendModulesWearAggregate
import org.koin.dsl.module

val wearAppModule =
    module {
        includes(
            frontendModulesCommonAggregate,
            frontendModulesWearAggregate,
        )
    }
