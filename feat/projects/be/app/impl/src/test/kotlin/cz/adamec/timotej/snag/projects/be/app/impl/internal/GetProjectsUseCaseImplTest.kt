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
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectsUseCase
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
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class GetProjectsUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: FakeProjectsLocalDataSource by inject()
    private val useCase: GetProjectsUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeProjectsLocalDataSource) bind ProjectsLocalDataSource::class
            },
        )

    @Test
    fun `returns empty list when none exist`() =
        runTest(testDispatcher) {
            val result = useCase()

            assertEquals(emptyList(), result)
        }

    @Test
    fun `returns all projects`() =
        runTest(testDispatcher) {
            val project1 =
                BackendProject(
                    project = Project(
                        id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                        name = "Project 1",
                        address = "Address 1",
                        updatedAt = Timestamp(10L),
                    ),
                )
            val project2 =
                BackendProject(
                    project = Project(
                        id = Uuid.parse("00000000-0000-0000-0000-000000000002"),
                        name = "Project 2",
                        address = "Address 2",
                        updatedAt = Timestamp(10L),
                    ),
                )
            dataSource.setProject(project1)
            dataSource.setProject(project2)

            val result = useCase()

            assertEquals(listOf(project1, project2), result)
        }
}
