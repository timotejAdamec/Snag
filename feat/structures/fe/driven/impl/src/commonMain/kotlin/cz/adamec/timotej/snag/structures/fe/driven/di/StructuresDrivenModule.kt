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

package cz.adamec.timotej.snag.structures.fe.driven.di

import cz.adamec.timotej.snag.core.foundation.common.di.getIoDispatcher
import cz.adamec.timotej.snag.structures.fe.driven.internal.api.RealStructuresApi
import cz.adamec.timotej.snag.structures.fe.driven.internal.db.RealStructuresDb
import cz.adamec.timotej.snag.structures.fe.driven.internal.db.StructuresSqlDelightDbOps
import cz.adamec.timotej.snag.structures.fe.ports.StructuresApi
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val structuresDrivenModule =
    module {
        factory {
            StructuresSqlDelightDbOps(
                queries = get(),
                ioDispatcher = getIoDispatcher(),
            )
        }
        factory { RealStructuresDb(ops = get()) } bind StructuresDb::class
        factoryOf(::RealStructuresApi) bind StructuresApi::class
    }
