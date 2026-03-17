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

package cz.adamec.timotej.snag.users.fe.driven.di

import cz.adamec.timotej.snag.core.foundation.common.di.getIoDispatcher
import cz.adamec.timotej.snag.users.fe.driven.internal.api.RealUsersApi
import cz.adamec.timotej.snag.users.fe.driven.internal.db.RealUsersDb
import cz.adamec.timotej.snag.users.fe.driven.internal.db.UsersSqlDelightDbOps
import cz.adamec.timotej.snag.users.fe.ports.UsersApi
import cz.adamec.timotej.snag.users.fe.ports.UsersDb
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val usersDrivenModule =
    module {
        factory {
            UsersSqlDelightDbOps(
                queries = get(),
                ioDispatcher = getIoDispatcher(),
            )
        }
        factory { RealUsersDb(ops = get()) } bind UsersDb::class
        factoryOf(::RealUsersApi) bind UsersApi::class
    }
