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

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.findings.fe.app.api.GetFindingPhotosUseCase
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.uuid.Uuid

internal class GetFindingPhotosUseCaseImpl(
    private val findingPhotosDb: FindingPhotosDb,
) : GetFindingPhotosUseCase {
    override operator fun invoke(findingId: Uuid): Flow<OfflineFirstDataResult<List<AppFindingPhoto>>> =
        findingPhotosDb
            .getPhotosFlow(findingId)
            .distinctUntilChanged()
}
