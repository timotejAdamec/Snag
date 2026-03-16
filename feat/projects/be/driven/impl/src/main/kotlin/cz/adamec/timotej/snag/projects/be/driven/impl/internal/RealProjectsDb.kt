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

import cz.adamec.timotej.snag.feat.shared.database.be.ClientEntity
import cz.adamec.timotej.snag.feat.shared.database.be.ProjectEntity
import cz.adamec.timotej.snag.feat.shared.database.be.ProjectsTable
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.sync.be.DeleteConflictResult
import cz.adamec.timotej.snag.lib.sync.be.ResolveConflictForDeleteUseCase
import cz.adamec.timotej.snag.lib.sync.be.ResolveConflictForSaveUseCase
import cz.adamec.timotej.snag.lib.sync.be.SaveConflictResult
import cz.adamec.timotej.snag.lib.sync.be.model.ResolveConflictForDeleteRequest
import cz.adamec.timotej.snag.lib.sync.be.model.ResolveConflictForSaveRequest
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class RealProjectsDb(
    private val database: Database,
    private val resolveConflictForSave: ResolveConflictForSaveUseCase,
    private val resolveConflictForDelete: ResolveConflictForDeleteUseCase,
) : ProjectsDb {
    override suspend fun getProjects(): List<BackendProject> =
        transaction(database) {
            ProjectEntity.all().map { it.toModel() }
        }

    override suspend fun getProject(id: Uuid): BackendProject? =
        transaction(database) {
            ProjectEntity.findById(id)?.toModel()
        }

    override suspend fun saveProject(project: BackendProject): BackendProject? =
        transaction(database) {
            val existing = ProjectEntity.findById(project.project.id)
            when (
                val result =
                    resolveConflictForSave(
                        ResolveConflictForSaveRequest(
                            existing = existing?.toModel(),
                            incoming = project,
                        ),
                    )
            ) {
                is SaveConflictResult.Proceed -> {
                    if (existing != null) {
                        existing.name = project.project.name
                        existing.address = project.project.address
                        existing.client = project.project.clientId?.let { ClientEntity.findById(it) }
                        existing.isClosed = project.project.isClosed
                        existing.updatedAt = project.project.updatedAt.value
                        existing.deletedAt = project.deletedAt?.value
                    } else {
                        ProjectEntity.new(project.project.id) {
                            name = project.project.name
                            address = project.project.address
                            client = project.project.clientId?.let { ClientEntity.findById(it) }
                            isClosed = project.project.isClosed
                            updatedAt = project.project.updatedAt.value
                            deletedAt = project.deletedAt?.value
                        }
                    }
                    null
                }
                is SaveConflictResult.Rejected -> {
                    result.serverVersion
                }
            }
        }

    override suspend fun deleteProject(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendProject? =
        transaction(database) {
            val existing = ProjectEntity.findById(id)
            when (
                val result =
                    resolveConflictForDelete(
                        ResolveConflictForDeleteRequest(
                            existing = existing?.toModel(),
                            deletedAt = deletedAt,
                        ),
                    )
            ) {
                is DeleteConflictResult.Proceed -> {
                    existing!!.deletedAt = deletedAt.value
                    null
                }
                is DeleteConflictResult.NotFound -> {
                    null
                }
                is DeleteConflictResult.AlreadyDeleted -> {
                    null
                }
                is DeleteConflictResult.Rejected -> {
                    result.serverVersion
                }
            }
        }

    override suspend fun getProjectsModifiedSince(since: Timestamp): List<BackendProject> =
        transaction(database) {
            @Suppress("UnnecessaryParentheses")
            ProjectEntity
                .find {
                    (ProjectsTable.updatedAt greater since.value) or
                        (ProjectsTable.deletedAt greater since.value)
                }.map { it.toModel() }
        }
}
