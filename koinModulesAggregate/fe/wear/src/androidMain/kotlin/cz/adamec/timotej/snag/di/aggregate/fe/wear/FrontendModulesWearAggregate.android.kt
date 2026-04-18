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

package cz.adamec.timotej.snag.di.aggregate.fe.wear

import cz.adamec.timotej.snag.authentication.fe.wear.driven.di.authenticationDrivenWearModule
import cz.adamec.timotej.snag.projects.fe.wear.driving.di.projectsDrivingWearModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformWearModule: Module =
    module {
        includes(
            authenticationDrivenWearModule,
            projectsDrivingWearModule,
        )
    }
