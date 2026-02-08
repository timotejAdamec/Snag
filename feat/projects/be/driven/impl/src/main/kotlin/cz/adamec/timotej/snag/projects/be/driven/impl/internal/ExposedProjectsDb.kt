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

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.Project
import kotlin.uuid.Uuid
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal class ExposedProjectsDb(
    private val database: Database,
) : ProjectsDb {
    init {
        transaction(database) {
            SchemaUtils.create(ProjectsTable)
        }
    }

    override suspend fun getProjects(): List<BackendProject> =
        transaction(database) {
            ProjectEntity.all().map { it.toBackendProject() }
        }

    override suspend fun getProject(id: Uuid): BackendProject? =
        transaction(database) {
            ProjectEntity.findById(id)?.toBackendProject()
        }

    @Suppress("ReturnCount")
    override suspend fun updateProject(project: BackendProject): BackendProject? =
        transaction(database) {
            val existing = ProjectEntity.findById(project.project.id)

            if (existing != null) {
                val serverTimestamp =
                    maxOf(
                        Timestamp(existing.updatedAt),
                        existing.deletedAt?.let { Timestamp(it) } ?: Timestamp(0),
                    )
                if (serverTimestamp >= project.project.updatedAt) {
                    return@transaction existing.toBackendProject()
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

    @Suppress("ReturnCount")
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
                return@transaction existing.toBackendProject()
            }

            existing.deletedAt = deletedAt.value
            null
        }

    override suspend fun getProjectsModifiedSince(since: Timestamp): List<BackendProject> =
        transaction(database) {
            ProjectEntity.find {
                (ProjectsTable.updatedAt greater since.value) or
                    (ProjectsTable.deletedAt greater since.value)
            }.map { it.toBackendProject() }
        }

    private fun ProjectEntity.toBackendProject(): BackendProject =
        BackendProject(
            project =
                Project(
                    id = id.value,
                    name = name,
                    address = address,
                    updatedAt = Timestamp(updatedAt),
                ),
            deletedAt = deletedAt?.let { Timestamp(it) },
        )
}
