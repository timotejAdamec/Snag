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

package cz.adamec.timotej.snag.projects.fe.driven.internal.sync

import cz.adamec.timotej.snag.lib.sync.fe.app.api.SyncCoordinator
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsPullSyncCoordinator

internal class RealProjectsPullSyncCoordinator(
    private val syncCoordinator: SyncCoordinator,
) : ProjectsPullSyncCoordinator {
    override suspend fun <T> withFlushedQueue(block: suspend () -> T): T = syncCoordinator.withFlushedQueue(block)
}
