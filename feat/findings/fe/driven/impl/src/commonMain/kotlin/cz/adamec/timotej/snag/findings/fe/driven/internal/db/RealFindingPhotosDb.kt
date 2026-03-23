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

package cz.adamec.timotej.snag.findings.fe.driven.internal.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhotoData
import cz.adamec.timotej.snag.feat.shared.database.fe.db.FindingPhotoEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.FindingPhotoEntityQueries
import cz.adamec.timotej.snag.findings.fe.driven.internal.LH
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotosDb
import cz.adamec.timotej.snag.lib.database.fe.safeDbWrite
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

internal class RealFindingPhotosDb(
    private val findingPhotoEntityQueries: FindingPhotoEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : FindingPhotosDb {
    override fun getPhotosFlow(findingId: Uuid): Flow<OfflineFirstDataResult<List<AppFindingPhoto>>> =
        findingPhotoEntityQueries
            .selectByFindingId(findingId.toString())
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<FindingPhotoEntity>, OfflineFirstDataResult<List<AppFindingPhoto>>> { entities ->
                OfflineFirstDataResult.Success(
                    entities.map { it.toModel() },
                )
            }.catch { e ->
                LH.logger.e { "Error loading photos for finding $findingId from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override fun getPhotoFlow(id: Uuid): Flow<OfflineFirstDataResult<AppFindingPhoto?>> =
        findingPhotoEntityQueries
            .selectById(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map<FindingPhotoEntity?, OfflineFirstDataResult<AppFindingPhoto?>> { entity ->
                OfflineFirstDataResult.Success(entity?.toModel())
            }.catch { e ->
                LH.logger.e { "Error loading photo $id from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun savePhoto(photo: AppFindingPhoto): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error saving photo ${photo.id} to DB.") {
            findingPhotoEntityQueries.save(
                FindingPhotoEntity(
                    id = photo.id.toString(),
                    findingId = photo.findingId.toString(),
                    url = photo.url,
                    updatedAt = photo.updatedAt.value,
                ),
            )
        }

    override suspend fun deletePhoto(id: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error deleting photo $id from DB.") {
            findingPhotoEntityQueries.deleteById(id.toString())
        }

    override suspend fun deletePhotosByFindingId(findingId: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error deleting photos for finding $findingId from DB.") {
            findingPhotoEntityQueries.deleteByFindingId(findingId.toString())
        }
}

private fun FindingPhotoEntity.toModel() =
    AppFindingPhotoData(
        id = Uuid.parse(id),
        findingId = Uuid.parse(findingId),
        url = url,
        updatedAt = Timestamp(updatedAt),
    )
