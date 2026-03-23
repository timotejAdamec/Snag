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

package cz.adamec.timotej.snag.findings.be.driven.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingPhoto
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingPhotoData
import cz.adamec.timotej.snag.feat.shared.database.be.FindingEntity
import cz.adamec.timotej.snag.feat.shared.database.be.FindingPhotoEntity
import cz.adamec.timotej.snag.feat.shared.database.be.FindingPhotosTable
import cz.adamec.timotej.snag.findings.be.ports.FindingPhotosDb
import cz.adamec.timotej.snag.sync.be.DeleteConflictResult
import cz.adamec.timotej.snag.sync.be.ResolveConflictForDeleteUseCase
import cz.adamec.timotej.snag.sync.be.ResolveConflictForSaveUseCase
import cz.adamec.timotej.snag.sync.be.SaveConflictResult
import cz.adamec.timotej.snag.sync.be.model.ResolveConflictForDeleteRequest
import cz.adamec.timotej.snag.sync.be.model.ResolveConflictForSaveRequest
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class RealFindingPhotosDb(
    private val database: Database,
    private val resolveConflictForSave: ResolveConflictForSaveUseCase,
    private val resolveConflictForDelete: ResolveConflictForDeleteUseCase,
) : FindingPhotosDb {
    override suspend fun savePhoto(photo: BackendFindingPhoto): BackendFindingPhoto? =
        transaction(database) {
            val existing = FindingPhotoEntity.findById(photo.id)
            when (
                val result =
                    resolveConflictForSave(
                        ResolveConflictForSaveRequest(
                            existing = existing?.toModel(),
                            incoming = photo,
                        ),
                    )
            ) {
                is SaveConflictResult.Proceed -> {
                    if (existing != null) {
                        existing.finding = FindingEntity[photo.findingId]
                        existing.url = photo.url
                        existing.updatedAt = photo.updatedAt.value
                        existing.deletedAt = photo.deletedAt?.value
                    } else {
                        FindingPhotoEntity.new(photo.id) {
                            finding = FindingEntity[photo.findingId]
                            url = photo.url
                            updatedAt = photo.updatedAt.value
                            deletedAt = photo.deletedAt?.value
                        }
                    }
                    null
                }
                is SaveConflictResult.Rejected -> {
                    result.serverVersion
                }
            }
        }

    override suspend fun deletePhoto(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendFindingPhoto? =
        transaction(database) {
            val existing = FindingPhotoEntity.findById(id)
            when (
                val result =
                    resolveConflictForDelete(
                        ResolveConflictForDeleteRequest(
                            existing = existing?.toModel(),
                            deletedAt = deletedAt,
                        ),
                    )
            ) {
                is DeleteConflictResult.Proceed -> {
                    existing!!.deletedAt = deletedAt.value
                    null
                }
                is DeleteConflictResult.NotFound -> {
                    null
                }
                is DeleteConflictResult.AlreadyDeleted -> {
                    null
                }
                is DeleteConflictResult.Rejected -> {
                    result.serverVersion
                }
            }
        }

    override suspend fun getPhotosModifiedSince(
        findingId: Uuid,
        since: Timestamp,
    ): List<BackendFindingPhoto> =
        transaction(database) {
            @Suppress("UnnecessaryParentheses")
            FindingPhotoEntity
                .find {
                    (FindingPhotosTable.finding eq findingId) and
                        (
                            (FindingPhotosTable.updatedAt greater since.value) or
                                (FindingPhotosTable.deletedAt greater since.value)
                        )
                }.map { it.toModel() }
        }
}

internal fun FindingPhotoEntity.toModel(): BackendFindingPhoto =
    BackendFindingPhotoData(
        id = id.value,
        findingId = finding.id.value,
        url = url,
        updatedAt = Timestamp(updatedAt),
        deletedAt = deletedAt?.let { Timestamp(it) },
    )
