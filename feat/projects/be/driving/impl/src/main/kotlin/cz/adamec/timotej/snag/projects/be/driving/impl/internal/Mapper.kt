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
import cz.adamec.timotej.snag.projects.business.Project
import kotlin.uuid.Uuid

internal fun Project.toDto() = ProjectApiDto(
    id = id,
    name = name,
    address = address,
)

internal fun PutProjectApiDto.toBusiness(id: Uuid) = Project(
    id = id,
    name = name,
    address = address,
)
