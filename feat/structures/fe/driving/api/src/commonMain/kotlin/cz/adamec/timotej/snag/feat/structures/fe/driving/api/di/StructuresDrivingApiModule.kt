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

package cz.adamec.timotej.snag.feat.structures.fe.driving.api.di

import org.koin.core.module.Module
import org.koin.dsl.module

val structuresDrivingApiModule =
    module {
        includes(platformModule)
    }

internal expect val platformModule: Module
