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
import cz.adamec.timotej.snag.feat.shared.database.be.ProjectsTable
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

internal class ExposedProjectsDb(
    private val database: Database,
) : ProjectsDb {
    override suspend fun getProjects(): List<BackendProject> =
        transaction(database) {
            ProjectEntity.all().map { it.toModel() }
        }

    override suspend fun getProject(id: Uuid): BackendProject? =
        transaction(database) {
            ProjectEntity.findById(id)?.toModel()
        }

    @Suppress("ReturnCount", "LabeledExpression")
    override suspend fun saveProject(project: BackendProject): BackendProject? =
        transaction(database) {
            val existing = ProjectEntity.findById(project.project.id)

            if (existing != null) {
                val serverTimestamp =
                    maxOf(
                        Timestamp(existing.updatedAt),
                        existing.deletedAt?.let { Timestamp(it) } ?: Timestamp(0),
                    )
                if (serverTimestamp >= project.project.updatedAt) {
                    return@transaction existing.toModel()
                }
                existing.name = project.project.name
                existing.address = project.project.address
                existing.updatedAt = project.project.updatedAt.value
                existing.deletedAt = project.deletedAt?.value
            } else {
                ProjectEntity.new(project.project.id) {
                    name = project.project.name
                    address = project.project.address
                    updatedAt = project.project.updatedAt.value
                    deletedAt = project.deletedAt?.value
                }
            }
            null
        }

    @Suppress("ReturnCount", "LabeledExpression")
    override suspend fun deleteProject(
        id: Uuid,
        deletedAt: Timestamp,
    ): BackendProject? =
        transaction(database) {
            val existing =
                ProjectEntity.findById(id)
                    ?: return@transaction null

            if (existing.deletedAt != null) return@transaction null
            if (Timestamp(existing.updatedAt) >= deletedAt) {
                return@transaction existing.toModel()
            }

            existing.deletedAt = deletedAt.value
            null
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
