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

package cz.adamec.timotej.snag.projects.fe.driven.test

import cz.adamec.timotej.snag.projects.fe.ports.ProjectsSync
import kotlin.uuid.Uuid

class FakeProjectsSync : ProjectsSync {
    val savedProjectIds = mutableListOf<Uuid>()
    val deletedProjectIds = mutableListOf<Uuid>()

    override suspend fun enqueueProjectSave(projectId: Uuid) {
        savedProjectIds.add(projectId)
    }

    override suspend fun enqueueProjectDelete(projectId: Uuid) {
        deletedProjectIds.add(projectId)
    }
}
