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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetailsEdit.vm

import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.app.api.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsSync
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsSync
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectDetailsEditViewModelTest : FrontendKoinInitializedTest() {

    private val fakeProjectsDb: FakeProjectsDb by inject()

    private val getProjectUseCase: GetProjectUseCase by inject()
    private val saveProjectUseCase: SaveProjectUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeProjectsApi) bind ProjectsApi::class
                singleOf(::FakeProjectsDb) bind ProjectsDb::class
                singleOf(::FakeProjectsSync) bind ProjectsSync::class
            },
        )

    private fun createViewModel(projectId: Uuid? = null) =
        ProjectDetailsEditViewModel(
            projectId = projectId,
            getProjectUseCase = getProjectUseCase,
            saveProjectUseCase = saveProjectUseCase,
        )

    @Test
    fun `initial state is empty when projectId is null`() =
        runTest {
            val viewModel = createViewModel(projectId = null)

            assertEquals("", viewModel.state.value.projectName)
            assertEquals("", viewModel.state.value.projectAddress)
        }

    @Test
    fun `loading project data updates state when projectId is provided`() =
        runTest {
            val projectId = Uuid.random()
            val project = FrontendProject(project = Project(projectId, "Test Project", "Test Address"))
            fakeProjectsDb.setProject(project)

            val viewModel = createViewModel(projectId = projectId)

            advanceUntilIdle()

            assertEquals("Test Project", viewModel.state.value.projectName)
            assertEquals("Test Address", viewModel.state.value.projectAddress)
        }

    @Test
    fun `onProjectNameChange updates state`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onProjectNameChange("New Name")

            assertEquals("New Name", viewModel.state.value.projectName)
        }

    @Test
    fun `onProjectAddressChange updates state`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onProjectAddressChange("New Address")

            assertEquals("New Address", viewModel.state.value.projectAddress)
        }

    @Test
    fun `onSaveProject with empty name sends error`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onProjectAddressChange("Address")

            viewModel.onSaveProject()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.CustomUserMessage>(error)
            assertEquals("Project name cannot be empty", error.message)
        }

    @Test
    fun `onSaveProject with empty address sends error`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onProjectNameChange("Name")

            viewModel.onSaveProject()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.CustomUserMessage>(error)
            assertEquals("Project address cannot be empty", error.message)
        }

    @Test
    fun `onSaveProject successful sends save event`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onProjectNameChange("Name")
            viewModel.onProjectAddressChange("Address")

            viewModel.onSaveProject()

            val savedId = viewModel.saveEventFlow.first()

            // Verify project is saved in DB
            val savedProjectResult = fakeProjectsDb.getProjectFlow(savedId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendProject?>>(savedProjectResult)
            val savedProject = savedProjectResult.data
            assertEquals("Name", savedProject?.project?.name)
            assertEquals("Address", savedProject?.project?.address)
        }

    @Test
    fun `onSaveProject failure sends error`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onProjectNameChange("Name")
            viewModel.onProjectAddressChange("Address")

            fakeProjectsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("Failed"))

            viewModel.onSaveProject()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
        }
}
