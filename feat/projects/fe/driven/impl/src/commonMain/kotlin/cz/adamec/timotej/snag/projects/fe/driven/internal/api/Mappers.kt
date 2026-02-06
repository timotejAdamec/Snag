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

package cz.adamec.timotej.snag.projects.fe.driven.internal.api

import cz.adamec.timotej.snag.projects.be.driving.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.be.driving.contract.PutProjectApiDto
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject

internal fun ProjectApiDto.toModel() =
    FrontendProject(
        project = Project(
            id = id,
            name = name,
            address = address,
            updatedAt = updatedAt,
        ),
    )

internal fun FrontendProject.toPutApiDto() =
    PutProjectApiDto(
        name = project.name,
        address = project.address,
        updatedAt = project.updatedAt,
    )
