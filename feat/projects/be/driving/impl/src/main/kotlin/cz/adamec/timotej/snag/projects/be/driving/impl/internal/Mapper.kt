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

import cz.adamec.timotej.snag.projects.be.driving.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.be.driving.contract.PutProjectApiDto
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.business.Project
import kotlin.uuid.Uuid

internal fun BackendProject.toDto() =
    with(project) {
        ProjectApiDto(
            id = id,
            name = name,
            address = address,
            clientId = clientId,
            updatedAt = updatedAt,
            deletedAt = this@toDto.deletedAt,
        )
    }

internal fun PutProjectApiDto.toModel(id: Uuid) =
    BackendProject(
        project =
            Project(
                id = id,
                name = name,
                address = address,
                clientId = clientId,
                updatedAt = updatedAt,
            ),
    )
