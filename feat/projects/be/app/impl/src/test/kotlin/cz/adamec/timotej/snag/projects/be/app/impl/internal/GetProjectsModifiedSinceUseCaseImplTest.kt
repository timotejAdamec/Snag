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
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsModifiedSinceUseCase
import cz.adamec.timotej.snag.projects.be.driven.test.FakeProjectsLocalDataSource
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsLocalDataSource
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class GetProjectsModifiedSinceUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: FakeProjectsLocalDataSource by inject()
    private val useCase: GetProjectsModifiedSinceUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeProjectsLocalDataSource) bind ProjectsLocalDataSource::class
            },
        )

    @Test
    fun `returns empty list when no projects exist`() =
        runTest(testDispatcher) {
            val result = useCase(since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns projects with updatedAt after since`() =
        runTest(testDispatcher) {
            val project =
                BackendProject(
                    project = Project(
                        id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                        name = "Project 1",
                        address = "Address 1",
                        updatedAt = Timestamp(200L),
                    ),
                )
            dataSource.setProject(project)

            val result = useCase(since = Timestamp(100L))

            assertEquals(listOf(project), result)
        }

    @Test
    fun `excludes projects with updatedAt before since`() =
        runTest(testDispatcher) {
            val project =
                BackendProject(
                    project = Project(
                        id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                        name = "Project 1",
                        address = "Address 1",
                        updatedAt = Timestamp(50L),
                    ),
                )
            dataSource.setProject(project)

            val result = useCase(since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns deleted projects when deletedAt is after since`() =
        runTest(testDispatcher) {
            val project =
                BackendProject(
                    project = Project(
                        id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                        name = "Project 1",
                        address = "Address 1",
                        updatedAt = Timestamp(50L),
                    ),
                    deletedAt = Timestamp(200L),
                )
            dataSource.setProject(project)

            val result = useCase(since = Timestamp(100L))

            assertEquals(listOf(project), result)
        }

    @Test
    fun `excludes deleted projects when deletedAt is before since`() =
        runTest(testDispatcher) {
            val project =
                BackendProject(
                    project = Project(
                        id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                        name = "Project 1",
                        address = "Address 1",
                        updatedAt = Timestamp(50L),
                    ),
                    deletedAt = Timestamp(80L),
                )
            dataSource.setProject(project)

            val result = useCase(since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }
}
