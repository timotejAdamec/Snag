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

package cz.adamec.timotej.snag.sync.fe.driven.test

import cz.adamec.timotej.snag.sync.fe.ports.PullSyncTimestampDb
import cz.adamec.timotej.snag.sync.fe.ports.SyncQueue
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val syncFeDrivenTestModule =
    module {
        singleOf(::FakeSyncQueue) bind SyncQueue::class
        singleOf(::FakePullSyncTimestampDb) bind PullSyncTimestampDb::class
    }
