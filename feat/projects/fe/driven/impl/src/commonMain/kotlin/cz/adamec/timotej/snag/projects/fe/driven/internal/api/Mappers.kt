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

import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.be.driving.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.be.driving.contract.PutProjectApiDto

internal fun ProjectApiDto.toModel(): AppProject =
    AppProjectData(
        id = id,
        name = name,
        address = address,
        clientId = clientId,
        isClosed = isClosed,
        updatedAt = updatedAt,
    )

internal fun AppProject.toPutApiDto() =
    PutProjectApiDto(
        name = name,
        address = address,
        clientId = clientId,
        isClosed = isClosed,
        updatedAt = updatedAt,
    )
