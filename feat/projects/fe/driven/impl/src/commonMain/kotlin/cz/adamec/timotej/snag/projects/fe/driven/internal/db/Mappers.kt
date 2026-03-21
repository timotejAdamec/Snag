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

package cz.adamec.timotej.snag.projects.fe.driven.internal.db

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntity
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import kotlin.uuid.Uuid

internal fun AppProject.toEntity() =
    ProjectEntity(
        id = id.toString(),
        name = name,
        address = address,
        clientId = clientId?.toString(),
        creatorId = creatorId.toString(),
        isClosed = if (isClosed) 1L else 0L,
        updatedAt = updatedAt.value,
    )

internal fun ProjectEntity.toModel(): AppProject =
    AppProjectData(
        id = Uuid.parse(id),
        name = name,
        address = address,
        clientId = clientId?.let { Uuid.parse(it) },
        creatorId = Uuid.parse(creatorId),
        isClosed = isClosed == 1L,
        updatedAt = Timestamp(updatedAt),
    )
