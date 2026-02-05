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

import cz.adamec.timotej.snag.projects.be.driven.test.FakeProjectsLocalDataSource
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.business.Project
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class GetProjectUseCaseImplTest {
    private val dataSource = FakeProjectsLocalDataSource()
    private val useCase = GetProjectUseCaseImpl(dataSource)

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val project =
        BackendProject(
            project = Project(
                id = projectId,
                name = "Test Project",
                address = "Test Address",
            ),
        )

    @Test
    fun `returns project when it exists`() =
        runTest {
            dataSource.setProject(project)

            val result = useCase(projectId)

            assertEquals(project, result)
        }

    @Test
    fun `returns null when not found`() =
        runTest {
            val result = useCase(projectId)

            assertNull(result)
        }
}
