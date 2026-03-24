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
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.findings.fe.app.api.DeleteFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.FINDING_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncDeleteUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.EnqueueSyncDeleteRequest
import kotlin.uuid.Uuid

internal class DeleteFindingPhotoUseCaseImpl(
    private val findingPhotosDb: FindingPhotosDb,
    private val enqueueSyncDeleteUseCase: EnqueueSyncDeleteUseCase,
) : DeleteFindingPhotoUseCase {
    override suspend operator fun invoke(photoId: Uuid): OfflineFirstDataResult<Unit> {
        enqueueSyncDeleteUseCase(
            EnqueueSyncDeleteRequest(
                entityTypeId = FINDING_PHOTO_SYNC_ENTITY_TYPE,
                entityId = photoId,
            ),
        )
        logger.d { "Enqueued sync delete for photo $photoId. Photo will be removed from local DB after successful sync." }
        return OfflineFirstDataResult.Success(Unit)
    }
}
