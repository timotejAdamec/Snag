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

package cz.adamec.timotej.snag.findings.fe.app.api

import cz.adamec.timotej.snag.findings.fe.app.api.model.SaveNewFindingRequest
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import kotlin.uuid.Uuid

interface SaveNewFindingUseCase {
    suspend operator fun invoke(request: SaveNewFindingRequest): OfflineFirstDataResult<Uuid>
}
