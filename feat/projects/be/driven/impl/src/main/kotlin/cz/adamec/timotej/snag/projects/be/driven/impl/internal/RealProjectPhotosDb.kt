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

package cz.adamec.timotej.snag.projects.be.driven.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.featuresShared.database.be.driven.api.ProjectEntity
import cz.adamec.timotej.snag.featuresShared.database.be.driven.api.ProjectPhotoEntity
import cz.adamec.timotej.snag.featuresShared.database.be.driven.api.ProjectPhotosTable
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhoto
import cz.adamec.timotej.snag.projects.be.ports.ProjectPhotosDb
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

internal class RealProjectPhotosDb(
    private val database: Database,
    private val resolveConflictForSave: ResolveConflictForSaveUseCase,
    private val resolveConflictForDelete: ResolveConflictForDeleteUseCase,
) : ProjectPhotosDb {
    override suspend fun savePhoto(photo: BackendProjectPhoto): BackendProjectPhoto? =
        transaction(database) {
            val existing = ProjectPhotoEntity.findById(photo.id)
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
                        existing.project = ProjectEntity[photo.projectId]
                        existing.url = photo.url
                        existing.description = photo.description
                        existing.updatedAt = photo.updatedAt.value
                        existing.deletedAt = photo.deletedAt?.value
                    } else {
                        ProjectPhotoEntity.new(photo.id) {
                            project = ProjectEntity[photo.projectId]
                            url = photo.url
                            description = photo.description
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
    ): BackendProjectPhoto? =
        transaction(database) {
            val existing = ProjectPhotoEntity.findById(id)
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
        projectId: Uuid,
        since: Timestamp,
    ): List<BackendProjectPhoto> =
        transaction(database) {
            @Suppress("UnnecessaryParentheses")
            ProjectPhotoEntity
                .find {
                    (ProjectPhotosTable.project eq projectId) and
                        (
                            (ProjectPhotosTable.updatedAt greater since.value) or
                                (ProjectPhotosTable.deletedAt greater since.value)
                        )
                }.map { it.toModel() }
        }
}
