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

package cz.adamec.timotej.snag.projects.be.app.impl.internal

import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.projects.be.app.api.model.DeleteProjectRequest
import cz.adamec.timotej.snag.projects.be.driven.test.FakeProjectsLocalDataSource
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.business.Project
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class DeleteProjectUseCaseImplTest {
    private val dataSource = FakeProjectsLocalDataSource()
    private val useCase = DeleteProjectUseCaseImpl(dataSource)

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val project =
        BackendProject(
            project = Project(
                id = projectId,
                name = "Test Project",
                address = "Test Address",
                updatedAt = Timestamp(10L),
            ),
        )

    @Test
    fun `deletes project from storage`() =
        runTest {
            dataSource.setProject(project)

            useCase(DeleteProjectRequest(projectId = projectId, deletedAt = Timestamp(20L)))

            assertNull(dataSource.getProject(projectId))
        }

    @Test
    fun `does not delete project when saved updated at is later than deleted at`() =
        runTest {
            dataSource.setProject(project)

            useCase(
                DeleteProjectRequest(
                    projectId = projectId,
                    deletedAt = Timestamp(value = 1L),
                ),
            )

            assertNotNull(dataSource.getProject(projectId))
        }

    @Test
    fun `returns saved project when saved updated at is later than deleted at`() =
        runTest {
            dataSource.setProject(project)

            val result = useCase(
                DeleteProjectRequest(
                    projectId = projectId,
                    deletedAt = Timestamp(value = 1L),
                ),
            )

            assertNotNull(result)
            assertEquals(project, result)
        }

    @Test
    fun `returns null if no project was saved`() =
        runTest {
            val result = useCase(
                DeleteProjectRequest(
                    projectId = projectId,
                    deletedAt = Timestamp(value = 20L),
                ),
            )

            assertNull(result)
        }
}
