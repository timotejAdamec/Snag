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
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class RealFindingPhotosDb(
    private val database: Database,
) : FindingPhotosDb {
    override suspend fun savePhoto(photo: BackendFindingPhoto): BackendFindingPhoto? =
        transaction(database) {
            // Photos are immutable — if exists, reject (return existing)
            val existing = FindingPhotoEntity.findById(photo.id)
            if (existing != null) return@transaction existing.toModel()
            // Otherwise create new
            FindingPhotoEntity.new(photo.id) {
                finding = FindingEntity[photo.findingId]
                url = photo.url
                createdAt = photo.createdAt.value
                deletedAt = photo.deletedAt?.value
            }
            null
        }

    override suspend fun deletePhoto(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendFindingPhoto? =
        transaction(database) {
            val existing = FindingPhotoEntity.findById(id) ?: return@transaction null
            if (existing.deletedAt != null) return@transaction null
            existing.deletedAt = deletedAt.value
            null
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
                            (FindingPhotosTable.createdAt greater since.value) or
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
        createdAt = Timestamp(createdAt),
        deletedAt = deletedAt?.let { Timestamp(it) },
    )
