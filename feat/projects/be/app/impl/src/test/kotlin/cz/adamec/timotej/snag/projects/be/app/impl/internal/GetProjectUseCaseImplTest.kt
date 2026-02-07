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
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.be.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.projects.be.model.BackendProject
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class GetProjectUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: FakeProjectsDb by inject()
    private val useCase: GetProjectUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeProjectsDb) bind ProjectsDb::class
            },
        )

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
    fun `returns project when it exists`() =
        runTest(testDispatcher) {
            dataSource.setProject(project)

            val result = useCase(projectId)

            assertEquals(project, result)
        }

    @Test
    fun `returns null when not found`() =
        runTest(testDispatcher) {
            val result = useCase(projectId)

            assertNull(result)
        }
}
