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

package cz.adamec.timotej.snag.findings.be.app.api

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingPhoto
import cz.adamec.timotej.snag.findings.be.app.api.model.GetFindingPhotosModifiedSinceRequest

interface GetFindingPhotosModifiedSinceUseCase {
    suspend operator fun invoke(
        request: GetFindingPhotosModifiedSinceRequest,
    ): List<BackendFindingPhoto>
}
