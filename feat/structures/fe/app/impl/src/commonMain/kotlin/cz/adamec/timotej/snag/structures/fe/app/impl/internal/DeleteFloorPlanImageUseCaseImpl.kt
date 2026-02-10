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

package cz.adamec.timotej.snag.structures.fe.app.impl.internal

import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.structures.fe.app.api.DeleteFloorPlanImageUseCase
import cz.adamec.timotej.snag.structures.fe.ports.StructuresFileStorage

class DeleteFloorPlanImageUseCaseImpl(
    private val structuresFileStorage: StructuresFileStorage,
) : DeleteFloorPlanImageUseCase {
    override suspend fun invoke(url: String): OnlineDataResult<Unit> = structuresFileStorage.deleteFile(url)
}
