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

package cz.adamec.timotej.snag.projects.fe.app.api

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.fe.app.api.model.SaveProjectRequest
import kotlin.uuid.Uuid

interface SaveProjectUseCase {
    suspend operator fun invoke(request: SaveProjectRequest): OfflineFirstDataResult<Uuid>
}
