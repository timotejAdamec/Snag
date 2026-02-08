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
import cz.adamec.timotej.snag.projects.be.app.api.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class SaveProjectUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: ProjectsDb by inject()
    private val useCase: SaveProjectUseCase by inject()

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
    fun `saves project to data source`() =
        runTest(testDispatcher) {
            useCase(project)

            val stored = dataSource.getProject(projectId)
            assertEquals(project, stored)
        }

    @Test
    fun `does not save project if saved updated at is later than the new one`() =
        runTest(testDispatcher) {
            val savedProject = project.copy(
                project = project.project.copy(
                    updatedAt = Timestamp(value = 20L),
                ),
            )
            dataSource.saveProject(savedProject)

            useCase(project)

            assertEquals(savedProject, dataSource.getProject(projectId))
        }

    @Test
    fun `returns null if project was not present`() =
        runTest(testDispatcher) {
            val result = useCase(project)

            assertNull(result)
        }

    @Test
    fun `returns saved project if saved updated at is later than the new one`() =
        runTest(testDispatcher) {
            val savedProject = project.copy(
                project = project.project.copy(
                    updatedAt = Timestamp(value = 20L),
                ),
            )
            dataSource.saveProject(savedProject)

            val result = useCase(project)

            assertEquals(savedProject, result)
        }

    @Test
    fun `returns null if saved updated at is earlier than the new one`() =
        runTest(testDispatcher) {
            dataSource.saveProject(project)

            val newerProject = project.copy(
                project = project.project.copy(
                    name = "New name",
                    updatedAt = Timestamp(value = 20L),
                ),
            )

            val result = useCase(newerProject)

            assertNull(result)
        }

    @Test
    fun `restores soft-deleted project when saved with newer updatedAt`() =
        runTest(testDispatcher) {
            val deletedProject = project.copy(deletedAt = Timestamp(15L))
            dataSource.saveProject(deletedProject)

            val restoredProject = project.copy(
                project = project.project.copy(
                    name = "Restored",
                    updatedAt = Timestamp(value = 20L),
                ),
            )

            val result = useCase(restoredProject)

            assertNull(result)
            val stored = dataSource.getProject(projectId)
            assertNotNull(stored)
            assertNull(stored.deletedAt)
            assertEquals("Restored", stored.project.name)
        }

    @Test
    fun `does not restore soft-deleted project when saved with older updatedAt`() =
        runTest(testDispatcher) {
            val deletedProject = project.copy(deletedAt = Timestamp(15L))
            dataSource.saveProject(deletedProject)

            val olderProject = project.copy(
                project = project.project.copy(
                    updatedAt = Timestamp(value = 5L),
                ),
            )

            val result = useCase(olderProject)

            assertNotNull(result)
            assertEquals(deletedProject, result)
        }
}
