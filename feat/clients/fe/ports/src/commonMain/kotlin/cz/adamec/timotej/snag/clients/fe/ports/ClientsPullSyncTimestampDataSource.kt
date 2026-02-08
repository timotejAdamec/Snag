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

package cz.adamec.timotej.snag.clients.fe.ports

import cz.adamec.timotej.snag.lib.core.common.Timestamp

interface ClientsPullSyncTimestampDataSource {
    suspend fun getLastSyncedAt(): Timestamp?

    suspend fun setLastSyncedAt(timestamp: Timestamp)
}
