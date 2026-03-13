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

package cz.adamec.timotej.snag.users.be.driven.impl.di

import cz.adamec.timotej.snag.users.be.driven.impl.internal.RealUsersDb
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val usersDrivenModule =
    module {
        singleOf(::RealUsersDb) bind UsersDb::class
    }
