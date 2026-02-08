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
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert

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
            ProjectsTable.selectAll().map { it.toBackendProject() }
        }

    override suspend fun getProject(id: Uuid): BackendProject? =
        transaction(database) {
            ProjectsTable
                .selectAll()
                .where { ProjectsTable.id eq id.toJavaUuid() }
                .map { it.toBackendProject() }
                .singleOrNull()
        }

    @Suppress("ReturnCount")
    override suspend fun updateProject(project: BackendProject): BackendProject? =
        transaction(database) {
            val existing =
                ProjectsTable
                    .selectAll()
                    .where { ProjectsTable.id eq project.project.id.toJavaUuid() }
                    .map { it.toBackendProject() }
                    .singleOrNull()

            if (existing != null) {
                val serverTimestamp =
                    maxOf(
                        existing.project.updatedAt,
                        existing.deletedAt ?: Timestamp(0),
                    )
                if (serverTimestamp >= project.project.updatedAt) {
                    return@transaction existing
                }
            }

            ProjectsTable.upsert {
                it[id] = project.project.id.toJavaUuid()
                it[name] = project.project.name
                it[address] = project.project.address
                it[updatedAt] = project.project.updatedAt.value
                it[deletedAt] = project.deletedAt?.value
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
                ProjectsTable
                    .selectAll()
                    .where { ProjectsTable.id eq id.toJavaUuid() }
                    .map { it.toBackendProject() }
                    .singleOrNull()
                    ?: return@transaction null

            if (existing.deletedAt != null) return@transaction null
            if (existing.project.updatedAt >= deletedAt) return@transaction existing

            ProjectsTable.update({ ProjectsTable.id eq id.toJavaUuid() }) {
                it[ProjectsTable.deletedAt] = deletedAt.value
            }
            null
        }

    override suspend fun getProjectsModifiedSince(since: Timestamp): List<BackendProject> =
        transaction(database) {
            ProjectsTable
                .selectAll()
                .where {
                    (ProjectsTable.updatedAt greater since.value) or
                        (ProjectsTable.deletedAt greater since.value)
                }
                .map { it.toBackendProject() }
        }

    private fun ResultRow.toBackendProject(): BackendProject =
        BackendProject(
            project =
                Project(
                    id = this[ProjectsTable.id].value.toKotlinUuid(),
                    name = this[ProjectsTable.name],
                    address = this[ProjectsTable.address],
                    updatedAt = Timestamp(this[ProjectsTable.updatedAt]),
                ),
            deletedAt = this[ProjectsTable.deletedAt]?.let { Timestamp(it) },
        )
}
