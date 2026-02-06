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

package cz.adamec.timotej.snag.findings.fe.driven.test

import cz.adamec.timotej.snag.findings.fe.ports.FindingsPullSyncCoordinator

class FakeFindingsPullSyncCoordinator : FindingsPullSyncCoordinator {
    override suspend fun <T> withFlushedQueue(block: suspend () -> T): T = block()
}
