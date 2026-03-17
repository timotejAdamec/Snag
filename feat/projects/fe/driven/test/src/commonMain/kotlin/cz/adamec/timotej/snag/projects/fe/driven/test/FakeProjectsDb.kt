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

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.database.fe.test.FakeDbOps
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

class FakeProjectsDb : ProjectsDb {
    private val ops = FakeDbOps<AppProject>(getId = { it.id })

    var forcedFailure
        get() = ops.forcedFailure
        set(value) {
            ops.forcedFailure = value
        }

    override fun getAllProjectsFlow(): Flow<OfflineFirstDataResult<List<AppProject>>> = ops.allItemsFlow()

    override fun getProjectFlow(id: Uuid): Flow<OfflineFirstDataResult<AppProject?>> = ops.itemByIdFlow(id)

    override suspend fun getProject(id: Uuid): OfflineFirstDataResult<AppProject?> = OfflineFirstDataResult.Success(ops.items.value[id])

    override suspend fun saveProject(project: AppProject): OfflineFirstDataResult<Unit> = ops.saveOneItem(project)

    override suspend fun saveProjects(projects: List<AppProject>): OfflineFirstDataResult<Unit> = ops.saveManyItems(projects)

    override suspend fun deleteProject(id: Uuid): OfflineFirstDataResult<Unit> = ops.deleteItem(id)

    fun setProject(project: AppProject) = ops.setItem(project)
}
