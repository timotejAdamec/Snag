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

package cz.adamec.timotej.snag.projects.be.driving.impl.internal

import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhoto
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhotoData
import cz.adamec.timotej.snag.projects.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.contract.ProjectPhotoApiDto
import cz.adamec.timotej.snag.projects.contract.PutProjectApiDto
import cz.adamec.timotej.snag.projects.contract.PutProjectPhotoApiDto
import kotlin.uuid.Uuid

internal fun BackendProject.toDto() =
    ProjectApiDto(
        id = id,
        name = name,
        address = address,
        clientId = clientId,
        creatorId = creatorId,
        isClosed = isClosed,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

internal fun PutProjectApiDto.toModel(id: Uuid): BackendProject =
    BackendProjectData(
        id = id,
        name = name,
        address = address,
        clientId = clientId,
        creatorId = creatorId,
        isClosed = isClosed,
        updatedAt = updatedAt,
    )

internal fun BackendProjectPhoto.toPhotoDto() =
    ProjectPhotoApiDto(
        id = id,
        projectId = projectId,
        url = url,
        description = description,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

internal fun PutProjectPhotoApiDto.toModel(
    id: Uuid,
    projectId: Uuid,
): BackendProjectPhoto =
    BackendProjectPhotoData(
        id = id,
        projectId = projectId,
        url = url,
        description = description,
        updatedAt = updatedAt,
    )
