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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import cz.adamec.timotej.snag.findings.fe.app.api.CascadeDeleteLocalFindingPhotosByFindingIdUseCase
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosDb
import kotlin.uuid.Uuid

internal class CascadeDeleteLocalFindingPhotosByFindingIdUseCaseImpl(
    private val findingPhotosDb: FindingPhotosDb,
) : CascadeDeleteLocalFindingPhotosByFindingIdUseCase {
    override suspend operator fun invoke(findingId: Uuid) {
        findingPhotosDb.deletePhotosByFindingId(findingId)
    }
}
