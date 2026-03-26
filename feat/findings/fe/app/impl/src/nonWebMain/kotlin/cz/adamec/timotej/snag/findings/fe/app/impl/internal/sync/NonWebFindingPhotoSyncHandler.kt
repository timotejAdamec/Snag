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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.storage.fe.LocalFileStorage
import cz.adamec.timotej.snag.core.storage.fe.RemoteFileStorage
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhotoData
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosApi
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosDb
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructureUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.handler.DbApiPushSyncHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

internal class NonWebFindingPhotoSyncHandler(
    private val findingPhotosApi: FindingPhotosApi,
    private val findingPhotosDb: FindingPhotosDb,
    private val localFileStorage: LocalFileStorage,
    private val remoteFileStorage: RemoteFileStorage,
    private val findingsDb: FindingsDb,
    private val getStructureUseCase: GetStructureUseCase,
    timestampProvider: TimestampProvider,
) : DbApiPushSyncHandler<AppFindingPhoto>(LH.logger, timestampProvider) {
    override val entityTypeId: String = FINDING_PHOTO_SYNC_ENTITY_TYPE
    override val entityName: String = "finding_photo"

    override fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<AppFindingPhoto?>> =
        findingPhotosDb.getPhotoFlow(entityId)

    override suspend fun saveEntityToDb(entity: AppFindingPhoto): OfflineFirstDataResult<Unit> =
        findingPhotosDb.savePhoto(entity)

    override suspend fun deleteEntityFromApi(
        entityId: Uuid,
        deletedAt: Timestamp,
        scopeId: Uuid?,
    ): OnlineDataResult<AppFindingPhoto?> =
        findingPhotosApi.deletePhoto(
            id = entityId,
            findingId = scopeId!!,
            deletedAt = deletedAt,
        )

    @Suppress("ReturnCount")
    override suspend fun saveEntityToApi(entity: AppFindingPhoto): OnlineDataResult<AppFindingPhoto?> {
        if (entity.url.startsWith("http")) {
            return findingPhotosApi.savePhoto(entity)
        }

        val bytes = localFileStorage.readFileBytes(entity.url)
        val fileName = entity.url.substringAfterLast("/")
        val projectId =
            resolveProjectId(entity.findingId)
                ?: return OnlineDataResult.Failure.NetworkUnavailable

        val uploadResult =
            remoteFileStorage.uploadFile(
                bytes = bytes,
                fileName = fileName,
                directory = "projects/$projectId/findings/${entity.findingId}/photos",
            )

        return when (uploadResult) {
            is OnlineDataResult.Failure -> uploadResult
            is OnlineDataResult.Success -> {
                localFileStorage.deleteFile(entity.url)
                val updatedPhoto =
                    AppFindingPhotoData(
                        id = entity.id,
                        findingId = entity.findingId,
                        url = uploadResult.data,
                        createdAt = entity.createdAt,
                    )
                findingPhotosDb.savePhoto(updatedPhoto)
                findingPhotosApi.savePhoto(updatedPhoto)
            }
        }
    }

    private suspend fun resolveProjectId(findingId: Uuid): Uuid? {
        val findingResult = findingsDb.getFindingFlow(findingId).first()
        val finding =
            when (findingResult) {
                is OfflineFirstDataResult.Success -> findingResult.data ?: return null
                is OfflineFirstDataResult.ProgrammerError -> return null
            }

        val structureResult = getStructureUseCase(finding.structureId).first()
        val structure =
            when (structureResult) {
                is OfflineFirstDataResult.Success -> structureResult.data ?: return null
                is OfflineFirstDataResult.ProgrammerError -> return null
            }

        return structure.projectId
    }
}
