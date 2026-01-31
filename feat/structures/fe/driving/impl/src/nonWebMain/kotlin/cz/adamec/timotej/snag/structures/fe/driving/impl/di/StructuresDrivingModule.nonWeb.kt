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

package cz.adamec.timotej.snag.structures.fe.driving.impl.di

import cz.adamec.timotej.snag.feat.structures.fe.driving.api.NonWebStructureCreationRoute
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.NonWebStructureDetailRoute
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.NonWebStructureEditRoute
import org.koin.dsl.module

internal actual val platformModule =
    module {
        structureCreateScreenNav<NonWebStructureCreationRoute>()
        structureDetailScreenNav<NonWebStructureDetailRoute>()
        structureEditScreenNavigation<NonWebStructureEditRoute>()
    }
