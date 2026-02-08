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

import cz.adamec.timotej.snag.feat.shared.database.be.ProjectEntity
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.business.Project

internal fun ProjectEntity.toModel() =
    BackendProject(
        project =
            Project(
                id = id.value,
                name = name,
                address = address,
                clientId = client?.id?.value,
                updatedAt = Timestamp(updatedAt),
            ),
        deletedAt = deletedAt?.let { Timestamp(it) },
    )
