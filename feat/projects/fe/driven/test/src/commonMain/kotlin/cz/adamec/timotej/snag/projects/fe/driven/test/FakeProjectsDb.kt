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

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.database.fe.test.FakeDbOps
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

class FakeProjectsDb : ProjectsDb {
    private val ops = FakeDbOps<FrontendProject>(getId = { it.project.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    override fun getAllProjectsFlow(): Flow<OfflineFirstDataResult<List<FrontendProject>>> =
        ops.allItemsFlow()

    override fun getProjectFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendProject?>> =
        ops.itemByIdFlow(id)

    override suspend fun saveProject(project: FrontendProject): OfflineFirstDataResult<Unit> =
        ops.saveOneItem(project)

    override suspend fun saveProjects(projects: List<FrontendProject>): OfflineFirstDataResult<Unit> =
        ops.saveManyItems(projects)

    override suspend fun deleteProject(id: Uuid): OfflineFirstDataResult<Unit> =
        ops.deleteItem(id)

    fun setProject(project: FrontendProject) = ops.setItem(project)
}
