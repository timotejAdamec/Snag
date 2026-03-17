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

package cz.adamec.timotej.snag.clients.fe.driven.di

import cz.adamec.timotej.snag.clients.fe.driven.internal.api.RealClientsApi
import cz.adamec.timotej.snag.clients.fe.driven.internal.db.ClientsSqlDelightDbOps
import cz.adamec.timotej.snag.clients.fe.driven.internal.db.RealClientsDb
import cz.adamec.timotej.snag.clients.fe.ports.ClientsApi
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.core.foundation.common.di.getIoDispatcher
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val clientsDrivenModule =
    module {
        factory {
            ClientsSqlDelightDbOps(
                queries = get(),
                ioDispatcher = getIoDispatcher(),
            )
        }
        factory { RealClientsDb(ops = get()) } bind ClientsDb::class
        factoryOf(::RealClientsApi) bind ClientsApi::class
    }
