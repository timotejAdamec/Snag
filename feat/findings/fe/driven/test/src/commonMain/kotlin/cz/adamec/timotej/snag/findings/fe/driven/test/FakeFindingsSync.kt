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

import cz.adamec.timotej.snag.findings.fe.ports.FindingsSync
import kotlin.uuid.Uuid

class FakeFindingsSync : FindingsSync {
    val savedFindingIds = mutableListOf<Uuid>()
    val deletedFindingIds = mutableListOf<Uuid>()

    override suspend fun enqueueFindingSave(findingId: Uuid) {
        savedFindingIds.add(findingId)
    }

    override suspend fun enqueueFindingDelete(findingId: Uuid) {
        deletedFindingIds.add(findingId)
    }
}
