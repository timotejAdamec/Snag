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

import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntity
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import kotlin.uuid.Uuid

internal fun FrontendProject.toEntity() =
    ProjectEntity(
        id = project.id.toString(),
        name = project.name,
        address = project.address,
    )

internal fun ProjectEntity.toModel() =
    FrontendProject(
        project = Project(
            id = Uuid.parse(id),
            name = name,
            address = address,
        ),
    )
